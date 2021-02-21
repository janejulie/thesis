import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Meso {
    private final int maxTrainingDays;
    private final int[] targetRanges;
    private final int[] targetWeek;
    private final int maxMinutesDay;
    private Model model;
    private Solution plan;

    private IntVar[] minutes;
    private IntVar[] methods;
    private IntVar[] names;
    private IntVar[][] ranges;
    private IntVar[] rangeSums;
    private IntVar[] rangeDistances;
    private IntVar overallDistance;

    private final LocalDate startDay;

    public Meso(int[] targetWeek, int[] targetRanges, int maxTrainingDays, LocalDate startDay) {
        this.maxTrainingDays = maxTrainingDays;
        this.targetRanges = targetRanges;
        this.targetWeek = targetWeek;
        this.startDay = startDay;
        this.maxMinutesDay = 60*5;
        this.model = new Model("starts on" + startDay);
        initializeModel();
        defineConstraints();
    }

    private void initializeModel() {
        // alternative to modulo constraints
        int[] stepsRanges = getSteppedArray(maxMinutesDay, 15);
        int[] stepsMinutes = getSteppedArray(maxMinutesDay, 15);
        int[] stepsSums = getSteppedArray(maxTrainingDays * maxMinutesDay * 4, 15);

        // define variables
        this.methods = model.intVarArray("methods", 28, 0, Method.values().length - 1);
        this.ranges = model.intVarMatrix("ranges", 28, Range.values().length, stepsRanges);
        this.names = model.intVarArray("names", 28, 0, SessionPool.values().length - 1);
        this.minutes = model.intVarArray("minutes", 28, stepsMinutes);
        this.rangeSums = model.intVarArray("sumsRanges", Range.values().length, stepsSums);
        this.rangeDistances = model.intVarArray("distRanges", Range.values().length, 0, 120);
        this.overallDistance = model.intVar("distance", 0, 120 * Range.values().length);
    }

    public void defineConstraints() {
        // constraint on days
        for (int day = 0; day < 28; day++) {
            addTrainingsessionPool(day, Method.Pause, SessionPool.getPause());
            addTrainingsessionPool(day, Method.Wiederholung, SessionPool.getWH());
            addTrainingsessionPool(day, Method.Intervall, SessionPool.getIV());
            addTrainingsessionPool(day, Method.Dauerleistung, SessionPool.getDL());
            addTrainingsessionPool(day, Method.Fahrtspiel, SessionPool.getFS());

            // variation of methods
            for (Method method : Method.values()) {
                IntVar var = model.intVar("counter_" + method.toString(), 2, 20);
                model.count(method.index(), methods, var).post();
            }

            // consistency on ranges and duration
            model.sum(ranges[day], "=", minutes[day]).post();

            // redundant constraint for performance
            // method is pause exactly when 0 training minutes
            model.ifOnlyIf(
                    model.arithm(minutes[day], "=", 0),
                    model.arithm(methods[day], "=", Method.Pause.index())
            );
            model.ifOnlyIf(
                    model.arithm(minutes[day], "=", 0),
                    model.arithm(names[day], "=", SessionPool.Pause.index())
            );
        }
        // constraints on weeks
        for (int week = 0; week < 4; week++) {
            int startDay = week * 7;
            IntVar[] weekVariable = new IntVar[]{
                    minutes[startDay],
                    minutes[startDay + 1],
                    minutes[startDay + 2],
                    minutes[startDay + 3],
                    minutes[startDay + 4],
                    minutes[startDay + 5],
                    minutes[startDay + 6]
            };

            // limit weekly minutes
            model.sum(weekVariable, "<=", targetWeek[week]).post();

            // limit weekly days
            model.count(0, weekVariable, model.intVar(7 - maxTrainingDays, 8-maxTrainingDays)).post();
        }

        // constaint on range columns
        IntVar[][] transposed = ArrayUtils.transpose(ranges); // ranges instead of days
        for (int i = 0; i < transposed.length; ++i) {
            // get duration in different ranges
            model.sum(transposed[i], "=", rangeSums[i]).post();
            // get distance of each range
            model.arithm(rangeSums[i].dist(targetRanges[i]).intVar(), "=", rangeDistances[i]).post();
        }
        // Minimize this variable in optimization
        model.sum(rangeDistances, "=", overallDistance).post();
    }

    private void addTrainingsessionPool(int day, Method method, SessionPool[] sessionPool) {
        ArrayList<Constraint> trainingPool = new ArrayList<>();
        ArrayList<Constraint> limitConstraint = new ArrayList<>();
        for (SessionPool session: sessionPool) {
            limitConstraint.clear();
            // set identifier as name in SessionPool
            limitConstraint.add(model.arithm(names[day], "=", session.index()));


            int[][] limits = session.getDomains();
            for(int limit = 0; limit<limits.length; limit++){
                // add constraint for each range to be in limit
                limitConstraint.add(model.arithm(ranges[day][limit], "=", model.intVar(limits[limit][0], limits[limit][1])));
            }
            // collects limits for each Session in SessionPool
            Constraint[] arrayAND= new Constraint[limitConstraint.size()];
            limitConstraint.toArray(arrayAND);
            trainingPool.add(model.and(arrayAND));
        }

        // ensures method and session match definition
        Constraint[] arrayOR= new Constraint[trainingPool.size()]; //
        model.ifThen(
                model.arithm(methods[day], "=", method.index()),
                model.or(trainingPool.toArray(arrayOR))
        );
    }

    public void solveMonthOptimized() {
        Solver solver = model.getSolver();
        plan = new Solution(model);
        solver.limitTime("15s");
        solver.plugMonitor((IMonitorSolution) () -> plan.record());
        solver.findOptimalSolution(overallDistance, false);
    }

    public Session[] getSessionsMonth() {
        if (plan != null) {
            Session[] sessions = new Session[28];
            for (int i = 0; i < 28; i++) {
                // extract data from Solution instance and create corresponding Session objects
                HashMap<Range, Integer> dis = new HashMap<>();
                dis.put(Range.KB, plan.getIntVal(ranges[i][0]));
                dis.put(Range.GA, plan.getIntVal(ranges[i][1]));
                dis.put(Range.EB, plan.getIntVal(ranges[i][2]));
                dis.put(Range.SB, plan.getIntVal(ranges[i][3]));
                dis.put(Range.K123, plan.getIntVal(ranges[i][4]));
                dis.put(Range.K45, plan.getIntVal(ranges[i][5]));

                int minute = plan.getIntVal(minutes[i]);
                Method meth = Method.values()[plan.getIntVal(methods[i])];
                int name = plan.getIntVal(names[i]);
                LocalDate day = startDay.plusDays(i);
                sessions[i] = new Session(minute, meth, dis, day, name);
            }
            return sessions;
        } else {
            throw new NullPointerException();
        }
    }

    // similar to range in py
    private int[] getSteppedArray(int end, int steps) {
        int length = end/steps;
        int[] array = new int[length];
        for (int i = 0; i < length; i++){
            array[i] = i*15;
        }
        return array;
    }

    public int[] getTargetWeek() {
        return targetWeek;
    }

    public int[] getTargetRanges() {
        return targetRanges;
    }

    public int getDistance(){
        return plan.getIntVal(overallDistance);
    }

    @Override
    public String toString() {
        return "distance = " + plan.getIntVal(overallDistance);
    }
}


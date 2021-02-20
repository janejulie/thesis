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
        this.maxMinutesDay = 300;

        this.model = new Model("end on " + startDay);
        initializeModel();
        defineConstraints();
    }

    private void initializeModel() {
        this.methods = model.intVarArray("methods", 28, 0, Method.values().length - 1);
        this.ranges = model.intVarMatrix("ranges", 28, Range.values().length, 0, maxMinutesDay);
        this.names = model.intVarArray("names", 28, 0, SessionPool.values().length - 1);
        this.minutes = model.intVarArray("minutes", 28, 0, maxMinutesDay);

        this.rangeSums = model.intVarArray("sumsRanges", Range.values().length, 0, maxTrainingDays * maxMinutesDay * 4);
        this.rangeDistances = model.intVarArray("distRanges", Range.values().length, 0, 120);
        this.overallDistance = model.intVar("distance", 0, 120 * Range.values().length);
    }

    public void defineConstraints() {
        // constraint on days
        for (int day = 0; day < 28; day++) {
            addTrainingsessionPool(day, Method.Wiederholung, SessionPool.getWH());
            addTrainingsessionPool(day, Method.Intervall, SessionPool.getIV());
            addTrainingsessionPool(day, Method.Pause, SessionPool.getPause());
            addTrainingsessionPool(day, Method.Dauerleistung, SessionPool.getDL());
            addTrainingsessionPool(day, Method.Fahrtspiel, SessionPool.getFS());

            // variation of methods min 2 of every method
            for (Method method : Method.values()) {
                IntVar var = model.intVar("counter_" + method.toString(), 2, 20);
                model.count(method.index(), methods, var).post();
            }

            for (int range = 0; range < Range.values().length; range++) {
                //ranges in 5 minute steps
                model.mod(ranges[day][range], 5, 0).post();
            }
            // trainings always in 15 minute steps
            model.mod(minutes[day], 15, 0).post();

            // minutes equal sum of minutes in ranges
            model.sum(ranges[day], "=", minutes[day]).post();

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
            IntVar[] weekVariable = new IntVar[]{minutes[startDay], minutes[startDay + 1], minutes[startDay + 2], minutes[startDay + 3], minutes[startDay + 4], minutes[startDay + 5], minutes[startDay + 6]};

            // dont train more than x minutes in a week
            model.sum(weekVariable, "<=", targetWeek[week]).post();

            // train x days in a week
            model.count(0, weekVariable, model.intVar(7 - maxTrainingDays)).post();
        }

        // constaint on ranges columns
        IntVar[][] transposed = ArrayUtils.transpose(ranges);
        for (int i = 0; i < transposed.length; ++i) {
            model.sum(transposed[i], "=", rangeSums[i]).post();
            model.arithm(rangeSums[i].dist(targetRanges[i]).intVar(), "=", rangeDistances[i]).post();
        }
        // Minimize this Variable
        model.sum(rangeDistances, "=", overallDistance).post();
    }

    private void addTrainingsessionPool(int day) {
        ArrayList<Constraint> trainingPool = new ArrayList<>();
        ArrayList<Constraint> limitConstraint = new ArrayList<>();
        SessionPoolMethod[] sessionPool = SessionPoolMethod.values();
        for (SessionPoolMethod session: sessionPool) {
            limitConstraint.clear();
            limitConstraint.add(model.arithm(names[day], "=", session.index()));  //identifier
            limitConstraint.add(model.arithm(methods[day], "=", session.getMethod()));  //identifier

            int[][] limits = session.getDomains();
            for(int limit = 0; limit<limits.length; limit++){
                limitConstraint.add(model.arithm(ranges[day][limit], "=", model.intVar(limits[limit][0], limits[limit][1])));
            }
            Constraint[] arrayAND= new Constraint[limitConstraint.size()];
            limitConstraint.toArray(arrayAND);
            trainingPool.add(model.and(arrayAND));
        }

        Constraint[] arrayOR= new Constraint[trainingPool.size()];
        model.or(trainingPool.toArray(arrayOR)).post();
    }

    private void addTrainingsessionPool(int day, Method method, SessionPool[] sessionPool) {
        ArrayList<Constraint> trainingPool = new ArrayList<>();
        ArrayList<Constraint> limitConstraint = new ArrayList<>();
        for (SessionPool session: sessionPool) {
            limitConstraint.clear();
            limitConstraint.add(model.arithm(names[day], "=", session.index()));  //identifier

            int[][] limits = session.getDomains();
            for(int limit = 0; limit<limits.length; limit++){
                limitConstraint.add(model.arithm(ranges[day][limit], "=", model.intVar(limits[limit][0], limits[limit][1])));
            }
            Constraint[] arrayAND= new Constraint[limitConstraint.size()];
            limitConstraint.toArray(arrayAND);
            trainingPool.add(model.and(arrayAND));
        }

        Constraint[] arrayOR= new Constraint[trainingPool.size()];

        model.ifThen(
                model.arithm(methods[day], "=", method.index()),
                model.or(trainingPool.toArray(arrayOR))
        );
    }

    public void solveMonthOptimized() {
        Solver solver = model.getSolver();
        plan = new Solution(model);
        IntVar[] vars = model.retrieveIntVars(false);
/*        solver.setSearch(
                Search.inputOrderUBSearch(vars)
        );*/
        solver.limitTime("10s");
        solver.plugMonitor((IMonitorSolution) () -> plan.record());
        solver.showShortStatistics();
        solver.findOptimalSolution(overallDistance, false);
    }

    public Session[] getSessionsMonth() {
        if (plan != null) {
            Session[] sessions = new Session[28];
            for (int i = 0; i < 28; i++) {
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

    public int[] getTargetWeek() {
        return targetWeek;
    }

    public int[] getTargetRanges() {
        return targetRanges;
    }

    @Override
    public String toString() {
        return "distance = " + plan.getIntVal(overallDistance);
    }
}


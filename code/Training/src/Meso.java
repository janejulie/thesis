import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.HashMap;

public class Meso {
    private final HashMap<Integer, Integer> targetMinutes;
    protected int[] maxWeekMinutes;
    protected int maxDays;
    protected int maxMinutesDay;
    private Model model;
    private IntVar[] minutes;
    private IntVar[] methods;
    private IntVar[][] ranges;
    private IntVar[] rangeSums;
    private IntVar[] rangeDistances;
    private IntVar overallDistance;
    protected Double[] mesoIntensity;
    protected Solution plan;
    private Solver solver;

    public Meso(double intensity, HashMap<Range, Double> targetPercent, int maxMinutesWeek, int weeklyDays) {
        this.mesoIntensity = new Double[]{0.7, 0.8, 0.9, 1.0};
        this.maxWeekMinutes = Arrays.stream(mesoIntensity).mapToInt(i -> (int) (i * maxMinutesWeek * intensity)).toArray();
        this.targetMinutes = new HashMap<>();
        targetPercent.forEach((k, v) -> targetMinutes.put(k.index(), (int) (v * Arrays.stream(maxWeekMinutes).sum())));
        this.maxDays = weeklyDays;
        this.maxMinutesDay = 600;

        System.out.println(this);
        this.model = new Model("training");
        this.solver = model.getSolver();
        this.plan = new Solution(model);

        initializeModel();
        defineConstraints();
    }

    private void initializeModel() {
        this.ranges = model.intVarMatrix("ranges", 28, Range.values().length, 0, maxMinutesDay, false);
        this.methods = model.intVarArray("methods", 28, 0, Method.values().length - 1, false);
        this.minutes = model.intVarArray("minutes", 28, 0, maxMinutesDay, false);

        this.rangeSums = model.intVarArray("sumsRanges", Range.values().length, 0, maxDays * maxMinutesDay * 4, false);
        this.rangeDistances = model.intVarArray("distRanges", Range.values().length, 0, maxDays * maxMinutesDay * 4, false);
        this.overallDistance = model.intVar("distance", 0, maxDays * maxMinutesDay * 4 * Range.values().length, false);
    }

    private IntVar[] getColumn(IntVar[][] matrix, int colNum) {
        IntVar[] column = new IntVar[matrix.length];
        for (int rowNum = 0; rowNum < matrix.length; rowNum++) {
            column[rowNum] = matrix[rowNum][colNum];
        }
        return column;
    }

    private IntVar[] getColumn(IntVar[][] matrix, Range range) {
        return getColumn(matrix, range.index());
    }

    public void defineConstraints() {

        // variation of methods
        for (Method method : Method.values()) {
            IntVar var = model.intVar("counter_" + method.toString(), 2, 28);
            model.count(method.index(), methods, var).post();
        }

        // constraints on weeks
        for (int week = 0; week < 4; week++) {
            int startDay = week * 7;
            IntVar[] weekVariable = new IntVar[]{minutes[startDay], minutes[startDay + 1], minutes[startDay + 2], minutes[startDay + 3], minutes[startDay + 4], minutes[startDay + 5], minutes[startDay + 6]};

            // dont train more than x minutes in a week with small fault tolerance
            model.sum(weekVariable, "<=", maxWeekMinutes[week]).post();
            model.sum(weekVariable, ">", maxWeekMinutes[week] - 30).post();

            // train x days in a week
            model.count(0, weekVariable, model.intVar(7 - maxDays)).post();
        }

        // constraint on days
        for (int day = 0; day < 28; day++) {
            // minutes equal sum of minutes in ranges
            model.sum(ranges[day], "=", minutes[day]).post();

            // trainings always in 15 minute steps
            model.mod(minutes[day], 15, 0).post();

            // method is pause exactly when 0 training minutes
            model.ifOnlyIf(
                    model.arithm(minutes[day], "=", 0),
                    model.arithm(methods[day], "=", Method.PAUSE.index())
            );

            // compensation training only in DL
            model.ifThen(
                    model.arithm(ranges[day][Range.KB.index()], ">", 0),
                    model.arithm(methods[day], "=", Method.DAUERLEISTUNG.index())
            );
            /*
            model.ifThen(
                    model.arithm(methods[day], "=", Method.DAUERLEISTUNG.index()),
                    model.and(
                            model.or(
                                    model.and(
                                            model.arithm(ranges[day][Range.KB.index()], ">", 60),
                                            model.arithm(ranges[day][Range.KB.index()], "<", 60 * 4),
                                            model.arithm(ranges[day][Range.GA.index()], "=", 0)

                                    ),
                                    model.and(
                                            model.arithm(ranges[day][Range.GA.index()], ">", 60),
                                            model.arithm(ranges[day][Range.GA.index()], "<", 60 * 4),
                                            model.arithm(ranges[day][Range.KB.index()], "=", 0)

                                    )
                            ),
                            model.arithm(ranges[day][Range.EB.index()], "=", 0),
                            model.arithm(ranges[day][Range.SB.index()], "=", 0),
                            model.arithm(ranges[day][Range.K123.index()], "=", 0),
                            model.arithm(ranges[day][Range.K45.index()], "=", 0)
                    )
            );

            model.ifThen(
                    model.arithm(methods[day], "=", Method.INTERVALL.index()),
                    model.and(
                            model.arithm(ranges[day][Range.GA.index()], "=", 45),
                            model.arithm(minutes[day], ">", 0)
                    )
            );

            model.ifThen(
                    model.arithm(methods[day], "=", Method.WIEDERHOLUNG.index()),
                    model.and(
                            model.arithm(ranges[day][Range.GA.index()], "=", 60),
                            model.arithm(ranges[day][Range.KB.index()], "=", minutes[day].sub(60).div(10).mul(6).intVar()),
                            model.arithm(ranges[day][Range.EB.index()], "=", minutes[day].sub(60 + 15).div(10).mul(4).intVar()),
                            model.arithm(ranges[day][Range.K123.index()], "=", 0),
                            model.arithm(ranges[day][Range.K45.index()], "=", 0)
                    )
            );
             */
        }

        // constaint on ranges
        for (int range = 0; range < Range.values().length; range++) {
            // sum of minutes in different performance ranges -> optimize
            IntVar[] rangeColumm = new IntVar[28];
            for (int day = 0; day < 28; day++) {
                rangeColumm[day] = ranges[day][range];
            }

            model.sum(rangeColumm, "=", rangeSums[range]).post();
            // Calculate distance to the target Minutes for each performance ranges
            model.arithm(rangeSums[range].dist(targetMinutes.get(range)).intVar(), "=", rangeDistances[range]).post();
        }

        // Minimize this Variable
        model.sum(rangeDistances, "=", overallDistance).post();

    }

    public void solveMonthSimple() {
        solver.streamSolutions().forEach(s -> System.out.println(overallDistance.getValue()));
        plan = solver.findSolution();
        solver.printShortStatistics();
    }

    public void solveMonthOptimized() {
        solver.limitTime("60s");
        /*
        solver.setSearch(Search.minDomUBSearch(minutes));
        solver.setSearch(Search.randomSearch(methods, 100));
        solver.setSearch(
                Search.inputOrderLBSearch(overallDistance)
        );
                for (IntVar[] day : ranges){
            solver.setSearch(Search.minDomUBSearch());
        }
        solver.setSearch(Search.inputOrderLBSearch(rangeSums));
        */

        model.setObjective(false, overallDistance);

        int od = Integer.MAX_VALUE;
        while (solver.solve()){
            int newod = overallDistance.getValue();
            System.out.println(newod);
            if (od > newod) {
                od = overallDistance.getValue();
                plan.record();
            }
        }

        System.out.println(overallDistance.getValue());
        // Achtung die Werte von overalldistance kommen eben nicht an. Hier ist das Problem beim Tracken der Solution Objekte
        solver.printStatistics();
    }

    public Session[] getSessionsMonth() {
        if (plan != null) {
            Session[] sessions = new Session[28];
            for (int i = 0; i < 28; i++) {
                HashMap dis = new HashMap();
                dis.put(Range.KB, plan.getIntVal(ranges[i][0]));
                dis.put(Range.GA, plan.getIntVal(ranges[i][1]));
                dis.put(Range.EB, plan.getIntVal(ranges[i][2]));
                dis.put(Range.SB, plan.getIntVal(ranges[i][3]));
                dis.put(Range.K123, plan.getIntVal(ranges[i][4]));
                dis.put(Range.K45, plan.getIntVal(ranges[i][5]));

                int minute = plan.getIntVal(minutes[i]);
                Method meth = Method.values()[plan.getIntVal(methods[i])];

                sessions[i] = new Session(minute, meth, dis);

            }
            return sessions;
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public String toString() {
        return "ranges=" + targetMinutes + "<br>" +
                "maxWeekMinutes=" + Arrays.toString(maxWeekMinutes) + "<br>" +
                "distances=" + Arrays.toString(rangeDistances) + "<br>" +
                "over_dist=" + overallDistance + "<br>";
    }
}


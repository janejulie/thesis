import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.variables.Largest;
import org.chocosolver.solver.variables.IntVar;
import java.util.Arrays;
import java.util.HashMap;

public class Meso {
    private final int[] targetMinutes;
    protected int[] maxWeekMinutes;
    protected int maxDays;
    protected int maxMinutesDay;
    private Model model;
    private IntVar[] minutes;
    private IntVar[] methods;
    private IntVar[][] ranges;
    private IntVar trainingMinutes;
    private IntVar overallDistance;
    protected Double[] mesoIntensity;
    protected Solution plan;
    private IntVar[] rangeSums;
    private IntVar[] rangeDistances;

    public Meso(double intensity, HashMap<Range, Double> targetPercent, int maxMinutesWeek, int weeklyDays) {
        this.mesoIntensity = new Double[]{0.7, 0.8, 0.9, 1.0};
        this.maxWeekMinutes = Arrays.stream(mesoIntensity).mapToInt(i->(int)(i*maxMinutesWeek*intensity)).toArray();
        this.targetMinutes = new int[Range.values().length];
        int sum = Arrays.stream(maxWeekMinutes).sum();
        this.targetMinutes[0] = (int) (sum * targetPercent.get(Range.KB));
        this.targetMinutes[1] = (int) (sum * targetPercent.get(Range.GA));
        this.targetMinutes[2] = (int) (sum * targetPercent.get(Range.EB));
        this.targetMinutes[3] = (int) (sum * targetPercent.get(Range.SB));
        this.targetMinutes[4] = (int) (sum * targetPercent.get(Range.K123));
        this.targetMinutes[5] = (int) (sum * targetPercent.get(Range.K45));

        this.maxDays = weeklyDays;
        this.maxMinutesDay = 60*10;

        initializeModel();
        defineConstraints();
    }

    private void initializeModel() {
        this.model = new Model("training");
        this.minutes = model.intVarArray("minutes",28, 0, maxMinutesDay, false);
        this.methods = model.intVarArray("methods",28,  0, Method.values().length-1, false);
        this.ranges = model.intVarMatrix("ranges", 28, Range.values().length, 0, maxMinutesDay, false);
        this.trainingMinutes = model.intVar("totalMinutes", 0, 28*maxMinutesDay, false);

        this.rangeSums = model.intVarArray("sumsRanges", Range.values().length, 0, maxDays * maxMinutesDay * 4, false);
        this.rangeDistances = model.intVarArray("distRanges", Range.values().length, 0, maxDays * maxMinutesDay * 4, false);
        this.overallDistance = model.intVar("distance", 0, maxDays * maxMinutesDay * 4 * Range.values().length, false);
    }

    private IntVar[] getColumn(IntVar[][] matrix, int colNum){
        IntVar[] column = new IntVar[matrix.length];
        for (int rowNum = 0; rowNum<matrix.length; rowNum++){
            column[rowNum] = matrix[rowNum][colNum];
        }
        return column;
    }
    private IntVar[] getColumn(IntVar[][] matrix, Range performanceRange){
        return getColumn(matrix, performanceRange.ordinal());
    }

    public void defineConstraints(){
        // get sum of trainingminutes to maximize them
        model.sum(minutes, "=", trainingMinutes).post();

        // variation of methods
        for (Method method : Method.values()) {
            IntVar var = model.intVar("counter_" + method.toString(), 2, 28);
            model.count(method.ordinal(), methods, var).post();
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
            model.arithm(rangeSums[range].dist(targetMinutes[range]).intVar(), "=", rangeDistances[range]).post();
        }

        model.sum(rangeDistances, "=", overallDistance).post();

        // CONSTRAINTS for weeks
        for (int week = 0; week <4; week++) {
            int startDay = week*7;
            IntVar[] weekVariable = new IntVar[]{minutes[startDay], minutes[startDay + 1], minutes[startDay + 2], minutes[startDay + 3], minutes[startDay + 4], minutes[startDay + 5], minutes[startDay + 6]};
            // time limit on week trainings
            model.sum(weekVariable, "<=", maxWeekMinutes[week]).post();
            model.sum(weekVariable, ">", maxWeekMinutes[week]-30).post();

            // dont train more than x days in a week
            model.count(0, weekVariable, model.intVar(6-maxDays)).post();
        }

        // Training minutes in 15 minute steps
        for (int i = 0; i<minutes.length; i++){
            model.mod(minutes[i], 15, 0).post();
            model.sum(ranges[i], "=", minutes[i]).post();

            // Kompensation nur als Dauerleistung
            model.ifThen(
                    model.arithm(ranges[i][Range.KB.ordinal()], ">", 0),
                    model.arithm(methods[i], "=", Method.DAUERLEISTUNG.ordinal())
            );

            // PAUSE
            model.ifOnlyIf(
                    model.arithm(minutes[i], "=", 0),
                    model.arithm(methods[i], "=", Method.PAUSE.ordinal())
            );

            model.ifThen(
                    model.arithm(methods[i], "=", Method.DAUERLEISTUNG.ordinal()),
                    model.and(
                            model.or(
                                model.and(
                                        model.arithm(ranges[i][Range.KB.ordinal()], ">", 60),
                                        model.arithm(ranges[i][Range.KB.ordinal()], "<", 60*4),
                                        model.arithm(ranges[i][Range.GA.ordinal()], "=", 0)

                                        ),
                                model.and(
                                        model.arithm(ranges[i][Range.GA.ordinal()], ">", 60),
                                        model.arithm(ranges[i][Range.GA.ordinal()], "<", 60*4),
                                        model.arithm(ranges[i][Range.KB.ordinal()], "=", 0)

                                        )
                            ),
                            model.arithm(ranges[i][Range.EB.ordinal()], "=", 0),
                            model.arithm(ranges[i][Range.SB.ordinal()], "=", 0),
                            model.arithm(ranges[i][Range.K123.ordinal()], "=", 0),
                            model.arithm(ranges[i][Range.K45.ordinal()], "=", 0)
                    )
            );

            model.ifThen(
                    model.arithm(methods[i], "=", Method.INTERVALL.ordinal()),
                    model.and(
                            model.arithm(ranges[i][Range.GA.ordinal()], "=", 45),
                            model.arithm(minutes[i], ">", 0)
                    )
            );

            model.ifThen(
                    model.arithm(methods[i], "=", Method.WIEDERHOLUNG.ordinal()),
                    model.and(
                            model.arithm(ranges[i][Range.GA.ordinal()], "=", 60),
                            model.arithm(ranges[i][Range.KB.ordinal()], "=", minutes[i].sub(60).div(10).mul(6).intVar()),
                            model.arithm(ranges[i][Range.EB.ordinal()], "=", minutes[i].sub(60+15).div(10).mul(4).intVar()),
                            model.arithm(ranges[i][Range.K123.ordinal()], "=", 0),
                            model.arithm(ranges[i][Range.K45.ordinal()], "=", 0)
                    )
            );


        }
    }

    public void solveWithoutOptimization(){
        Solver solver = model.getSolver();
        solver.limitTime("20s");
        plan = solver.findOptimalSolution(overallDistance, false);
        System.out.println(plan);
        solver.printShortStatistics();
    }


    public Session[] getSessionsMonth(){
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

            sessions[i] = new Session(minute, meth , dis);

        }
        return sessions;
    }

    @Override
    public String toString() {
        return "ranges=" + targetMinutes + "<br>" +
                "maxWeekMinutes=" + Arrays.toString(maxWeekMinutes) + "<br>" +
                "dist=" + rangeDistances + "<br>" +
                "overalldistance = " + overallDistance.getValue();
    }
}


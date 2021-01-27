import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.Largest;
import org.chocosolver.solver.search.strategy.selectors.variables.MaxDelta;
import org.chocosolver.solver.search.strategy.selectors.variables.MaxRegret;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.criteria.Criterion;

import java.util.Arrays;
import java.util.HashMap;

public class Meso {
    private final HashMap targetRanges;
    protected int[] maxWeekMinutes;
    protected int maxDays;
    protected int maxMinutesDay;
    private Model model;
    private IntVar[] minutes;
    private IntVar[] methods;
    private IntVar[][] ranges;
    private IntVar trainingMinutes;
    protected Double[] mesoIntensity;


    protected Solution plan;

    public Meso(double intensity, HashMap targetRanges, int maxMinutesWeek, int weeklyDays) {
        this.mesoIntensity = new Double[]{0.7, 0.8, 0.9, 1.0};
        this.maxWeekMinutes = Arrays.stream(mesoIntensity).mapToInt(i->(int)(i*maxMinutesWeek*intensity)).toArray();
        this.targetRanges = targetRanges;
        this.maxDays = weeklyDays;
        this.maxMinutesDay = 60*10;

        initializeModel();
        defineConstraints();
        solveMonth();
    }

    private void initializeModel() {
        this.model = new Model("training");
        this.minutes = model.intVarArray("minutes",28, 0, maxMinutesDay, false);
        this.methods = model.intVarArray("methods",28,  0, Methods.values().length-1, false);
        this.ranges = model.intVarMatrix("ranges", 28, PerformanceRange.values().length, 0, maxMinutesDay);
        this.trainingMinutes = model.intVar("totalMinutes", 0, 28*maxMinutesDay);
    }

    public void defineConstraints(){
        // get sum of trainingminutes to maximize them
        model.sum(minutes, "=", trainingMinutes).post();

        // CONSTRAINTS for weeks
        for (int week = 0; week <4; week++) {
            int startDay = week*7;
            IntVar[] weekVariable = new IntVar[]{minutes[startDay], minutes[startDay + 1], minutes[startDay + 2], minutes[startDay + 3], minutes[startDay + 4], minutes[startDay + 5], minutes[startDay + 6]};
            // time limit on week trainings
            model.sum(weekVariable, "<=", maxWeekMinutes[week]).post();

            // dont train more than x days in a week
            model.count(0, weekVariable, model.intVar(6-maxDays)).post();
        }

        // Training minutes in 15 minute steps
        for (int i = 0; i<minutes.length; i++){
            model.mod(minutes[i], 15, 0).post();
            model.sum(ranges[i], "=", minutes[i]).post();


            // PAUSE
            model.ifOnlyIf(
                    model.arithm(minutes[i], "=", 0),
                    model.arithm(methods[i], "=", Methods.PAUSE.ordinal())
            );
            /*
            model.ifThen(
                    model.arithm(methods[i], "=", Methods.DAUERLEISTUNG.ordinal()),
                    model.and(
                            model.arithm(ranges[i][PerformanceRange.GA.ordinal()], ">", 60),
                            model.arithm(ranges[i][PerformanceRange.GA.ordinal()], "<", 60*4)
                    )
            );

            model.ifThen(
                    model.arithm(methods[i], "=", Methods.INTERVALL.ordinal()),
                    model.arithm(minutes[i], ">", 30)
            );

            model.ifThen(
                    model.arithm(methods[i], "=", Methods.WIEDERHOLUNG.ordinal()),
                    model.arithm(minutes[i], ">", 15)
            );

             */
        }
    }

    private void solveMonth() {
        model.setObjective(Model.MAXIMIZE, trainingMinutes);
        Solver solver = model.getSolver();
        plan = solver.findOptimalSolution(
                model.getObjective().asIntVar(),
                true,
                new TimeCounter(model, 300000000)
        );
        solver.showStatistics();
        System.out.println(plan);
    }

    public Session[] getSessionsMonth(){
        Session[] sessions = new Session[28];
        for (int i = 0; i < 28; i++) {
            HashMap dis = new HashMap();
            dis.put(PerformanceRange.KB, ranges[i][0].getValue());
            dis.put(PerformanceRange.GA, ranges[i][1].getValue());
            dis.put(PerformanceRange.EB, ranges[i][2].getValue());
            dis.put(PerformanceRange.SB, ranges[i][3].getValue());
            dis.put(PerformanceRange.K123, ranges[i][4].getValue());
            dis.put(PerformanceRange.K45, ranges[i][5].getValue());
            sessions[i] = new Session(minutes[i].getValue(), Methods.values()[methods[i].getValue()], dis);
        }
        return sessions;
    }

    @Override
    public String toString() {
        return "Meso{" +
                "ranges=" + ranges +
                ", maxWeekMinutes=" + Arrays.toString(maxWeekMinutes) +
                ", maxDays=" + maxDays +
                ", maxMinutesDay=" + maxMinutesDay +
                '}';
    }
}


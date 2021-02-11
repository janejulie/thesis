import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;

public class Meso {
    private final HashMap<Integer, Integer> targetMinutes;
    protected int[] maxWeekMinutes;
    protected int maxDays;
    protected int maxMinutesDay;
    private Model model;
    private Solution plan;
    private Solver solver;

    private IntVar[] minutes;
    private IntVar[] methods;
    private IntVar[][] ranges;
    private IntVar[] rangeSums;
    private IntVar[] rangeDistances;
    private IntVar overallDistance;
    private Double[] mesoIntensity;
    private LocalDate startDay;


    public Meso(double intensity, HashMap<Range, Double> targetPercent, int maxMinutesWeek, int weeklyDays, LocalDate startDay) {
        this.mesoIntensity = new Double[]{0.7, 0.8, 0.9, 1.0};
        this.maxWeekMinutes = Arrays.stream(mesoIntensity).mapToInt(i -> (int) (i * maxMinutesWeek * intensity)).toArray();
        this.targetMinutes = new HashMap<>();
        targetPercent.forEach((k, v) -> targetMinutes.put(k.index(), (int) (v * Arrays.stream(maxWeekMinutes).sum())));
        int sum = targetMinutes.values().stream().reduce(0, Integer::sum);
        System.out.println(targetMinutes + " summe = " + sum);
        this.maxDays = weeklyDays;
        this.maxMinutesDay = 360;
        this.startDay = startDay;

        this.model = new Model("training  " + intensity);

        initializeModel();
        defineConstraints();
    }

    private void initializeModel() {
        this.methods = model.intVarArray("methods", 28, 0, Method.values().length - 1, false);
        this.minutes = model.intVarArray("minutes", 28, 0, maxMinutesDay, true);
        this.ranges = model.intVarMatrix("ranges", 28, Range.values().length, 0, maxMinutesDay, false);

        this.rangeSums = model.intVarArray("sumsRanges", Range.values().length, 0, maxDays * maxMinutesDay * 4, true);
        this.rangeDistances = model.intVarArray("distRanges", Range.values().length, 0, 120, false);
        this.overallDistance = model.intVar("distance", 0, 60 * Range.values().length, true);
    }

    public void defineConstraints() {
        // variation of methods min 2 of every method
        for (Method method : Method.values()) {
            IntVar var = model.intVar("counter_" + method.toString(), 2, 20);
            model.count(method.index(), methods, var).post();
        }
        // constraints on weeks
        for (int week = 0; week < 4; week++) {
            int startDay = week * 7;
            IntVar[] weekVariable = new IntVar[]{minutes[startDay], minutes[startDay + 1], minutes[startDay + 2], minutes[startDay + 3], minutes[startDay + 4], minutes[startDay + 5], minutes[startDay + 6]};

            // dont train more than x minutes in a week with small fault tolerance
            model.sum(weekVariable, "<=", maxWeekMinutes[week]).post();

            // train x days in a week
            model.count(0, weekVariable, model.intVar(7 - maxDays, 6)).post();
        }

        // constraint on days
        for (int day = 0; day < 28; day++) {
            // minutes equal sum of minutes in ranges
            model.sum(ranges[day], "=", minutes[day]).post();

            // trainings always in 15 minute steps
            model.mod(minutes[day], 15, 0).post();
            for (int range = 0; range < Range.values().length; range++) {
                //ranges 5 minute diskretization
                model.mod(ranges[day][range], 15, 0).post();
            }

            // method is pause exactly when 0 training minutes
            model.ifOnlyIf(
                    model.arithm(minutes[day], "=", 0),
                    model.arithm(methods[day], "=", Method.PAUSE.index())
            );

            model.ifThen(
                    model.arithm(methods[day], "=", Method.INTERVALL.index()),
                    model.or(
                            //Bergausdauer
                            model.and(
                                    model.arithm(ranges[day][Range.KB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(30, 90)),
                                    model.arithm(ranges[day][Range.EB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", model.intVar(15, 120))
                            ),
                            //Schnelligkeitsausdauer
                            model.and(
                                    model.arithm(ranges[day][Range.KB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(60, 180)),
                                    model.arithm(ranges[day][Range.EB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.SB.index()], "=", model.intVar(15, 45)),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            )
                    )
            );
            model.ifThen(
                    model.arithm(methods[day], "=", Method.FAHRTSPIEL.index()),
                    model.or(
                            // Extensives Fahrtspiel
                            model.and(
                                    model.arithm(ranges[day][Range.KB.index()], "=", model.intVar(0, 30)),
                                    model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(30, 240)),
                                    model.arithm(ranges[day][Range.EB.index()], "=", model.intVar(30, 240)),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            ),
                            //Schnelligkeitsausdauer
                            model.and(
                                    model.arithm(ranges[day][Range.KB.index()], "=", model.intVar(0, 30)),
                                    model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(30, 180)),
                                    model.arithm(ranges[day][Range.EB.index()], "=", model.intVar(15, 120)),
                                    model.arithm(ranges[day][Range.SB.index()], "=", model.intVar(15, 120)),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            )
                    )
            );

            model.ifThen(
                    model.arithm(methods[day], "=", Method.DAUERLEISTUNG.index()),
                    model.or(
                            //1 Rekom
                            model.and(
                                    model.arithm(ranges[day][Range.KB.index()], "=", model.intVar(30, 180)),
                                    model.arithm(ranges[day][Range.GA.index()], "=", 0),
                                    model.arithm(ranges[day][Range.EB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            ),
                            // 2 und 3 Extensive Fahrt und Fettstoffwechsel
                            model.and(
                                    model.arithm(ranges[day][Range.KB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(60, 360)),
                                    model.arithm(ranges[day][Range.EB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            ),
                            // 5 Intensive Fahrt
                            model.and(
                                    model.arithm(ranges[day][Range.KB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.GA.index()], "=", 60),
                                    model.arithm(ranges[day][Range.EB.index()], "=", model.intVar(15, 120)),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            ),
                            // 8 Extensive Kraftausdauer
                            model.and(
                                    model.arithm(ranges[day][Range.KB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(30, 60)),
                                    model.arithm(ranges[day][Range.EB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", model.intVar(30, 150)),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            ),
                            // 13 Einzelfahrt
                            model.and(
                                    model.arithm(ranges[day][Range.KB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.GA.index()], "=", 60),
                                    model.arithm(ranges[day][Range.EB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.SB.index()], "=", model.intVar(30, 60)),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            )
                    )
            );

            model.ifThen(
                    model.arithm(methods[day], "=", Method.WIEDERHOLUNG.index()),
                    // 12 Sprinttraining
                    model.and(
                            model.arithm(ranges[day][Range.KB.index()], "=", 0),
                            model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(30, 60)),
                            model.arithm(ranges[day][Range.EB.index()], "=", 0),
                            model.arithm(ranges[day][Range.SB.index()], "=", model.intVar(15, 100)),
                            model.arithm(ranges[day][Range.K123.index()], "=", 0),
                            model.arithm(ranges[day][Range.K45.index()], "=", 0)
                    )
            );
        }

        // constaint on ranges columns
        IntVar[][] transposed = ArrayUtils.transpose(ranges);
        for (int i = 0; i < transposed.length; ++i) {
            model.sum(transposed[i], "=", rangeSums[i]).post();
            model.arithm(rangeSums[i].dist(targetMinutes.get(i)).intVar(), "=", rangeDistances[i]).post();
        }

        // Minimize this Variable
        model.sum(rangeDistances, "=", overallDistance).post();

    }

    public void solveMonthOptimized() {
        solver = model.getSolver();
        plan = new Solution(model);

        System.out.println(targetMinutes);
        solver.plugMonitor((IMonitorSolution) () -> {
                    plan.record();
                    printPretty();
                }
        );

        solver.limitTime("30s");
        solver.showShortStatistics();
        solver.findOptimalSolution(overallDistance, false);
    }

    private void printPretty() {
        for (IntVar i : rangeDistances) {
            System.out.println(i);
        }
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

                LocalDate day = startDay.plusDays(i);
                sessions[i] = new Session(minute, meth, dis, day);

            }
            return sessions;
        } else {
            throw new NullPointerException();
        }
    }

    public Solution getPlan() {
        return plan;
    }

    @Override
    public String toString() {
        return this.getClass() + "distance = " + overallDistance;
    }
}


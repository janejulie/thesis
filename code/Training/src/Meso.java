import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.time.LocalDate;
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
        this.maxMinutesDay = 360;

        this.model = new Model("training  " + startDay);
        initializeModel();
        defineConstraints();
    }

    private void initializeModel() {
        this.methods = model.intVarArray("methods", 28, 0, Method.values().length - 1, false);
        this.minutes = model.intVarArray("minutes", 28, 0, maxMinutesDay, true);
        this.ranges = model.intVarMatrix("ranges", 28, Range.values().length, 0, maxMinutesDay, false);
        this.names = model.intVarArray("names", 28, 0, SessionName.values().length - 1, false);

        this.rangeSums = model.intVarArray("sumsRanges", Range.values().length, 0, maxTrainingDays * maxMinutesDay * 4, true);
        this.rangeDistances = model.intVarArray("distRanges", Range.values().length, 0, 120, false);
        this.overallDistance = model.intVar("distance", 0, 120 * Range.values().length, true);
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

            // dont train more than x minutes in a week
            model.sum(weekVariable, "<=", targetWeek[week]).post();

            // train x days in a week
            model.count(0, weekVariable, model.intVar(7 - maxTrainingDays, 8 - maxTrainingDays)).post();
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
                    model.arithm(methods[day], "=", Method.Pause.index())
            );
            model.ifThen(
                    model.arithm(methods[day], "=", Method.Pause.index()),
                    model.arithm(names[day], "=", SessionName.Pause.index())
            );

            model.ifThen(
                    model.arithm(methods[day], "=", Method.Intervall.index()),
                    model.or(
                            //Bergausdauer
                            model.and(
                                    model.arithm(names[day], "=", SessionName.Intensive_Kraftausdauer.index()),
                                    model.arithm(ranges[day][Range.KB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(30, 90)),
                                    model.arithm(ranges[day][Range.EB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", model.intVar(15, 120))
                            ),
                            //Schnelligkeitsausdauer
                            model.and(
                                    model.arithm(names[day], "=", SessionName.Schnelligkeitsausdauer.index()),
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
                    model.arithm(methods[day], "=", Method.Fahrtspiel.index()),
                    model.or(
                            // Extensives Fahrtspiel
                            model.and(
                                    model.arithm(names[day], "=", SessionName.Extensives_Fahrtspiel.index()),
                                    model.arithm(ranges[day][Range.KB.index()], "=", model.intVar(0, 30)),
                                    model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(30, 240)),
                                    model.arithm(ranges[day][Range.EB.index()], "=", model.intVar(30, 240)),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            ),
                            //Intensives Fahrtspiel
                            model.and(
                                    model.arithm(names[day], "=", SessionName.Intensives_Fahrtspiel.index()),
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
                    model.arithm(methods[day], "=", Method.Dauerleistung.index()),
                    model.or(
                            //1 Rekom
                            model.and(
                                    model.arithm(names[day], "=", SessionName.Kompensationsfahrt.index()),
                                    model.arithm(ranges[day][Range.KB.index()], "=", model.intVar(30, 180)),
                                    model.arithm(ranges[day][Range.GA.index()], "=", 0),
                                    model.arithm(ranges[day][Range.EB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            ),
                            // 2 und 3 Extensive Fahrt und Fettstoffwechsel
                            model.and(
                                    model.arithm(names[day], "=", SessionName.Extensive_Fahrt.index()),
                                    model.arithm(ranges[day][Range.KB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(60, 360)),
                                    model.arithm(ranges[day][Range.EB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            ),
                            // 5 Intensive Fahrt
                            model.and(
                                    model.arithm(names[day], "=", SessionName.Intensive_Fahrt.index()),
                                    model.arithm(ranges[day][Range.KB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.GA.index()], "=", 60),
                                    model.arithm(ranges[day][Range.EB.index()], "=", model.intVar(15, 120)),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            ),
                            // 8 Extensive Kraftausdauer
                            model.and(
                                    model.arithm(names[day], "=", SessionName.Extensive_Kraftfahrt.index()),
                                    model.arithm(ranges[day][Range.KB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.GA.index()], "=", model.intVar(30, 60)),
                                    model.arithm(ranges[day][Range.EB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.SB.index()], "=", 0),
                                    model.arithm(ranges[day][Range.K123.index()], "=", model.intVar(30, 150)),
                                    model.arithm(ranges[day][Range.K45.index()], "=", 0)
                            ),
                            // 13 Einzelfahrt
                            model.and(
                                    model.arithm(names[day], "=", SessionName.Einzelzeitfahrt.index()),
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
                    model.arithm(methods[day], "=", Method.Wiederholung.index()),
                    // 12 Sprinttraining
                    model.and(
                            model.arithm(names[day], "=", SessionName.Sprinttraining.index()),
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
            model.arithm(rangeSums[i].dist(targetRanges[i]).intVar(), "=", rangeDistances[i]).post();
        }
        // Minimize this Variable
        model.sum(rangeDistances, "=", overallDistance).post();
    }

    public void solveMonthOptimized() {
        Solver solver = model.getSolver();
        plan = new Solution(model);
        solver.limitTime("30s");
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
                int name = names[i].getValue();
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
        return this.getClass() + "distance = " + overallDistance.getValue();
    }
}


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.DoubleStream;

public abstract class Macro {
    private final int numMonths;
    private final int maxTrainingMinutes;
    private final int maxTrainingDays;
    private final LocalDate compDay;
    protected HashMap<Range, Double> ranges = new HashMap<>();

    private ArrayList<Meso> mesos = new ArrayList<>();

    public Macro(int numMonths, int maxMinutes, int maxDays, LocalDate compDay) {
        this.compDay = compDay;
        this.numMonths = numMonths;
        this.maxTrainingMinutes = maxMinutes;
        this.maxTrainingDays = maxDays;
        setPerformanceRanges();
        validateRanges();
        createMesos();
    }

    private void createMesos() {
        double steps = 0.1;
        double minIntensity = 1 - steps*(numMonths -1);
        for (int i = 0; i < numMonths; i++){
            LocalDate startDay = compDay.minusDays(28*(numMonths-i));
            Meso meso = new Meso(minIntensity + i*steps, getPerformanceRanges((int)(i/steps)), maxTrainingMinutes, maxTrainingDays, startDay);
            mesos.add(meso);
        }
    }

    public void solvePlan() {
        mesos.parallelStream().forEach((meso) -> {
            meso.solveMonthOptimized();
        });
    }

    abstract void setPerformanceRanges();
    abstract String getName();

    public void validateRanges(){
        double sum = ranges.values().stream().flatMapToDouble(e -> DoubleStream.of(e)).sum();
        if (sum != 1.0) {
            throw new ArithmeticException("Distribution on ranges unequal to 100 percent");
        }
    }

    public ArrayList<Session> getSessions() {
        ArrayList<Session> sessions = new ArrayList<>();
        for (Meso meso : mesos) {
            sessions.addAll(Arrays.asList(meso.getSessionsMonth()));
        }
        return sessions;
    }

    HashMap<Range, Double> getPerformanceRanges(int month){
        return ranges;
    }

    public int getNumMonths() {
        return numMonths;
    }

    public int getMaxTrainingMinutes() {
        return maxTrainingMinutes;
    }

    public int getMaxTrainingDays() {
        return maxTrainingDays;
    }

    public LocalDate getCompDay() {
        return compDay;
    }

    public HashMap<Range, Double> getRanges() {
        return ranges;
    }

    public ArrayList<Meso> getMesos() {
        return mesos;
    }

    public String getTargets(){
        return mesos.toString();
    }

    @Override
    public String toString() {
        return "Macro{" +
                "numMonths=" + numMonths +
                ", maxTrainingMinutes=" + maxTrainingMinutes +
                ", maxTrainingDays=" + maxTrainingDays +
                ", performanceRanges=" + ranges +
                ", mesos=" + mesos +
                '}';
    }
}
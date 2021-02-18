import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.DoubleStream;

public abstract class Macro {
    protected final HashMap<Range, Double> ranges = new HashMap<>();
    private final int numMonths;
    private final int maxTrainingMinutes;
    private final int maxTrainingDays;
    private final LocalDate compDay;
    private final ArrayList<Meso> mesos = new ArrayList<>();

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
        int[] macroIntensity = {60, 70, 80, 90, 100};
        macroIntensity = Arrays.copyOfRange(macroIntensity, macroIntensity.length-numMonths, macroIntensity.length); // crop to plan length
        int[] mesoIntensity = {80, 90, 100, 70};

        for (int month = 0; month < numMonths; month++){
            int iMacro = macroIntensity[month];
            LocalDate startDay = compDay.minusDays(28L *(numMonths-month));
            int[] targetWeek = Arrays.stream(mesoIntensity).map(iMeso -> (iMeso * maxTrainingMinutes * iMacro)/10000).toArray(); //periodization
            targetWeek = Arrays.stream(targetWeek).map(week -> (15*(Math.round(week/15)))).toArray(); //round to 5 step precision
            int[] targetRanges = new int[6];
            int trainingMinutes = Arrays.stream(targetWeek).sum();
            for(Range r : Range.values()){
                targetRanges[r.index()] = (int) (getPerformanceRanges(month).get(r) * trainingMinutes);
            }
            targetRanges = Arrays.stream(targetRanges).map(range -> (5*(Math.round(range/5)))).toArray(); //round to 5 step precision
            Meso meso = new Meso(targetWeek, targetRanges, maxTrainingDays, startDay);
            mesos.add(meso);
        }
    }

    public void solvePlan() {
        mesos.parallelStream().forEach(Meso::solveMonthOptimized);
    }

    abstract void setPerformanceRanges();
    abstract String getName();

    public void validateRanges(){
        double sum = ranges.values().stream().flatMapToDouble(DoubleStream::of).sum();
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
        HashMap<Range, Double> copy = (HashMap<Range, Double>) ranges.clone();
        copy.replace(Range.GA,  (ranges.get(Range.GA)*100-month)/100);
        copy.replace(Range.SB,  (ranges.get(Range.SB)*100+month)/100);
        return copy;
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
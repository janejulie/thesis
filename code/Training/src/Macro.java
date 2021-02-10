import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.DoubleStream;

public abstract class Macro {
    protected HashMap<Range, Double> performanceRanges = new HashMap<>();
    private int numMonths;
    private ArrayList<Meso> mesos = new ArrayList<>();
    private ArrayList<Session> sessions = new ArrayList<>();

    public Macro(int numMonths, int maxMinutesWeek, int maxWeeklyDays) {
        this.numMonths = numMonths;
        this.setPerformanceRanges();
        this.validateRanges();

        // create Mesos
        double steps = 0.1;
        double minIntensity = 1 - steps*(numMonths-1);
        for (double i = minIntensity; i <= 1; i += steps){
            Meso meso = new Meso(i, getPerformanceRanges((int)(i/steps)), maxMinutesWeek, maxWeeklyDays);
            mesos.add(meso);
        }
    }

    public void solvePlan() {
        // parallelize solving for every single month
        mesos.parallelStream().forEach((meso) -> {
            // meso.solveMonthSimple();
            meso.solveMonthOptimized();
        });
        // collect all sessions
        for (Meso meso: mesos){
            sessions.addAll(Arrays.asList(meso.getSessionsMonth()));
        }
    }



    HashMap<Range, Double> getPerformanceRanges(int month){
        return performanceRanges;
    }

    abstract void setPerformanceRanges();

    @Override
    public String toString() {
        String msg = "<html>";
        for (Meso meso : mesos){
            msg += meso.toString() + "<br>";
        }
        msg += "</html>";
        return msg;
    }

    public void validateRanges(){
        double sum = performanceRanges.values().stream().flatMapToDouble(e -> DoubleStream.of(e)).sum();
        if (sum != 1.0) {
            throw new ArithmeticException("Ranges dont equal 1");
        }
    }

    public ArrayList<Session> getSessions(){
        return sessions;
    }
}
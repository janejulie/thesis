import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class Macro {
    protected HashMap<PerformanceRange, Double> performanceRanges = new HashMap<>();
    protected int numMonths, numDays;
    protected ArrayList<Meso> mesos = new ArrayList<Meso>();
    protected ArrayList<Session> sessions = new ArrayList<Session>();

    public Macro(int numMonths, int maxMinutesWeek, int maxWeeklyDays) {
        this.numMonths = numMonths;
        this.numDays = numMonths*28;
        this.setPerformanceRanges();

        // create Mesos
        double steps = 0.1;
        double minIntensity = 1 - steps*(numMonths-1);
        for (double i = minIntensity; i <= 1; i += steps){

            Meso meso = new Meso(i, getPerformanceRanges((int)(i/steps)), maxMinutesWeek, maxWeeklyDays);
            mesos.add(meso);
        }

        mesos.parallelStream().forEach((meso) -> {
            meso.solveWithoutOptimization();
        });

        // collect all sessions
        for (Meso meso: mesos){
            sessions.addAll(Arrays.asList(meso.getSessionsMonth()));
        }
    }

    abstract HashMap<PerformanceRange, Double> getPerformanceRanges(int month);
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

    public ArrayList<Session> getSessions(){
        return sessions;
    }
}
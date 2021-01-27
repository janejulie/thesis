import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class Macro {
    protected HashMap<PerformanceRange, Double> performanceRanges = new HashMap<>();
    protected int numMonths, numDays;
    protected ArrayList<Meso> mesos = new ArrayList<>();

    public Macro(int numMonths, int maxMinutesWeek, int maxWeeklyDays) {
        this.numMonths = numMonths;
        this.numDays = numMonths*28;

        double steps = 0.1;
        double minIntensity = 1 - steps*(numMonths-1);
        for (double i = minIntensity; i <= 1; i += steps){
            Meso meso = new Meso(i, performanceRanges, maxMinutesWeek, maxWeeklyDays);
            mesos.add(meso);
            System.out.println(meso);
        }
    }

    @Override
    public String toString() {
        return "Makro: " + performanceRanges +  "\n"
                + mesos;
    }

    public Object[][] getListOfSessions(){
        ArrayList<Session> sessions = new ArrayList();
        for (Meso meso: mesos){
            sessions.addAll(Arrays.asList(meso.getSessionsMonth()));
        }

        Object[][] tableContent = new Object[sessions.size()][8];
        for (int i = 0; i<sessions.size(); i++){
            for (int ii = 0; ii < 8; ii++){
                tableContent[i][0] = sessions.get(i).getMinutes();
                tableContent[i][1] = sessions.get(i).getMethod();
                tableContent[i][2] = sessions.get(i).getDistribution().get(PerformanceRange.KB);
                tableContent[i][3] = sessions.get(i).getDistribution().get(PerformanceRange.GA);
                tableContent[i][4] = sessions.get(i).getDistribution().get(PerformanceRange.EB);
                tableContent[i][5] = sessions.get(i).getDistribution().get(PerformanceRange.SB);
                tableContent[i][6] = sessions.get(i).getDistribution().get(PerformanceRange.K123);
                tableContent[i][7] = sessions.get(i).getDistribution().get(PerformanceRange.K45);
            }
        }
        return tableContent;
    }
}
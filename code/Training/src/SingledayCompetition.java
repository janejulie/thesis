import java.util.HashMap;

public class SingledayCompetition extends Macro {
    public SingledayCompetition(int numMonth, int maxHours, int maxDays) {
        super(numMonth, maxHours, maxDays);
    }

    @Override
    HashMap<PerformanceRange, Double> getPerformanceRanges(int month) {
        return performanceRanges;
    }

    @Override
    void setPerformanceRanges() {
        performanceRanges.put(PerformanceRange.KB, 0.1);
        performanceRanges.put(PerformanceRange.GA, 0.4);
        performanceRanges.put(PerformanceRange.EB, 0.15);
        performanceRanges.put(PerformanceRange.SB, 0.1);
        performanceRanges.put(PerformanceRange.K123, 0.1);
        performanceRanges.put(PerformanceRange.K45, 0.15);
    }
}

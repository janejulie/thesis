import java.util.HashMap;

public class SingledayCompetition extends Macro {
    public SingledayCompetition(int numMonth, int maxHours, int maxDays) {
        super(numMonth, maxHours, maxDays);
    }

    @Override
    HashMap<Range, Double> getPerformanceRanges(int month) {
        return performanceRanges;
    }

    @Override
    void setPerformanceRanges() {
        performanceRanges.put(Range.KB, 0.1);
        performanceRanges.put(Range.GA, 0.4);
        performanceRanges.put(Range.EB, 0.15);
        performanceRanges.put(Range.SB, 0.1);
        performanceRanges.put(Range.K123, 0.1);
        performanceRanges.put(Range.K45, 0.15);
    }
}

import java.util.HashMap;

public class MountainCompetition extends Macro {
    public MountainCompetition(int numMonth, int maxHours, int maxDays) {
        super(numMonth, maxHours, maxDays);
    }

    @Override
    void setPerformanceRanges() {
        performanceRanges.put(Range.KB, 0.0);
        performanceRanges.put(Range.GA, 0.2);
        performanceRanges.put(Range.EB, 0.2);
        performanceRanges.put(Range.SB, 0.3);
        performanceRanges.put(Range.K123, 0.1);
        performanceRanges.put(Range.K45, 0.2);
    }
}

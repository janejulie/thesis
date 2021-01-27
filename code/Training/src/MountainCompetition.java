public class MountainCompetition extends Macro {
    public MountainCompetition(int numMonth, int maxHours, int maxDays) {
        super(numMonth, maxHours, maxDays);
        performanceRanges.put(PerformanceRange.KB, 0.0);
        performanceRanges.put(PerformanceRange.GA, 0.2);
        performanceRanges.put(PerformanceRange.EB, 0.2);
        performanceRanges.put(PerformanceRange.SB, 0.3);
        performanceRanges.put(PerformanceRange.K123, 0.1);
        performanceRanges.put(PerformanceRange.K45, 0.2);
    }
}

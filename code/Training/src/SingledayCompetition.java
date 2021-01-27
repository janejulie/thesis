public class SingledayCompetition extends Macro {
    public SingledayCompetition(int numMonth, int maxHours, int maxDays) {
        super(numMonth, maxHours, maxDays);
        performanceRanges.put(PerformanceRange.KB, 0.15);
        performanceRanges.put(PerformanceRange.GA, 0.6);
        performanceRanges.put(PerformanceRange.EB, 0.1);
        performanceRanges.put(PerformanceRange.SB, 0.05);
        performanceRanges.put(PerformanceRange.K123, 0.05);
        performanceRanges.put(PerformanceRange.K45, 0.05);
    }
}

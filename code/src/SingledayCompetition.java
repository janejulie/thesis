import java.time.LocalDate;

public class SingledayCompetition extends Macro {
    public SingledayCompetition(int numMonth, int maxHours, int maxDays, LocalDate compDay) {
        super(numMonth, maxHours, maxDays, compDay);
    }

    @Override
    void setPerformanceRanges() {
        ranges.put(Range.KB, 0.05);
        ranges.put(Range.GA, 0.35);
        ranges.put(Range.EB, 0.25);
        ranges.put(Range.SB, 0.10);
        ranges.put(Range.K123, 0.10);
        ranges.put(Range.K45, 0.15);
    }
    public String getName(){
        return "Straßeneinzel";
    }
}

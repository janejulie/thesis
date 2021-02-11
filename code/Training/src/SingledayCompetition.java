import java.time.LocalDate;

public class SingledayCompetition extends Macro {
    public SingledayCompetition(int numMonth, int maxHours, int maxDays, LocalDate compDay) {
        super(numMonth, maxHours, maxDays, compDay);
    }

    @Override
    void setPerformanceRanges() {
        ranges.put(Range.KB, 0.05);
        ranges.put(Range.GA, 0.45);
        ranges.put(Range.EB, 0.15);
        ranges.put(Range.SB, 0.1);
        ranges.put(Range.K123, 0.1);
        ranges.put(Range.K45, 0.15);
    }
    public String getName(){
        return "Straßeneinzel";
    }
}

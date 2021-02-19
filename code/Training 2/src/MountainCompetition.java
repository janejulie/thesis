import java.time.LocalDate;

public class MountainCompetition extends Macro {
    public MountainCompetition(int numMonth, int maxHours, int maxDays, LocalDate compDay) {
        super(numMonth, maxHours, maxDays, compDay);
    }

    @Override
    void setPerformanceRanges() {
        ranges.put(Range.KB, 0.05);
        ranges.put(Range.GA, 0.2);
        ranges.put(Range.EB, 0.2);
        ranges.put(Range.SB, 0.25);
        ranges.put(Range.K123, 0.1);
        ranges.put(Range.K45, 0.2);
    }
    public String getName(){
        return "Bergfahrt";
    }
}

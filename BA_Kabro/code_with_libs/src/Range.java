public enum Range {
    KB(0),
    GA(1),
    EB(2),
    SB(3),
    K123(4),
    K45(5);

    private final int range;

    Range(int index) {
        this.range = index;
    }

    public int index() {
        return range;
    }
}

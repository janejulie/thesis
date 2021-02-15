public enum SessionName {
    Kompensationsfahrt(0),
    Extensive_Fahrt(1),
    Fettstoffwechselfahrt(2),
    Intensive_Fahrt(3),
    Extensive_Kraftfahrt(4),
    Einzelzeitfahrt(5),
    Extensives_Fahrtspiel(6),
    Intensives_Fahrtspiel(7),
    Intensive_Kraftausdauer(8),
    Schnelligkeitsausdauer(9),
    Sprinttraining(10),

    Pause(11);

    private final int name;

    private SessionName(int index) {
        this.name = index;
    }

    public int index() {
        return name;
    }
}

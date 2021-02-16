public enum Method {
    Dauerleistung(0),
    Intervall(1),
    Wiederholung(2),
    Pause(3),
    Fahrtspiel(4);

    private final int method;

    Method(int index) {
        this.method = index;
    }

    public int index() {
        return method;
    }
}

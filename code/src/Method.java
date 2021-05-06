public enum Method {
    Pause(0),
    Dauerleistung(1),
    Intervall(2),
    Wiederholung(3),
    Fahrtspiel(4);

    private final int method;

    Method(int index) {
        this.method = index;
    }

    public int index() {
        return method;
    }
}

public enum Method {
    DAUERLEISTUNG(0),
    INTERVALL(1),
    WIEDERHOLUNG(2),
    PAUSE(3),
    FAHRTSPIEL(4);

    private final int method;

    private Method(int index) {
        this.method = index;
    }

    public int index() {
        return method;
    }
}

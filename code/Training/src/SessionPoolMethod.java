public enum SessionPoolMethod {
    Pause                       (0, Method.Pause, new int[][]{{0, 0}, {0, 0},   {0, 0}, {0, 0}, {0, 0}, {0, 0}}), // everything 0
    Kompensationsfahrt          (1, Method.Dauerleistung, new int[][]{{15, 180}, {0, 0},  {0, 0}, {0, 0}, {0, 0}, {0, 0}}),
    Extensive_Fahrt             (2, Method.Dauerleistung, new int[][]{{0, 0}, {30, 360},  {0, 0}, {0, 0}, {0, 0}, {0, 0}}),
    Fettstoffwechselfahrt       (3, Method.Dauerleistung, new int[][]{{0, 0}, {60, 360}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}),
    Intensive_Fahrt             (4, Method.Dauerleistung, new int[][]{{0, 0}, {30, 60},   {15, 60}, {0, 0}, {0, 0}, {0, 0}}),
    Extensive_Kraftausdauerfahrt(5, Method.Dauerleistung, new int[][]{{0, 0}, {30, 60},   {0, 0}, {0, 0}, {15, 150}, {0, 0}}),
    Einzelzeitfahrt             (6, Method.Dauerleistung, new int[][]{{0, 0}, {30, 60},   {0, 0}, {15, 60}, {0, 0}, {0, 0}}),
    Extensives_Fahrtspiel       (7, Method.Fahrtspiel, new int[][]{{0, 0}, {15, 240},   {15, 240}, {0, 0}, {0, 0}, {0, 0}}),
    Freies_Fahrtspiel           (8, Method.Fahrtspiel, new int[][]{{0, 180}, {5, 180},   {15, 180}, {15, 180}, {0, 180}, {0, 180}}),
    Intensive_Kraftausdauer     (9, Method.Intervall, new int[][]{{0, 0}, {30, 90},   {0, 0}, {0, 0}, {0, 0}, {15, 120}}),
    Schnelligkeitsausdauer      (10, Method.Intervall, new int[][]{{0, 0}, {60, 180},  {0, 0}, {15, 45}, {0, 0}, {0, 0}}),
    Sprinttraining              (11, Method.Wiederholung, new int[][]{{0, 0}, {15, 30},   {0, 0}, {15, 60}, {0, 0}, {0, 0}});

    private final int number;
    private final int method;
    private final int[][] domains;

    SessionPoolMethod(int index, Method method, int[][] domains) {
        this.number = index;
        this.method = method.index();
        this.domains = domains;
    }

    public int index() {
        return number;
    }

    public int getMethod() {
        return method;
    }

    public int[][] getDomains() {
        return domains;
    }
}

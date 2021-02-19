public enum SessionPool {
    Pause                       (0, new int[][]{{0, 0}, {0, 0},   {0, 0}, {0, 0}, {0, 0}, {0, 0}}), // everything 0
    Kompensationsfahrt          (1, new int[][]{{15, 180}, {0, 0},  {0, 0}, {0, 0}, {0, 0}, {0, 0}}),
    Extensive_Fahrt             (2, new int[][]{{0, 0}, {30, 240},  {0, 0}, {0, 0}, {0, 0}, {0, 0}}),
    Fettstoffwechselfahrt       (3, new int[][]{{0, 0}, {60, 360}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}),
    Intensive_Fahrt             (4, new int[][]{{0, 0}, {30, 60},   {15, 60}, {0, 0}, {0, 0}, {0, 0}}),
    Extensive_Kraftausdauerfahrt(5, new int[][]{{0, 0}, {30, 60},   {0, 0}, {0, 0}, {15, 150}, {0, 0}}),
    Einzelzeitfahrt             (6, new int[][]{{0, 0}, {30, 60},   {0, 0}, {15, 60}, {0, 0}, {0, 0}}),
    Extensives_Fahrtspiel       (7, new int[][]{{0, 0}, {15, 240},   {15, 240}, {0, 0}, {0, 0}, {0, 0}}),
    Freies_Fahrtspiel           (8, new int[][]{{0, 120}, {5, 120},   {15, 120}, {15, 120}, {0, 120}, {0, 120}}),
    Intensive_Kraftausdauer     (9, new int[][]{{0, 0}, {30, 90},   {0, 0}, {0, 0}, {0, 0}, {15, 120}}),
    Schnelligkeitsausdauer      (10, new int[][]{{0, 0}, {60, 180},  {0, 0}, {15, 45}, {0, 0}, {0, 0}}),
    Sprinttraining              (11, new int[][]{{0, 0}, {15, 30},   {0, 0}, {15, 60}, {0, 0}, {0, 0}});

    private final int number;
    private final int[][] domains;

    SessionPool(int index, int[][] domains) {
        this.number = index;
        this.domains = domains;
    }

    public static SessionPool[] getWH(){
        return new SessionPool[]{Sprinttraining};
    }
    public static SessionPool[] getIV(){
        return new SessionPool[]{Intensive_Kraftausdauer, Schnelligkeitsausdauer};
    }
    public static SessionPool[] getDL(){
        return new SessionPool[]{Kompensationsfahrt, Extensive_Fahrt, Fettstoffwechselfahrt, Intensive_Fahrt, Extensive_Kraftausdauerfahrt, Einzelzeitfahrt};
    }
    public static SessionPool[] getFS(){
        return new SessionPool[]{Extensives_Fahrtspiel, Freies_Fahrtspiel};
    }
    public static SessionPool[] getPause(){
        return new SessionPool[]{Pause};
    }

    public int index() {
        return number;
    }

    public int[][] getDomains() {
        return domains;
    }
}

import java.util.HashMap;

public class Session {
    protected int minutes;
    protected Methods method;
    protected HashMap<PerformanceRange, Integer> distribution;

    public Session(int min, Methods method, HashMap<PerformanceRange, Integer> distribution) {
        this.minutes = min;
        this.method = method;
        this.distribution = distribution;
    }

    public int getMinutes() {
        return minutes;
    }

    public Methods getMethod() {
        return method;
    }

    public HashMap<PerformanceRange, Integer> getDistribution() {
        return distribution;
    }

    @Override
    public String toString() {
        return "Session{" +
                "minutes=" + minutes +
                ", method='" + method + '\'' +
                ", distribution=" + distribution +
                '}';
    }
}

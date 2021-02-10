import java.util.HashMap;

public class Session {
    protected int minutes;
    protected Method method;
    protected HashMap<Range, Integer> distribution;

    public Session(int min, Method method, HashMap<Range, Integer> distribution) {
        this.minutes = min;
        this.method = method;
        this.distribution = distribution;
    }

    public int getMinutes() {
        return minutes;
    }

    public Method getMethod() {
        return method;
    }

    public HashMap<Range, Integer> getDistribution() {
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

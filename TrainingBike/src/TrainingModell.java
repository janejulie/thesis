import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.util.stream.IntStream;

public class TrainingModell {
    protected int numDays, numWeeks, numMonths;
    protected int[] timesteps, maxMinutesPerWeek;
    protected int maxDaysWeekly, maxMinutesWeekly;
    protected int percKB, percGA, percEB, percSB;

    public TrainingModell() {
        // CONSTANTS (not changing from Solver)
        this.numMonths = 3;
        this.numWeeks = numMonths*4;
        this.numDays = numWeeks*7;
        this.maxMinutesWeekly = 10*60;
        this.maxDaysWeekly = 4;
        this.timesteps = new int[]{60, 120};
        this.percKB = 15;
        this.percGA = 60;
        this.percEB = 15;
        this.percSB = 10;
        this.maxMinutesPerWeek = getMaxMinutesAllWeeks();
    }

    public int[] getMaxMinutesAllWeeks() {
        int[] hours = new int[numWeeks];
        for (int i = 0; i < numWeeks; i++) {
            hours[i] = getPeriodization(numMonths)[i] * maxMinutesWeekly;
        }
        return hours;
    }

    public int[] getPeriodization(int numMonths) {
        int steps = 10;
        int minIntensity = 100 - steps * (numMonths - 1);
        int[] intensityMakro = IntStream.rangeClosed(minIntensity / steps, 100 / steps).map(i -> i * steps).toArray();
        int[] intensityMeso = {80, 90, 100, 40};

        int[] intensity = new int[numMonths * intensityMeso.length];
        int week;
        for (int i = 0; i < numMonths; i++) {
            for (int j = 0; j < intensityMeso.length; j++) {
                if (j == 3) {
                    week = intensityMeso[3];
                } else {
                    week = intensityMeso[j] * intensityMakro[i] / 100;
                }
                intensity[i*intensityMeso.length + j] = week;
            }
        }
        return intensity;
    }

    public void modelling() {
        Model model = new Model("training");
        // VARIABLES
        // CONSTANTS see class constructor
        IntVar[] minutes = model.intVarArray("minutes", numDays, 30, 120); // on change modifies ranges
        IntVar[] method = model.intVarArray("method", numDays, 0, 3); // on change modifies ranges
        IntVar[][] ranges = model.intVarMatrix(numDays, 3, 0, 120);


        // CONSTRAINTS ON WEEK
        for (int week = 0; week < numWeeks; week++) {
            // dont train more than x days in a week
/*            IntVar trainingdays = ;
            model.count(trainingdays, "<=", maxDaysWeekly);*/


            // time limit on week trainings
            IntVar weeklyMinutes = minutes[week]
                    .add(minutes[week+1], minutes[week+2], minutes[week+3], minutes[week+4], minutes[week+5], minutes[week+6])
                    .intVar();
            model.arithm(weeklyMinutes, "<=", maxMinutesPerWeek[week]).post();        }

        // CONSTRAINTS ON DAYS
        for (int day = 0; day < numDays; day++){
        }

        // SOLVING
        Solver solver = model.getSolver();
        Solution plan = solver.findSolution();
        System.out.println(model);
    }

}

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

// can not create Macro in JUnit directly,
// because test doesnt await threads from parallel solving

class MainTest {
    LocalDate compDate = LocalDate.now().plusMonths(6);
    Main main = new Main();

    @BeforeEach
    void setUp() {
    }

    // toggle comment on AfterEach if pdf should not be generated
    @AfterEach
    void tearDown() {
        main.getPlan().getMesos().forEach(i -> {
                String msg = "";
                int distance = i.getDistance();
                int summe = Arrays.stream(i.getTargetWeek()).sum();
                msg += "Distance: " + distance + "\t";
                msg += "Summe: "    + summe + "\t";
                msg += "Abweichung " +  (double) distance*100/summe + "%";
                System.out.println(msg);
            }
        );
        main.createPDF();
    }

    @Test
    void planAmateurSingleday() throws Exception {
        main.createPlan(3, "Straßeneinzel", 12, 6, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planSingleday() throws Exception {
        main.createPlan(3, "Straßeneinzel", 8, 4, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planHobbySingleday() throws Exception {
        main.createPlan(3, "Straßeneinzel", 4, 2, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planAmateurMountain() throws Exception {
        main.createPlan(3, "Bergfahrt", 12, 6, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planMountain() throws Exception {
        main.createPlan(3, "Bergfahrt", 8, 4, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planHobbyMountain() throws Exception {
        main.createPlan(3, "Bergfahrt", 4, 2, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planAmateurTimetrial() throws Exception {
        main.createPlan(3, "Rundstrecke", 12, 6, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planTimetrial() throws Exception {
        main.createPlan(3, "Rundstrecke", 8, 4, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planHobbyTimetrial() throws Exception {
        main.createPlan(3, "Rundstrecke", 4, 2, compDate);
        assertNotNull(main.plan);
    }
}
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// can not create Macro in JUnit directly, because test doesnt await threads from parallel solving
class MainTest {
    LocalDate compDate = LocalDate.now().plusMonths(6);
    Main main = new Main();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void planAmateurSingleday() throws Exception {
        main.createPlan(3, "Straßeneinzel", 15, 5, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planHobbySingleday() throws Exception {
        main.createPlan(3, "Straßeneinzel", 4, 2, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planAmateurMountain() throws Exception {
        main.createPlan(3, "Bergfahrt", 15, 5, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planHobbyMountain() throws Exception {
        main.createPlan(3, "Bergfahrt", 4, 2, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planAmateurTimetrial() throws Exception {
        main.createPlan(3, "Rundstecke", 15, 5, compDate);
        assertNotNull(main.plan);
    }
    @Test
    void planHobbyTimetrial() throws Exception {
        main.createPlan(3, "Rundstecke", 4, 2, compDate);
        assertNotNull(main.plan);
    }

    @Test
    void createPDF() throws Exception {
        main.createPlan(3, "Straßeneinzel", 10, 4, compDate);
        main.getPlan().getMesos().forEach(e -> System.out.println(e.toString()));
        main.createPDF();
    }

}
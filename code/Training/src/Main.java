import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.IntStream;

public class Main {
    JFrame frame;
    JPanel mainPanel;
    JTable table;
    JButton bCalculate;

    public Main() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // define possible inputs
        Integer[] months = new Integer[]{3, 4, 5};
        Integer[] weeklyDays = new Integer[]{1, 2, 3, 4, 5, 6};
        Integer[] weeklyHours = IntStream.rangeClosed(5, 20).boxed().toArray(Integer[] ::new);
        String[] competition = new String[]{"Straßeneinzel", "Rundstecke", "Bergfahrt"};
        String competitionDate = new SimpleDateFormat("dd.MM.yy").format(new Date());

        // GUI elements
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        JComboBox<Integer> iMonths = new JComboBox<Integer>(months);
        JComboBox<Integer> iWeeklyDays = new JComboBox<Integer>(weeklyDays);
        iWeeklyDays.setSelectedItem(3);
        JComboBox<Integer> iWeeklyHours = new JComboBox<Integer>(weeklyHours);
        iWeeklyHours.setSelectedItem(10);
        JComboBox<String> iCompetition = new JComboBox<String>(competition);
        JFormattedTextField iCompetitionDate = new JFormattedTextField(competitionDate);
        iCompetitionDate.setColumns(8);

        bCalculate = new JButton("Erstelle Trainingsplan");

        bCalculate.addActionListener( e ->
                {
                    try {
                        bCalculatePressed(
                                (Integer) iMonths.getSelectedItem(),
                                (String) iCompetition.getSelectedItem(),
                                (Integer) iWeeklyHours.getSelectedItem(),
                                (Integer) iWeeklyDays.getSelectedIndex(),
                                iCompetitionDate.getValue().toString()
                        );
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
        );
        inputPanel.add(new Label("Dauer in Monaten"));
        inputPanel.add(iMonths);
        inputPanel.add(new Label("Maximale wöchentliche Trainingstage"));
        inputPanel.add(iWeeklyDays);
        inputPanel.add(new Label("Maximale wöchentliche Trainingsstunden"));
        inputPanel.add(iWeeklyHours);
        inputPanel.add(new Label("Art des Wettkampfs"));
        inputPanel.add(iCompetition);
        inputPanel.add(new Label("Wettkampfstag"));
        inputPanel.add(iCompetitionDate);

        JPanel output = new JPanel();
        table = new JTable(new DefaultTableModel());
        output.setLayout(new BorderLayout());
        output.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(inputPanel);
        mainPanel.add(bCalculate);
        mainPanel.add(output);
        frame.getRootPane().setDefaultButton(bCalculate);
        frame.setContentPane(mainPanel);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    private void bCalculatePressed(int month, String comp, int weeklyHours, int weeklyDays, String compDate) throws Exception {
        Macro plan;
        int iMaxWeeklyHours = weeklyHours * 60;
        if (comp == "Straßeneinzel") {
            plan = new SingledayCompetition(month, iMaxWeeklyHours, weeklyDays);
        } else if (comp == "Rundstecke") {
            plan = new TimetrialCompetition(month, iMaxWeeklyHours, weeklyDays);
        } else if (comp == "Bergfahrt") {
            plan = new MountainCompetition(month, iMaxWeeklyHours, weeklyDays);
        } else {
            throw new Exception();
        }
        Object[] header = {"Minuten", "Trainingsmethode", "KB", "GA", "EB", "SB", "K123", "K45"};
        ((DefaultTableModel) table.getModel()).setDataVector(plan.getListOfSessions(), header);
    }

    public static void main(String[] args) {
        Main m = new Main();
        m.bCalculate.doClick();
    }
}

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.IntStream;

public class Main {
    JFrame frame;
    JPanel mainPanel;
    TrainingTable table;
    JButton bCalculate;
    JLabel targets;
    Macro plan;

    public Main() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // GUI Input elements
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        JComboBox<Integer> iMonths = new JComboBox<Integer>(new Integer[]{3, 4, 5});
        JComboBox<Integer> iWeeklyDays = new JComboBox<Integer>(new Integer[]{1, 2, 3, 4, 5, 6});
        iWeeklyDays.setSelectedItem(3);
        JComboBox<Integer> iWeeklyHours = new JComboBox<Integer>(IntStream.rangeClosed(5, 20).boxed().toArray(Integer[]::new));
        iWeeklyHours.setSelectedItem(10);
        JComboBox<String> iCompetition = new JComboBox<String>(new String[]{"Straßeneinzel", "Rundstecke", "Bergfahrt"});
        JFormattedTextField iCompetitionDate = new JFormattedTextField(new SimpleDateFormat("dd.MM.yy").format(new Date()));
        iCompetitionDate.setColumns(8);

        bCalculate = new JButton("Erstelle Trainingsplan");
        bCalculate.addActionListener(e ->
                {
                    try {
                        bCalculatePressed(
                                (Integer) iMonths.getSelectedItem(),
                                (String) iCompetition.getSelectedItem(),
                                (Integer) iWeeklyHours.getSelectedItem(),
                                (Integer) iWeeklyDays.getSelectedItem()
                                //iCompetitionDate.getValue().toString()
                        );
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
        );

        inputPanel.add(new Label("Dauer in Monaten"));
        inputPanel.add(iMonths);
        inputPanel.add(new Label("max. wöchentliche Trainingstage"));
        inputPanel.add(iWeeklyDays);
        inputPanel.add(new Label("max. wöchentliche Trainingsstunden"));
        inputPanel.add(iWeeklyHours);
        inputPanel.add(new Label("Art des Wettkampfs"));
        inputPanel.add(iCompetition);
        inputPanel.add(new Label("Wettkampfstag"));
        inputPanel.add(iCompetitionDate);

        targets = new JLabel();
        JLabel monitor = new JLabel();
        JPanel wrap = new JPanel();
        wrap.add(inputPanel);
        wrap.add(targets);
        wrap.add(monitor);

        JLabel lSelected = new JLabel("");
        JPanel output = new JPanel();
        table = new TrainingTable(monitor);

        output.setLayout(new BorderLayout());
        output.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(wrap);
        mainPanel.add(bCalculate);
        mainPanel.add(lSelected);
        mainPanel.add(output);
        frame.getRootPane().setDefaultButton(bCalculate);
        frame.setContentPane(mainPanel);
        frame.setSize(new Dimension(1500, 800));
        frame.setVisible(true);
    }

    private void bCalculatePressed(int month, String comp, int weeklyHours, int weeklyDays) throws Exception {
        table.getSelectionModel().clearSelection();
        targets.setText("loading");
        int iMaxWeeklyHours = weeklyHours * 60;
        try{
            switch (comp) {
                case "Straßeneinzel" -> plan = new SingledayCompetition(month, iMaxWeeklyHours, weeklyDays);
                case "Rundstecke" -> plan = new TimetrialCompetition(month, iMaxWeeklyHours, weeklyDays);
                case "Bergfahrt" -> plan = new MountainCompetition(month, iMaxWeeklyHours, weeklyDays);
                default -> throw new Exception("Wettkampfsart unbekannt");
            }
            targets.setText(plan.toString());
            plan.solvePlan();
            table.createTableContent(plan.getSessions());
        } catch (NullPointerException e){
            targets.setText("");
            table.setMonitor("No solution");
        }
    }

    public static void main(String[] args) {
        Main m = new Main();
        m.bCalculate.doClick();
    }
}

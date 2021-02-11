import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

public class Main {
    JFrame frame;
    JPanel mainPanel;
    TrainingTable table;
    JLabel monitor;
    Macro plan;

    public Main() {
        frame = new JFrame("Trainingsplanerstellung");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // GUI Input elements
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        JComboBox<Integer> iMonths = new JComboBox<Integer>(new Integer[]{3, 4, 5});
        JComboBox<Integer> iWeeklyDays = new JComboBox<Integer>(new Integer[]{1, 2, 3, 4, 5, 6});
        iWeeklyDays.setSelectedItem(3);
        JComboBox<Integer> iWeeklyHours = new JComboBox<Integer>(IntStream.rangeClosed(5, 20).boxed().toArray(Integer[]::new));
        iWeeklyHours.setSelectedItem(6);
        JComboBox<String> iCompetition = new JComboBox<String>(new String[]{"Straßeneinzel", "Rundstecke", "Bergfahrt"});
        JFormattedTextField iCompetitionDate = new JFormattedTextField(LocalDate.now().plusMonths(6));
        iCompetitionDate.setColumns(8);
        JButton bCreatePDF = new JButton("PDF erstellen");
        bCreatePDF.setVisible(false);
        bCreatePDF.addActionListener(e -> {
            try {
                createPDF();
            } catch (IOException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        });

        JButton bCalculate = new JButton("Erstelle Trainingsplan");
        bCalculate.addActionListener(e ->
                {
                    table.getSelectionModel().clearSelection();
                    monitor.setText("loading");
                    try {
                        createPlan(
                                (Integer) iMonths.getSelectedItem(),
                                (String) iCompetition.getSelectedItem(),
                                (Integer) iWeeklyHours.getSelectedItem(),
                                (Integer) iWeeklyDays.getSelectedItem(),
                                (LocalDate) iCompetitionDate.getValue()
                        );
                        updateMonitor();
                        bCreatePDF.setVisible(true);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "No Solution found");
                        monitor.setText(plan.getRanges().toString());
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

        monitor = new JLabel();
        JLabel monitor = new JLabel();
        JPanel wrap = new JPanel();
        wrap.add(inputPanel);
        wrap.add(monitor);

        JLabel lSelected = new JLabel("");
        JPanel output = new JPanel();
        table = new TrainingTable();
        table.getSelectionModel().addListSelectionListener(event -> {
            String targets = plan.getTargets();
            monitor.setText("<html>"+ targets+"<br>" + table.monitorStats() + "<br><html>");
         }
        );

        output.setLayout(new BorderLayout());
        output.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(wrap);
        mainPanel.add(bCalculate);
        mainPanel.add(lSelected);
        mainPanel.add(output);
        mainPanel.add(bCreatePDF);

        frame.getRootPane().setDefaultButton(bCalculate);
        frame.setContentPane(mainPanel);
        frame.setSize(new Dimension(1500, 800));
        frame.setVisible(true);
    }

    public void updateMonitor(){
        String msg;
        msg = plan.getRanges().toString();
        monitor.setText(msg);
    }

    private void createPDF() throws IOException {
        String destination = "pdf/trainingsplan" + LocalDateTime.now() + ".pdf";
        File file = new File(destination);
        file.getParentFile().mkdirs();
        PdfWriter writer = new PdfWriter(destination);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        Table pdfTable = new Table(UnitValue.createPercentArray(9)).useAllAvailableWidth();

        for (int i = 0; i < table.getColumnCount(); i++) {
            pdfTable.addCell(table.getColumnName(i));
        }

        for (int rows = 0; rows < table.getRowCount() - 1; rows++) {
            for (int cols = 0; cols < table.getColumnCount(); cols++) {
                pdfTable.setFontSize(8).addCell(table.getModel().getValueAt(rows, cols).toString());

            }
        }

        String description =
                plan.getName() + " am " + plan.getCompDay().toString() + "\n" +
                "wöchentliche Stunden " + plan.getMaxTrainingMinutes()/60 +"\n" +
                "wöchentliche Tage " + plan.getMaxTrainingDays() +"\n" +
                "Anteile der Leistungsbereiche " + plan.getRanges().toString() +"\n";

        for (int month = 0; month < plan.getNumMonths(); month++){
            description += "Distanz " + (month+1) + ". Monat "+ plan.getMesos().get(month).toString() + "\n";
        }

        document.add(new Paragraph(description));
        document.add(pdfTable);
        document.close();

        File myFile = new File(destination);
        Desktop.getDesktop().open(myFile);
    }

    private void createPlan(int month, String comp, int weeklyHours, int weeklyDays, LocalDate compDay) throws Exception {
        int iMaxWeeklyHours = weeklyHours * 60;
        switch (comp) {
            case "Straßeneinzel" -> plan = new SingledayCompetition(month, iMaxWeeklyHours, weeklyDays, compDay);
            case "Rundstecke" -> plan = new TimetrialCompetition(month, iMaxWeeklyHours, weeklyDays, compDay);
            case "Bergfahrt" -> plan = new MountainCompetition(month, iMaxWeeklyHours, weeklyDays, compDay);
            default -> throw new Exception("Wettkampfsart unbekannt");
        }
        plan.solvePlan();
        if (plan != null){
            monitor.setText(plan.toString());
            table.createTableContent(plan.getSessions());
        } else {
            throw new Exception("No Solution found");
        }
    }

    public static void main(String[] args) {
        Main m = new Main();
    }
}

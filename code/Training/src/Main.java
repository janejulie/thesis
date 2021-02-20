import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.stream.Collectors;
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
        JComboBox<Integer> iMonths = new JComboBox<>(new Integer[]{3, 4, 5});
        JComboBox<Integer> iWeeklyDays = new JComboBox<>(new Integer[]{2, 3, 4, 5, 6});
        iWeeklyDays.setSelectedItem(4);
        JComboBox<Integer> iWeeklyHours = new JComboBox<>(IntStream.rangeClosed(4, 12).boxed().toArray(Integer[]::new));
        iWeeklyHours.setSelectedItem(8);
        JComboBox<String> iCompetition = new JComboBox<>(new String[]{"Straßeneinzel", "Rundstrecke", "Bergfahrt"});
        JFormattedTextField iCompetitionDate = new JFormattedTextField(LocalDate.now().plusMonths(6));
        iCompetitionDate.setColumns(8);
        JButton bCreatePDF = new JButton("PDF erstellen");
        bCreatePDF.setVisible(false);
        bCreatePDF.addActionListener(e -> createPDF());

        JButton bCalculate = new JButton("Erstelle Trainingsplan");
        bCalculate.addActionListener(e -> calculateButtonPressed(iMonths, iWeeklyDays, iWeeklyHours, iCompetition, iCompetitionDate, bCreatePDF));

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
        table.getSelectionModel().addListSelectionListener(event -> monitorUpdate(monitor));

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

    private void calculateButtonPressed(JComboBox<Integer> iMonths, JComboBox<Integer> iWeeklyDays, JComboBox<Integer> iWeeklyHours, JComboBox<String> iCompetition, JFormattedTextField iCompetitionDate, JButton bCreatePDF) {
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
            bCreatePDF.setVisible(true);
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Keine Lösung in der Zeit gefunden");
            monitor.setText(plan.getRanges().toString());
        }
    }

    private void monitorUpdate(JLabel monitor) {
        String targets = "<html>";
        targets += getMonitorString().replace("\n", "<br>");
        targets += "Auswahl -------";
        targets += table.monitorStats();
        targets += "</html>";
        monitor.setText(targets);
    }

    public void createPDF() {
        String destination = "pdf/"+ plan.getClass().toString().split(" ")[1] + "_" +
                plan.getMaxTrainingDays() + "d_" +
                plan.getMaxTrainingMinutes()/60 + "h_" +
                ".pdf";
        File file = new File(destination);
        file.getParentFile().mkdirs();
        PdfWriter writer = null;
        try {
            writer = new PdfWriter(destination);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        int numColumns = table.getColumnCount();
        int numRows = table.getRowCount() - 1;
        Table pdfTable = new Table(UnitValue.createPercentArray(numColumns)).useAllAvailableWidth();

        for (int i = 0; i < numColumns; i++) {
            pdfTable.addCell(table.getColumnName(i));
        }

        for (int rows = 0; rows < numRows; rows++) {
            for (int cols = 0; cols < numColumns; cols++) {
                pdfTable.setFontSize(8).addCell(table.getModel().getValueAt(rows, cols).toString());
            }
        }

        String description = getMonitorString();
        document.add(new Paragraph(description));
        document.add(pdfTable);
        document.close();

        File myFile = new File(destination);
        try {
            Desktop.getDesktop().open(myFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String arrayToString(int[] array){
        return IntStream.of(array)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", "));
    }

    private String getMonitorString() {
        String description =
                plan.getName() + " am " + plan.getCompDay().toString() + "\n" +
                "wöchentliche Stunden " + plan.getMaxTrainingMinutes()/60 +"\n" +
                "wöchentliche Tage " + plan.getMaxTrainingDays() + "\n";

        for (int month = 0; month < plan.getNumMonths(); month++){
            Meso meso = plan.getMesos().get(month);
            description += (month+1) + ". Monat -------" + "\n";
            description += "Wochenziele " + arrayToString(meso.getTargetWeek()) + "\n";
            description += "Leistungsbereiche " + arrayToString(meso.getTargetRanges()) + "\n";
        }
        return description;
    }

    void createPlan(int month, String comp, int weeklyHours, int weeklyDays, LocalDate compDay) throws Exception {
        int iMaxWeeklyMinutes = weeklyHours * 60;
        switch (comp) {
            case "Straßeneinzel" -> plan = new SingledayCompetition(month, iMaxWeeklyMinutes, weeklyDays, compDay);
            case "Rundstrecke" -> plan = new TimetrialCompetition(month, iMaxWeeklyMinutes, weeklyDays, compDay);
            case "Bergfahrt" -> plan = new MountainCompetition(month, iMaxWeeklyMinutes, weeklyDays, compDay);
            default -> throw new Exception("Wettkampfsart unbekannt");
        }
        if (plan != null){
            table.createTableContent(plan.getSessions());
            table.setRowSelectionInterval(0, 0);
        } else {
            throw new Exception("No Solution found");
        }
    }

    public Macro getPlan() {
        return plan;
    }

    public static void main(String[] args) {
        new Main();
    }
}

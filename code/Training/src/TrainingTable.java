import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class TrainingTable extends JTable {
    DefaultTableCellRenderer renderer;
    JLabel monitor;
    HashMap<String, Integer> sums;
    public TrainingTable(JLabel monitor) {
        super();
        this.renderer = new TrainingCellRenderer();
        this.monitor = monitor;
        sums = new HashMap<>();
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                sums = new HashMap<>();
                int[] rows = getSelectedRows();
                for (int rowNum : rows) {
                    for (int colNum = 0; colNum < getColumnCount(); colNum++) {
                        String colName = getColumnName(colNum);
                        if (colName != "Trainingsmethode") {
                            int newVal = (int) getValueAt(rowNum, colNum) + sums.getOrDefault(colName, 0);
                            sums.put(colName, newVal);
                        }
                    }
                }
                monitorStats();
            }
        });
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return renderer;
    }

    public void createTableContent(ArrayList<Session> sessions){
        Object[][] tableContent = new Object[sessions.size()][8];
        for (int i = 0; i<sessions.size(); i++){
            for (int ii = 0; ii < 8; ii++){
                tableContent[i][0] = sessions.get(i).getMinutes();
                tableContent[i][1] = sessions.get(i).getMethod();
                tableContent[i][2] = sessions.get(i).getDistribution().get(PerformanceRange.KB);
                tableContent[i][3] = sessions.get(i).getDistribution().get(PerformanceRange.GA);
                tableContent[i][4] = sessions.get(i).getDistribution().get(PerformanceRange.EB);
                tableContent[i][5] = sessions.get(i).getDistribution().get(PerformanceRange.SB);
                tableContent[i][6] = sessions.get(i).getDistribution().get(PerformanceRange.K123);
                tableContent[i][7] = sessions.get(i).getDistribution().get(PerformanceRange.K45);
            }
        }
        Object[] header = {"Minuten", "Trainingsmethode", "KB", "GA", "EB", "SB", "K123", "K45"};
        ((DefaultTableModel) getModel()).setDataVector(tableContent, header);
    }

    public void setMonitor(String text) {
        monitor.setText(text);
    }
    public void monitorStats(){
        String stats = "<html>Auswahl";
        stats += "<br>" + "min: " + sums.getOrDefault(getColumnName(0), 0).toString();
        stats += "<br>" + "KB: " + sums.getOrDefault(getColumnName(2), 0).toString();
        stats += "<br>" + "GA: " + sums.getOrDefault(getColumnName(3), 0).toString();
        stats += "<br>" + "EB: " + sums.getOrDefault(getColumnName(4), 0).toString();
        stats += "<br>" + "SB: " + sums.getOrDefault(getColumnName(5), 0).toString();
        stats += "<br>" + "K123: " + sums.getOrDefault(getColumnName(6), 0).toString();
        stats += "<br>" + "K45: " + sums.getOrDefault(getColumnName(7), 0).toString();
        stats += "</html>";
        monitor.setText(stats);
    }
}

class TrainingCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

            Component rendererComp = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (!isSelected) {
                if (row % 14 > 6){
                    setBackground(table.getBackground().brighter());
                    setForeground(table.getForeground());
                }else {
                    setBackground(table.getBackground().darker());
                    setForeground(table.getForeground());
                }
            }
            return rendererComp;
        }
    }
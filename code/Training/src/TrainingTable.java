import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class TrainingTable extends JTable {
    DefaultTableCellRenderer renderer;
    HashMap<String, Integer> sums;
    public TrainingTable() {
        super();
        this.renderer = new TrainingCellRenderer();
        sums = new HashMap<>();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return renderer;
    }

    public void createTableContent(ArrayList<Session> sessions){
        Object[][] tableContent = new Object[sessions.size()][9];
        for (int i = 0; i<sessions.size(); i++){
                tableContent[i][0] = sessions.get(i).getDay();
                tableContent[i][1] = sessions.get(i).getMinutes();
                tableContent[i][2] = sessions.get(i).getMethod();
                tableContent[i][3] = sessions.get(i).getDistribution().get(Range.KB);
                tableContent[i][4] = sessions.get(i).getDistribution().get(Range.GA);
                tableContent[i][5] = sessions.get(i).getDistribution().get(Range.EB);
                tableContent[i][6] = sessions.get(i).getDistribution().get(Range.SB);
                tableContent[i][7] = sessions.get(i).getDistribution().get(Range.K123);
                tableContent[i][8] = sessions.get(i).getDistribution().get(Range.K45);
        }
        Object[] header = {"Datum", "Minuten", "Trainingsmethode", "KB", "GA", "EB", "SB", "K123", "K45"};
        ((DefaultTableModel) getModel()).setDataVector(tableContent, header);
    }


    public String monitorStats(){
        sums = new HashMap<>();
        int[] rows = getSelectedRows();
        for (int rowNum : rows) {
            for (int colNum = 3; colNum < getColumnCount(); colNum++) {
                String colName = getColumnName(colNum);
                int newVal = (int) getValueAt(rowNum, colNum) + sums.getOrDefault(colName, 0);
                sums.put(colName, newVal);
            }
        }
        String stats = "Auswahl:";
        stats += " KB: " + sums.getOrDefault(getColumnName(3), 0).toString();
        stats += " GA: " + sums.getOrDefault(getColumnName(4), 0).toString();
        stats += " EB: " + sums.getOrDefault(getColumnName(5), 0).toString();
        stats += " SB: " + sums.getOrDefault(getColumnName(6), 0).toString();
        stats += " K123: " + sums.getOrDefault(getColumnName(7), 0).toString();
        stats += " K45: " + sums.getOrDefault(getColumnName(8), 0).toString();
        return stats;
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
                }else {
                    setBackground(table.getBackground().darker());
                }
                setForeground(table.getForeground());
            }
            return rendererComp;
        }
    }
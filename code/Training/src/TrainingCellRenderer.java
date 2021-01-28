import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TrainingCellRenderer extends DefaultTableCellRenderer {
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
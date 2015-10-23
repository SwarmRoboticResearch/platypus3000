package platypus3000.analyticstools;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * The JTable you see in the GUI for selecting colors and activating overlays.
 * You won't need to touch it ever (and you don't want to).
 */
public class SettingsTable extends JTable {

    OverlayTypeTableModel tableModel;
    OverlayManager overlayManager;
    public SettingsTable(OverlayManager overlayManager) {
        super();
        this.overlayManager = overlayManager;
        tableModel  = new OverlayTypeTableModel(overlayManager);
        overlayManager.newOverlayTypeListener = tableModel;
        setModel(tableModel);
        setDefaultEditor(HashMap.class, new ColorMapEditor());
        setDefaultRenderer(HashMap.class, new ColorMapRenderer());
        setRowHeight(40);
        getColumnModel().getColumn(3).setPreferredWidth(200);
        setPreferredScrollableViewportSize(new Dimension(600, (int) getPreferredScrollableViewportSize().getHeight()));
    }

    class ColormapPanel extends JPanel {
        ColormapPanel(HashMap<String, DynamicColor> map, ActionListener actionListener) {
            for(Map.Entry<String, DynamicColor> color : map.entrySet()) {
                JButton colorButton = new JButton(color.getKey());
                colorButton.setBackground(new Color(color.getValue().getColor()));
                colorButton.setOpaque(true);
                colorButton.setActionCommand(color.getKey());
                colorButton.setFont(colorButton.getFont().deriveFont(9f));
                colorButton.setPreferredSize(new Dimension((int) colorButton.getPreferredSize().getWidth(), 20));
                colorButton.setMargin(new Insets(0, 0, 0, 0));
                if(actionListener != null) colorButton.addActionListener(actionListener);
                add(colorButton);
            }
        }
    }

    class ColorMapRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return new ColormapPanel((HashMap<String,DynamicColor>) value, null);
        }
    }

    class ColorMapEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

        HashMap<String, DynamicColor> currentColors;

        public ColorMapEditor() {

        }

        public void actionPerformed(ActionEvent e) {
            String colorName = e.getActionCommand();
            if (currentColors.containsKey(colorName)) {
                DynamicColor selectedColor = currentColors.get(colorName);
                Color c = JColorChooser.showDialog(null, "Pick a color for " + colorName, new Color(selectedColor.getColor()));
                if(c != null)
                    selectedColor.setColor(c.getRGB());
                fireEditingStopped(); //Make the renderer reappear.
            }
        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        public Object getCellEditorValue() {
            return currentColors;
        }

        //Implement the one method defined by TableCellEditor.
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentColors = (HashMap<String, DynamicColor>) value;
            JScrollPane pane = new JScrollPane(new ColormapPanel((HashMap<String, DynamicColor>) value, this), JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            return pane;
        }
    }

    static class OverlayTypeTableModel extends AbstractTableModel implements OverlayManager.NewOverlayTypeListener {

        OverlayManager overlayManager;

        public OverlayTypeTableModel(OverlayManager overlayManager) {
            this.overlayManager = overlayManager;
        }

        @Override
        public void newOverlayType(SharedOverlayProperties properties) {
            this.fireTableRowsInserted(overlayManager.sharedPropertiesList.size() - 1, overlayManager.sharedPropertiesList.size() - 1);
        }

        @Override
        public int getRowCount() {
            return overlayManager.sharedPropertiesList.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SharedOverlayProperties type = overlayManager.sharedPropertiesList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return type.name;
                case 1:
                    return type.show_all;
                case 2:
                    return type.show_selected;
                case 3:
                    return type.colorMap;
                default:
                    return null;
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 1:
                    return true;
                case 2:
                    return true;
                case 3:
                    return true;
                default:
                    return false;
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    assert false;
                case 1:
                    overlayManager.sharedPropertiesList.get(rowIndex).show_all = (Boolean) aValue;
                    break;
                case 2:
                    overlayManager.sharedPropertiesList.get(rowIndex).show_selected = (Boolean) aValue;
                    break;
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Overlay";
                case 1:
                    return "Always";
                case 2:
                    return "On Selection";
                case 3:
                    return "Colors";
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return Boolean.class;
                case 2:
                    return Boolean.class;
                case 3:
                    return HashMap.class;
                default:
                    return null;
            }
        }


    };
}

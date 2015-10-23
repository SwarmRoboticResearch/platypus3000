package platypus3000.utils;

import org.jbox2d.common.Vec2;
import platypus3000.simulation.control.RobotController;
import platypus3000.simulation.control.RobotInterface;
import platypus3000.visualisation.InteractiveVisualisation;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by doms on 7/5/14.
 */
public class ForceTuner {
    private Vec2 summedForce = new Vec2();

    static TuningWindow tuningWindow;

    static HashMap<String, Model> models = new HashMap<String, Model>();
    static Model shownModel = null;
    Model model;

    public ForceTuner(String name, RobotController controller){
          if(!models.containsKey(name)){
             model = new Model();
             model.name = name;
             models.put(name, model);
              if(tuningWindow != null)
                tuningWindow.addToComboBox(name);
          }  else {
              model = models.get(name);
          }
        if(shownModel == null) shownModel = model;
    }

    static Thread refresher = null;
    public static void show(){
        tuningWindow = new TuningWindow();
        tuningWindow.setVisible(true);
        if(refresher==null) {
            refresher = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(tuningWindow.isShowing()){
                        refresh();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            refresher.start();
        }
    }

    public static void hide(){
        if(tuningWindow != null)
            tuningWindow.setVisible(false);
        refresher.stop();
        refresher = null;
    }
    public void addForce(String name, Vec2 force, RobotInterface robot) {
        addForce(name, force, 1f, robot);
    }

    public void addForce(String name, Vec2 force, float influence, RobotInterface robot){
        if(!model.entries.containsKey(name)){
            synchronized (models) {
                if(!model.entries.containsKey(name)) {
                    Row row = new Row(name);
                    row.influence = influence;
                    model.entries.put(name, row);
                    model.sortedEntries.add(row);
                    refresh();
                }
            }
        }
        Row row = model.entries.get(name);

        if(row.activated){
            Vec2 toAdd = force == null ? new Vec2() : force.mul(row.influence);
            summedForce.addLocal(toAdd);
            if( InteractiveVisualisation.instance != null && InteractiveVisualisation.instance.isSelectedRobot(robot)){
                row.selectedRobot = toAdd.length();
            }
        }

    }

    class Model{
        String name;
        HashMap<String, Row> entries = new HashMap<String, Row>();
        ArrayList<Row> sortedEntries = new ArrayList<Row>();
    }

    public static void refresh(){
        if(tuningWindow != null)
            tuningWindow.tuningTable.tuningTableModel.fireTableDataChanged();
    }

    public Vec2 getForce() {
       Vec2 ret = summedForce.clone();
        summedForce.setZero();
        return ret;
    }


    class Row {
        Row(String name){
            this.name = name;
        }
        String name;
        float influence=1f;
        boolean activated=true;
        float selectedRobot = 0f;
    }


    private static class TuningWindow extends JFrame {
        JPanel jPanel = new JPanel();
        TuningTable tuningTable = new TuningTable();
        JComboBox jComboBox = new JComboBox();

        TuningWindow(){
            this.add(jPanel);
            //jPanel.setLayout(new FlowLayout());
            jPanel.setLayout(new GridBagLayout());
                    jPanel.add(jComboBox);
            jPanel.add(new JScrollPane(tuningTable));
            jComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    shownModel = models.get((String)jComboBox.getSelectedItem());
                }
            });
            setSize(600,600);
            setResizable(false);
            setVisible(true);

        }

        void addToComboBox(String s){
            jComboBox.addItem(s);
        }
    }

    private static class TuningTable extends JTable{
        TuningTableModel tuningTableModel = new TuningTableModel();
        public TuningTable(){
            super();
            setModel(tuningTableModel);
            setRowHeight(40);
            getColumnModel().getColumn(1).setPreferredWidth(200);
            setPreferredScrollableViewportSize(new Dimension(600, (int) getPreferredScrollableViewportSize().getHeight()));
        }
    }

    private static class TuningTableModel extends AbstractTableModel{

        @Override
        public int getRowCount() {
            if(shownModel == null) return 0;
            return shownModel.entries.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }
        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0: return "Activated";
                case 1: return "Name";
                case 2: return "Influence";
                case 3: return "SelectedRobot";
                default: return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: return Boolean.class;
                case 1: return String.class;
                case 2: return Float.class;
                case 3: return Float.class;
                default: return null;
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    shownModel.sortedEntries.get(rowIndex).activated = (Boolean)aValue;
                    break;
                case 2:
                    shownModel.sortedEntries.get(rowIndex).influence = (Float)aValue;
                    break;
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return true;
                case 1:
                    return false;
                case 2:
                    return true;
                case 3:
                    return false;
                default:
                    return false;
            }
        }

        @Override
        public Object getValueAt(int i, int i2) {
            if(shownModel.sortedEntries.size()<=i) return null;
            switch(i2){
                case 0: return shownModel.sortedEntries.get(i).activated;
                case 1: return shownModel.sortedEntries.get(i).name;
                case 2: return shownModel.sortedEntries.get(i).influence;
                case 3: return shownModel.sortedEntries.get(i).selectedRobot;
            }
            return null;
        }
    }
}

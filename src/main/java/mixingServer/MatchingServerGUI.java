package mixingServer;

import Globals.Capsule;
import Globals.CriticalTuple;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatchingServerGUI extends JFrame{

    private JPanel mainPanel;

    private JTable receivedCapsulesTable;

    private JTable criticalFacilitiesTable;

    private JTable uninformedTokensTable;

    private MatchingServer matchingServer;

    private DefaultTableModel receivedCapsulesTableModel;

    private DefaultTableModel criticalFacilitiesTableModel;

    private DefaultTableModel uninformedTokensTableModel;

    public MatchingServerGUI(String title){
        super(title);

        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            matchingServer = new MatchingServer();
            registry.bind("MatchingServer", matchingServer);

            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setSize(new Dimension(1000, 600));
            this.setLocationRelativeTo(null);
            this.setLayout(null);
            this.setResizable(false);

            receivedCapsulesTableModel = new DefaultTableModel();
            criticalFacilitiesTableModel = new DefaultTableModel();
            uninformedTokensTableModel = new DefaultTableModel();

            this.mainPanel = new JPanel();
            this.mainPanel.setBounds(0, 0, 1000, 400);
            this.receivedCapsulesTable = new JTable(receivedCapsulesTableModel);
            receivedCapsulesTableModel.addColumn("Interval");
            receivedCapsulesTableModel.addColumn("User Token");
            receivedCapsulesTableModel.addColumn("CF Hash");

            this.criticalFacilitiesTable = new JTable(criticalFacilitiesTableModel);
            criticalFacilitiesTableModel.addColumn("Interval");
            criticalFacilitiesTableModel.addColumn("CF Hash");

            this.uninformedTokensTable = new JTable(uninformedTokensTableModel);
            uninformedTokensTableModel.addColumn("Uninformed Tokens");

            refreshRows();

            Border margin = new EmptyBorder(10,10,10,10);
            receivedCapsulesTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        int row = receivedCapsulesTable.getSelectedRow();
                        int col = receivedCapsulesTable.getSelectedColumn();
                        String value = "";
                        if(col == 0){
                            value = matchingServer.getCapsules().get(row).getInterval().toString();
                        } else if (col == 1){
                            value = matchingServer.getCapsules().get(row).getUserToken().toLongString();
                        } else if (col == 2){
                            value = matchingServer.getCapsules().get(row).getCfHash();
                        }
                        JTextArea ta = new JTextArea(value,20,40);
                        ta.setEditable(false);
                        ta.setLineWrap(true);
                        JOptionPane.showMessageDialog(MatchingServerGUI.this, ta);
                    }
                }
            });

            criticalFacilitiesTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        try {
                            int row = criticalFacilitiesTable.getSelectedRow();
                            int col = criticalFacilitiesTable.getSelectedColumn();
                            String value = "";
                            if(col == 0){
                                value = matchingServer.getCriticalTuples().get(row).getTimeInterval().toString();
                            } else if (col == 1){
                                value = matchingServer.getCriticalTuples().get(row).getCateringFacilityHash();
                            }
                            JTextArea ta = new JTextArea(value,20,40);
                            ta.setEditable(false);
                            ta.setLineWrap(true);
                            JOptionPane.showMessageDialog(MatchingServerGUI.this, ta);
                        } catch (RemoteException ex){
                            ex.printStackTrace();
                        }
                    }
                }
            });

            uninformedTokensTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        int row = uninformedTokensTable.getSelectedRow();
                        int col = uninformedTokensTable.getSelectedColumn();
                        String value = "";
                        value = matchingServer.getUninformedTokens().get(row);
                        JTextArea ta = new JTextArea(value,20,40);
                        ta.setEditable(false);
                        ta.setLineWrap(true);
                        JOptionPane.showMessageDialog(MatchingServerGUI.this, ta);
                    }
                }
            });

            JScrollPane receivedCapsulesTableScroller = new JScrollPane(receivedCapsulesTable);
            JScrollPane criticalFacilitiesTableScroller = new JScrollPane(criticalFacilitiesTable);
            JScrollPane uninformedTokensTableScroller = new JScrollPane(uninformedTokensTable);

            JPanel tablesPanel = new JPanel();
            tablesPanel.setLayout(new GridLayout(1, 3, 10, 10));
            mainPanel.setLayout(new BorderLayout());

            JLabel titleLabel = new JLabel("Matching Server");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setBorder(margin);

            tablesPanel.add(receivedCapsulesTableScroller);
            tablesPanel.add(criticalFacilitiesTableScroller);
            tablesPanel.add(uninformedTokensTableScroller);

            receivedCapsulesTableScroller.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Capsules"), margin));
            criticalFacilitiesTableScroller.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Critical Facilities"), margin));
            uninformedTokensTableScroller.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Uninformed Tokens"), margin));

            mainPanel.add(titleLabel, BorderLayout.PAGE_START);
            mainPanel.add(tablesPanel, BorderLayout.CENTER);

            this.add(mainPanel);

            ExecutorService executerService = Executors.newCachedThreadPool();

            executerService.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        refreshRows();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void refreshRows(){
        receivedCapsulesTableModel.setRowCount(0);
        for(String[] s : getCapsulesData(matchingServer.getCapsules())){
            receivedCapsulesTableModel.addRow(s);
        }

        uninformedTokensTableModel.setRowCount(0);
        for(String[] s : getUninformedTokensData(matchingServer.getUninformedTokens())){
            uninformedTokensTableModel.addRow(s);
        }
        try {
            criticalFacilitiesTableModel.setRowCount(0);
            for(String[] s : getCriticalFacilitiesData(matchingServer.getCriticalTuples())){
                criticalFacilitiesTableModel.addRow(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String[][] getCapsulesData(java.util.List<Capsule> capsules){
        String[][] data = new String[capsules.size()][3];
        for (int i = 0; i < capsules.size(); i++){
            data[i][0] = capsules.get(i).getInterval().toString();
            data[i][1] = capsules.get(i).getUserToken().toShortString();
            data[i][2] = capsules.get(i).getCfHashShortString();
        }
        return data;
    }

    private String[][] getCriticalFacilitiesData(java.util.List<CriticalTuple> criticalTuples){
        String[][] data = new String[criticalTuples.size()][2];
        for (int i = 0; i < criticalTuples.size(); i++){
            data[i][0] = criticalTuples.get(i).getTimeInterval().toString();
            data[i][1] = criticalTuples.get(i).getCateringFacilityHashShortString();
        }
        return data;
    }

    private String[][] getUninformedTokensData(java.util.List<String> uninformedTokens){
        String[][] data = new String[uninformedTokens.size()][1];
        for (int i = 0; i < uninformedTokens.size(); i++){
            String uninformedToken = uninformedTokens.get(i);
            if(uninformedToken.length() > 20){
                uninformedToken = uninformedToken.substring(0, 20) + "...";
            }
            data[i][0] = uninformedToken;
        }
        return data;
    }


    public static void main(String[] args) throws ParseException {
        JFrame frame = new MatchingServerGUI("Matching Server");
        frame.setVisible(true);
    }
}

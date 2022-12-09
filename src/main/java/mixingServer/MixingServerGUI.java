package mixingServer;

import Globals.Capsule;
import users.CateringFacilityGUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.ParseException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MixingServerGUI extends JFrame {
    private JPanel mainPanel;

    private JTable receivedCapsulesTable;

    private JButton flushCapsulesButton;

    private MixingServer mixingServer;

    private String[] tableColumns;

    private DefaultTableModel tableModel;

    private JLabel flushFeedbackLabel;

    public MixingServerGUI(String title){
        super(title);

        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            mixingServer = new MixingServer();
            registry.bind("MixingServer", mixingServer);

            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setSize(new Dimension(1920, 1080));
            this.setLocationRelativeTo(null);
            this.setLayout(null);
            this.setResizable(false);

            tableModel = new DefaultTableModel();

            this.mainPanel = new JPanel();
            this.mainPanel.setBounds(0, 0, 1600, 800);
//            tableColumns=new String[]{"Interval","User Token","Catering Facility Hash"};
            this.receivedCapsulesTable = new JTable(tableModel);
            tableModel.addColumn("Interval");
            tableModel.addColumn("User Token (Signature)");
            tableModel.addColumn("Catering Facility Hash");


            refreshRows();
            this.flushCapsulesButton = new JButton("Flush capsules");

            Border margin = new EmptyBorder(10,10,10,10);
            receivedCapsulesTable.getColumnModel().getColumn(0).setWidth(500);
            receivedCapsulesTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        final JTable jTable = (JTable) e.getSource();
                        int row = receivedCapsulesTable.getSelectedRow();
                        int col = receivedCapsulesTable.getSelectedColumn();
                        String value = "";
                        if(col == 0){
                            value = mixingServer.getReceivedCapsules().get(row).getInterval().toString();
                        } else if (col == 1){
                            value = mixingServer.getReceivedCapsules().get(row).getUserToken().toFullString();
                        } else if (col == 2){
                            value = mixingServer.getReceivedCapsules().get(row).getCfHash();
                        }
                        JTextArea ta = new JTextArea(value,20,40);
                        ta.setEditable(false);
                        ta.setLineWrap(true);
                        JOptionPane.showMessageDialog(MixingServerGUI.this, ta);
                    }
                }
            });

            JScrollPane tableScroller = new JScrollPane(receivedCapsulesTable);
            receivedCapsulesTable.setBorder(new EmptyBorder(10,10, 10, 10));
            mainPanel.setLayout(new BorderLayout());

            JLabel titleLabel = new JLabel("Mixing Server");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setBorder(margin);

            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new GridLayout(2, 1, 10, 10));

            flushFeedbackLabel = new JLabel();
            flushFeedbackLabel.setBorder(margin);
            flushCapsulesButton.addActionListener(this::flushCapsulesButtonClicked);

            bottomPanel.add(flushCapsulesButton);
            bottomPanel.add(flushFeedbackLabel);

            mainPanel.add(titleLabel, BorderLayout.PAGE_START);
            mainPanel.add(tableScroller, BorderLayout.CENTER);
            mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

            this.add(mainPanel);

            ExecutorService executerService = Executors.newCachedThreadPool();

            executerService.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        System.out.println("Update received capsules");
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
        tableModel.setRowCount(0);
        for(String[] s : getCapsulesData(mixingServer.getReceivedCapsules())){
            tableModel.addRow(s);
        }
    }

    private String[][] getCapsulesData(java.util.List<Capsule> capsules){
        String[][] data = new String[capsules.size()][3];
        for (int i = 0; i < capsules.size(); i++){
            data[i][0] = capsules.get(i).getInterval().toString();
            data[i][1] = capsules.get(i).getUserToken().toString();
            data[i][2] = capsules.get(i).getCfHash();
        }
        return data;
    }

    private void flushCapsulesButtonClicked(java.awt.event.ActionEvent evt) {
            try {
                mixingServer.flushCapsules();
                refreshRows();
            } catch (RemoteException ex){
                flushFeedbackLabel.setText("De capsules konden niet geflusht worden");
                flushFeedbackLabel.setForeground(Color.RED);
                ex.printStackTrace();
            }
    }


    public static void main(String[] args) throws ParseException {
        JFrame frame = new MixingServerGUI("Mixing Server");
        frame.setVisible(true);
    }

}

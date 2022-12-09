package mixingServer;

import Globals.Capsule;
import users.CateringFacilityGUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
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
            this.setSize(new Dimension(900, 600));
            this.setLocationRelativeTo(null);
            this.setLayout(null);
            this.setResizable(false);

            tableModel = new DefaultTableModel();

            this.mainPanel = new JPanel();
            this.mainPanel.setBounds(0, 0, 900, 400);
//            tableColumns=new String[]{"Interval","User Token","Catering Facility Hash"};
            this.receivedCapsulesTable = new JTable(tableModel);
            tableModel.addColumn("Interval");
            tableModel.addColumn("User Token");
            tableModel.addColumn("Catering Facility Hash");

            refreshRows();
            this.flushCapsulesButton = new JButton("Flush capsules");

            Border margin = new EmptyBorder(10,10,10,10);
//            Vector receivedCapsulesVector = new Vector(mixingServer.getReceivedCapsules());

            receivedCapsulesTable.setEnabled(false);
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

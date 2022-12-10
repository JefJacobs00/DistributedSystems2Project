package users;

import Globals.UserLog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorGUI extends JFrame {

    private CentralHealthAuthority healthAuthority;

    private String healthAuthorityReg;

    private JPanel mainPanel;

    private JTable logsTable;

    private JButton receiveLogsButton;

    private JButton transmitLogsButton;

    private java.util.List<UserLog> userLogs;

    private DefaultTableModel logsTableModel;

    private JLabel connectionNrLabel;

    public DoctorGUI(String title){
        super(title);

        try {
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setSize(new Dimension(800, 700));
            this.setLocationRelativeTo(null);
            this.setLayout(null);
            this.setResizable(false);

            healthAuthority = new CentralHealthAuthority();

            logsTableModel = new DefaultTableModel();

            this.mainPanel = new JPanel();
            this.mainPanel.setBounds(0, 0, 800, 600);
            this.logsTable = new JTable(logsTableModel);
            logsTableModel.addColumn("User Token");
            logsTableModel.addColumn("CF Hash");
            logsTableModel.addColumn("Hash random number");
            logsTableModel.addColumn("Visit interval");


            refreshRows();
            this.receiveLogsButton = new JButton("Ontvang Logs");
            this.transmitLogsButton = new JButton("Stuur logs door");

            Border margin = new EmptyBorder(10,10,10,10);
            logsTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        int row = logsTable.getSelectedRow();
                        int col = logsTable.getSelectedColumn();
                        String value = "";
                        if(col == 0){
                            value = userLogs.get(row).getUserToken();
                        } else if (col == 1){
                            value = userLogs.get(row).getCfHash();
                        } else if (col == 2){
                            value = String.valueOf(userLogs.get(row).getHashRandomNumber());
                        } else if (col == 3){
                            value = userLogs.get(row).getVisitInterval().toString();
                        }
                        JTextArea ta = new JTextArea(value,20,40);
                        ta.setEditable(false);
                        ta.setLineWrap(true);
                        JOptionPane.showMessageDialog(DoctorGUI.this, ta);
                    }
                }
            });


            JScrollPane tableScroller = new JScrollPane(logsTable);
            mainPanel.setLayout(new BorderLayout());

            JLabel titleLabel = new JLabel("Doctor GUI");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setBorder(margin);

            tableScroller.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Logs"), margin));

            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new GridLayout(2, 1, 10, 10));

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new GridLayout(2, 1, 10, 10));

            connectionNrLabel = new JLabel();
            topPanel.setBorder(margin);
            topPanel.add(connectionNrLabel);
            topPanel.add(receiveLogsButton);
            topPanel.setBorder(margin);


            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BorderLayout());

            centerPanel.add(topPanel, BorderLayout.PAGE_START);
            centerPanel.add(tableScroller, BorderLayout.CENTER);


            receiveLogsButton.addActionListener(this::receiveLogsButtonClicked);
            transmitLogsButton.addActionListener(this::transmitLogsButtonClicked);


            bottomPanel.add(transmitLogsButton);

            mainPanel.add(titleLabel, BorderLayout.PAGE_START);
            mainPanel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

            this.add(mainPanel);

            ExecutorService executerService = Executors.newCachedThreadPool();

            executerService.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        System.out.println("Update logs");

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
        System.out.println(healthAuthority.getUserLogs().size());
        logsTableModel.setRowCount(0);
        for(String[] s : getUserLogsData(healthAuthority.getUserLogs())){
            logsTableModel.addRow(s);
        }
    }

    private String[][] getUserLogsData(List<UserLog> userLogs){
        String[][] data = new String[userLogs.size()][3];
        for (int i = 0; i < userLogs.size(); i++){
            data[i][0] = userLogs.get(i).getUserTokenShortString();
            data[i][1] = userLogs.get(i).getCfHashShortString();
            data[i][2] = String.valueOf(userLogs.get(i).getHashRandomNumber());
            data[i][3] = userLogs.get(i).getVisitInterval().toString();
        }
        return data;
    }

    private void receiveLogsButtonClicked(java.awt.event.ActionEvent evt) {
        try {
            String connectionNr = healthAuthority.start();
            connectionNrLabel.setText("<html>" + "Connectienummer: " + "<B>" + connectionNr + "</B>" + "</html>");
        } catch(AlreadyBoundException | RemoteException ex){
            connectionNrLabel.setText("Er is iets foutgelopen bij de server.");
            connectionNrLabel.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }

    private void transmitLogsButtonClicked(java.awt.event.ActionEvent evt) {
        healthAuthority.sendLogs();
    }


    public static void main(String[] args) throws ParseException {
        JFrame frame = new DoctorGUI("Doctor GUI");
        frame.setVisible(true);
    }
}

package users;

import Globals.Capsule;
import Globals.UserLog;
import mixingServer.MixingServer;
import mixingServer.MixingServerGUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorGUI extends JFrame {

    private CentralHealthAuthority healthAuthority;

    private String healthAuthorityReg;

    private JPanel mainPanel;

    private JTable logsTable;

    private JButton receiveLogsButton;

    private JButton transmitLogsButton;

    private JLabel transmitLogsFeedbackLabel;

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
            this.receiveLogsButton = new JButton("Receive Logs");
            this.transmitLogsButton = new JButton("Send logs");

            Border margin = new EmptyBorder(10,10,10,10);
            logsTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        int row = logsTable.getSelectedRow();
                        int col = logsTable.getSelectedColumn();
                        String value = "";
                        if(col == 0){
                            value = healthAuthority.getUserLogsList().get(row).getUserToken();
                        } else if (col == 1){
                            value = healthAuthority.getUserLogsList().get(row).getCfHash();
                        } else if (col == 2){
                            value = String.valueOf(healthAuthority.getUserLogsList().get(row).getHashRandomNumber());
                        } else if (col == 3){
                            value = healthAuthority.getUserLogsList().get(row).getVisitInterval().toString();
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

            transmitLogsFeedbackLabel = new JLabel();

            bottomPanel.add(transmitLogsButton);
            bottomPanel.add(transmitLogsFeedbackLabel);

            bottomPanel.setBorder(margin);

            mainPanel.add(titleLabel, BorderLayout.PAGE_START);
            mainPanel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

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
        logsTableModel.setRowCount(0);
        for(String[] s : getUserLogsData(healthAuthority.getUserLogsList())){
            logsTableModel.addRow(s);
        }
    }

    private String[][] getUserLogsData(List<UserLog> userLogs){
        String[][] data = new String[userLogs.size()][4];
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
            connectionNrLabel.setText("<html>" + "Connection Nr: " + "<B>" + connectionNr + "</B>" + "</html>");
        } catch(AlreadyBoundException | RemoteException ex){
            connectionNrLabel.setText("Server error: something went wrong");
            connectionNrLabel.setForeground(Color.RED);
            ex.printStackTrace();
            Timer timer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Clear text or whatever you want
                    connectionNrLabel.setText("");
                }
            });
            timer.start();
        }
    }

    private void transmitLogsButtonClicked(java.awt.event.ActionEvent evt) {
        try {
            healthAuthority.sendLogs();
            transmitLogsFeedbackLabel.setText("The logs have been sent successfully");
            transmitLogsFeedbackLabel.setForeground(Color.GREEN);
        } catch (IOException | SignatureException | NoSuchAlgorithmException | InvalidKeyException ex){
            transmitLogsFeedbackLabel.setText("The logs could not be sent");
            transmitLogsFeedbackLabel.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) throws ParseException {
        JFrame frame = new DoctorGUI("Central Health Authority");
        frame.setVisible(true);
    }
}

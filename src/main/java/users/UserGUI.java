package users;

import Globals.UserLog;
import com.google.zxing.NotFoundException;
import org.bouncycastle.util.encoders.Hex;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserGUI extends JFrame {
    private JTabbedPane mainPanel;

    private JPanel registrationParentPanel;

    private JPanel registrationPanel;
    private JPanel regJPanel;

    private JPanel readQrParentPanel;

    private JPanel readQrPanel;

    private JButton openQrButton;

    private BufferedImage qrBufferedImage;

    private JLabel confirmationLabel;

    private JButton leaveFacilityButton;

    private JLabel qrImage;

    private JPanel logSendParentPanel;

    private JPanel logSendPanel;

    private JPanel logJPanel;

    private JTextField healthAuthorityTextField;

    private JButton sendLogsButton;

    private JTextField phoneNumberTextField;
    private JButton submitRegistrationButton;

    private JLabel logsFeedbackLabel;

    private User user;

    private JTable logsTable;

    private DefaultTableModel logsTableModel;

    private boolean isCritical;

    private boolean isAuthenticated;

    public UserGUI(String title) throws ParseException {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(920, 700));
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setResizable(false);
        this.isAuthenticated = false;
        this.mainPanel = new JTabbedPane();
        this.mainPanel.setBounds(0, 0, 920, 175);
        initRegistrationForm();
        initQrCodeForm();
        initLogSendForm();
        mainPanel.add("User Registration", registrationParentPanel);

        this.add(mainPanel);

        mainPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Component selectedComponent = mainPanel.getSelectedComponent();
                if ((registrationParentPanel).equals(selectedComponent)) {
                    initQrCodeForm();
                    initLogSendForm();
                    user = null;
                    mainPanel.remove(2);
                    mainPanel.remove(1);
                    mainPanel.setSize(new Dimension(920, 175));
                } else if (mainPanel.getSelectedIndex() == 1) {
                    mainPanel.setSize(new Dimension(920, 500));
                } else if (mainPanel.getSelectedIndex() == 2) {
                    mainPanel.setSize(new Dimension(920, 600));
                }
            }
        });

        ExecutorService executerService = Executors.newCachedThreadPool();

        executerService.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if(user != null){
                        refreshRows();
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initLogSendForm(){
        this.logSendPanel = new JPanel();
        this.logSendParentPanel = new JPanel();

        Border margin = new EmptyBorder(10,10,10,10);

        sendLogsButton = new JButton("Send");
        sendLogsButton.setMargin(new Insets(10, 10, 10, 10));

        logsFeedbackLabel = new JLabel();
        logsFeedbackLabel.setBorder(margin);

        logSendParentPanel.setLayout(new BorderLayout());

        logsTableModel = new DefaultTableModel();
        this.logsTable = new JTable(logsTableModel);
        logsTableModel.addColumn("User Token");
        logsTableModel.addColumn("CF Hash");
        logsTableModel.addColumn("Hash random number");
        logsTableModel.addColumn("Visit interval");
        if(user != null)
            refreshRows();
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridLayout(2, 1, 10, 10));
        JLabel titleLabel = new JLabel("User Log Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(margin);

        JLabel statusLabel = new JLabel();
        statusLabel.setBorder(margin);
        try {
            statusLabel.setText("User Status: " + user.getLogs());
            if(user.getUserStatus().equals("Healthy")){
                statusLabel.setForeground(Color.GREEN);
            } else if (user.getUserStatus().equals("Infected")){
                statusLabel.setForeground(Color.RED);
            }
        } catch (Exception ex){
            statusLabel.setText("User Status could not be fetched from server");
            statusLabel.setForeground(Color.RED);
        }

        titlePanel.add(titleLabel);
        titlePanel.add(statusLabel);

        JPanel logsPanel = new JPanel();

        logsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = logsTable.getSelectedRow();
                    int col = logsTable.getSelectedColumn();
                    String value = "";
                    if(col == 0){
                        value = user.getLogs().get(row).getUserToken();
                    } else if (col == 1){
                        value = user.getLogs().get(row).getCfHash();
                    } else if (col == 2){
                        value = String.valueOf(user.getLogs().get(row).getHashRandomNumber());
                    } else if (col == 3){
                        value = user.getLogs().get(row).getVisitInterval().toString();
                    }
                    JTextArea ta = new JTextArea(value,20,40);
                    ta.setEditable(false);
                    ta.setLineWrap(true);
                    JOptionPane.showMessageDialog(UserGUI.this, ta);
                }
            }
        });

        JScrollPane tableScroller = new JScrollPane(logsTable);

        logsPanel.setLayout(new BorderLayout());

        logSendPanel.setLayout(new GridLayout(1, 2, 10, 10));
        logSendPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Send User Logs"), margin));

        logJPanel = new JPanel();
        logJPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Health Authority"), margin));
        logJPanel.setLayout(new GridLayout(1, 2, 10, 10));

        sendLogsButton.addActionListener(this::sendLogsButtonClicked);

        JLabel healthAuthorityLabel = new JLabel("CHA connection number");
        healthAuthorityTextField = new JTextField("");

        logJPanel.add(healthAuthorityLabel);
        logJPanel.add(healthAuthorityTextField);

        logSendPanel.add(logJPanel);
        logSendPanel.add(sendLogsButton);

        logsPanel.add(tableScroller, BorderLayout.CENTER);
        logsPanel.add(logSendPanel, BorderLayout.PAGE_END);
//        logSendPanel.setBorder(margin);

        logSendParentPanel.add(titlePanel, BorderLayout.PAGE_START);
        logSendParentPanel.add(logsPanel, BorderLayout.CENTER);
        logSendParentPanel.add(logsFeedbackLabel, BorderLayout.PAGE_END);
    }

    private void sendLogsButtonClicked(java.awt.event.ActionEvent evt) {
        String chaIdentifier = healthAuthorityTextField.getText();
        if(chaIdentifier.equals("")){
            JOptionPane.showMessageDialog(this, "Gelieve een geldige identifier op te geven");
        } else {
            try {
                user.sendLogs(chaIdentifier);
                logsFeedbackLabel.setText("Logs zijn succesvol verstuurd");
                logsFeedbackLabel.setForeground(Color.GREEN);
            } catch (NotBoundException ex){
                logsFeedbackLabel.setText("Er is geen gezondheidsorganisatie aan dit ID gekoppeld");
                logsFeedbackLabel.setForeground(Color.RED);
                ex.printStackTrace();
            } catch (RemoteException ex) {
                logsFeedbackLabel.setText("Er is iets foutgelopen bij de server");
                logsFeedbackLabel.setForeground(Color.RED);
                ex.printStackTrace();
            }
        }
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Clear text or whatever you want
                logsFeedbackLabel.setText("");
            }
        });
        timer.start();
    }

    private void initQrCodeForm(){
        this.readQrPanel = new JPanel();
        this.readQrParentPanel = new JPanel();

        JPanel generateFormPanel = new JPanel();

        generateFormPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        Border margin = new EmptyBorder(10,10,10,10);

        confirmationLabel = new JLabel();
        confirmationLabel.setFont(new Font("Arial", Font.CENTER_BASELINE, 16));
        confirmationLabel.setBorder(margin);

        openQrButton = new JButton("QR code openen");
        qrImage = new JLabel();
        leaveFacilityButton = new JButton("Faciliteit verlaten");

        JPanel qrPanel = new JPanel();
        qrPanel.setLayout(new GridLayout(1, 2, 10, 10));
        qrPanel.add(qrImage);
        qrPanel.add(confirmationLabel);

        qrPanel.setBorder(margin);

        readQrParentPanel.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridLayout(2, 1, 10, 10));
        JLabel titleLabel = new JLabel("QR Code Generator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(margin);

        JLabel statusLabel = new JLabel();
        statusLabel.setBorder(margin);
        try {
            statusLabel.setText("User Status: " + user.getUserStatus());
            if(user.getUserStatus().equals("Healthy")){
                statusLabel.setForeground(Color.GREEN);
            } else if (user.getUserStatus().equals("Infected")){
                statusLabel.setForeground(Color.RED);
            }
        } catch (Exception ex){
            statusLabel.setText("User Status could not be fetched from server");
            statusLabel.setForeground(Color.RED);
        }

        titlePanel.add(titleLabel);
        titlePanel.add(statusLabel);

        readQrParentPanel.add(titlePanel, BorderLayout.PAGE_START);
        readQrParentPanel.add(readQrPanel, BorderLayout.CENTER);
        generateFormPanel.add(openQrButton);

        readQrPanel.setLayout(new BorderLayout());
        readQrPanel.setBorder(margin);
        readQrPanel.add(generateFormPanel, BorderLayout.PAGE_START);
        readQrPanel.add(qrPanel, BorderLayout.CENTER);
        readQrPanel.add(leaveFacilityButton, BorderLayout.PAGE_END);
        leaveFacilityButton.setVisible(false);
        openQrButton.setVisible(true);
        qrImage.setOpaque(true);
        openQrButton.addActionListener(this::openQrButtonClicked);
        leaveFacilityButton.addActionListener(this::leaveFacilityButtonClicked);
    }

    private void refreshRows(){
        logsTableModel.setRowCount(0);
        for(String[] s : getUserLogsData(user.getLogs())){
            logsTableModel.addRow(s);
        }
    }

    private String[][] getUserLogsData(java.util.List<UserLog> userLogs){
        String[][] data = new String[userLogs.size()][4];
        for (int i = 0; i < userLogs.size(); i++){
            data[i][0] = userLogs.get(i).getUserTokenShortString();
            data[i][1] = userLogs.get(i).getCfHashShortString();
            data[i][2] = String.valueOf(userLogs.get(i).getHashRandomNumber());
            data[i][3] = userLogs.get(i).getVisitInterval().toString();
        }
        return data;
    }

    private void leaveFacility(){
        user.leaveFacility();
        qrBufferedImage = null;
        qrImage.setIcon(null);
        qrImage.setOpaque(false);
        qrImage.setBackground(null);
        confirmationLabel.setText("");
        leaveFacilityButton.setVisible(false);
    }

    private void readQrCode() throws IOException, NotFoundException, SignatureException, InvalidKeyException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp"));
        int returnVal = fileChooser.showOpenDialog(null);
        if ( returnVal == JFileChooser.APPROVE_OPTION ){
            File file = fileChooser.getSelectedFile();
            qrBufferedImage = ImageIO.read(file);
            //qrImage.setIcon(new ImageIcon(qrBufferedImage));
            String confirmationText = user.visitFacility(qrBufferedImage);
            if(!confirmationText.equals("Invalid token")){
                openQrButton.setVisible(false);
                byte[] confirmation = Hex.decode(confirmationText);
                //qrImage.setIcon(new ImageIcon(qrBufferedImage));
                qrImage.setOpaque(true);
                qrImage.setBackground(new Color(confirmation[0] & 0xff,confirmation[1]& 0xff,confirmation[2] & 0xff));
                confirmationText = confirmationText.substring(confirmationText.length()-4);
                leaveFacilityButton.setVisible(true);
            }else{
                qrImage.setOpaque(false);
            }
            confirmationLabel.setText(confirmationText);

        }
        readQrPanel.repaint();
    }

    private void openQrButtonClicked(java.awt.event.ActionEvent evt){
        try {
            readQrCode();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void leaveFacilityButtonClicked(java.awt.event.ActionEvent evt){
        leaveFacility();
        openQrButton.setVisible(true);
    }

    private void initRegistrationForm(){
        this.registrationPanel = new JPanel();
        this.registrationParentPanel = new JPanel();

        Border margin = new EmptyBorder(10,10,10,10);

        submitRegistrationButton = new JButton("Submit");

        registrationParentPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("User Registration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(margin);

        registrationParentPanel.add(titleLabel, BorderLayout.PAGE_START);
        registrationParentPanel.add(registrationPanel, BorderLayout.CENTER);
        registrationParentPanel.add(submitRegistrationButton, BorderLayout.PAGE_END);

        registrationPanel.setLayout(new GridLayout(1, 2, 10, 10));
        registrationPanel.setBorder(margin);

        regJPanel = new JPanel();
        regJPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("General Information"), margin));
        regJPanel.setLayout(new GridLayout(1, 2, 10, 10));

        submitRegistrationButton.addActionListener(this::submitRegistrationButtonClicked);

        JLabel phoneNumberLabel = new JLabel("Phone Number");
        phoneNumberTextField = new JTextField("");

        regJPanel.add(phoneNumberLabel);
        regJPanel.add(phoneNumberTextField);

        registrationPanel.add(regJPanel);
    }

    private void submitRegistrationButtonClicked(java.awt.event.ActionEvent evt){
        String phoneNumber = phoneNumberTextField.getText();
        if(phoneNumber.equals("")
                || !verifyPhoneNumber(phoneNumber)){
            JOptionPane.showMessageDialog(this, "Please provide a valid phone number");
        } else {
            try {
                user = new User(phoneNumber);
                initQrCodeForm();
                initLogSendForm();
                this.isAuthenticated = true;
                mainPanel.add("QR Code Generator", readQrParentPanel);
                mainPanel.add("Log informatie", logSendParentPanel);
                mainPanel.setSelectedIndex(1);
            } catch (RuntimeException ex){
                JOptionPane.showMessageDialog(this, "Server error: something went wrong");
                ex.printStackTrace();
            }
        }
    }

    private boolean verifyPhoneNumber(String phoneNr){
        String patterns
                = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
                + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
                + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$";
        Pattern pattern = Pattern.compile(patterns);
        Matcher matcher = pattern.matcher(phoneNr);
        return matcher.matches();
    }

    public static void main(String[] args) throws ParseException {
        JFrame frame = new UserGUI("User");
        frame.setVisible(true);
    }
}

package users;

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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.text.ParseException;
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

    private boolean isCritical;

    private boolean isAuthenticated;

    public UserGUI(String title) throws ParseException {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(900, 600));
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setResizable(false);
        this.isAuthenticated = false;
        this.mainPanel = new JTabbedPane();
        this.mainPanel.setBounds(0, 0, 900, 200);
        initRegistrationForm();
        initQrCodeForm();
        initLogSendForm();
        mainPanel.add("User Registration", registrationParentPanel);
//        mainPanel.add("QR Code Generator", readQrParentPanel);

        this.add(mainPanel);

        mainPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Component selectedComponent = mainPanel.getSelectedComponent();
                if ((registrationParentPanel).equals(selectedComponent)) {
                    initQrCodeForm();
                    initLogSendForm();
                    user = null;
                    initQrCodeForm();
                    initLogSendForm();
                    mainPanel.remove(2);
                    mainPanel.remove(1);
                    mainPanel.setSize(new Dimension(900, 200));
                } else if (mainPanel.getSelectedIndex() == 1) {
                    mainPanel.setSize(new Dimension(900, 500));
                } else if (mainPanel.getSelectedIndex() == 2) {
                    mainPanel.setSize(new Dimension(900, 260));
                }
            }
        });
    }

    private void initLogSendForm(){
        this.logSendPanel = new JPanel();
        this.logSendParentPanel = new JPanel();

        Border margin = new EmptyBorder(10,10,10,10);

        sendLogsButton = new JButton("Verstuur");
        sendLogsButton.setMargin(new Insets(10, 10, 10, 10));

        logsFeedbackLabel = new JLabel();
        logsFeedbackLabel.setBorder(margin);

        logSendParentPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Verstuur gebruikerslogs");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(margin);

        logSendParentPanel.add(titleLabel, BorderLayout.PAGE_START);
        logSendParentPanel.add(logSendPanel, BorderLayout.CENTER);
        logSendParentPanel.add(logsFeedbackLabel, BorderLayout.PAGE_END);

        logSendPanel.setLayout(new GridLayout(2, 2, 10, 10));
        logSendPanel.setBorder(margin);


        logJPanel = new JPanel();
        logJPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Gezondheidsorganisatie"), margin));
        logJPanel.setLayout(new GridLayout(1, 2, 10, 10));

        sendLogsButton.addActionListener(this::sendLogsButtonClicked);

        JLabel healthAuthorityLabel = new JLabel("Gezondheidsorganisatie-ID");
        healthAuthorityTextField = new JTextField("");

        logJPanel.add(healthAuthorityLabel);
        logJPanel.add(healthAuthorityTextField);

        logSendPanel.add(logJPanel);
        logSendPanel.add(sendLogsButton);
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

        readQrParentPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("QR Code Generator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(margin);

        readQrParentPanel.add(titleLabel, BorderLayout.PAGE_START);
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

    private void leaveFacility(){
        qrBufferedImage = null;
        qrImage.setIcon(null);
        qrImage.setOpaque(false);
        confirmationLabel.setText("");
        user.leaveFacility();
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
    }

    private void initRegistrationForm(){
        this.registrationPanel = new JPanel();
        this.registrationParentPanel = new JPanel();

        Border margin = new EmptyBorder(10,10,10,10);

        submitRegistrationButton = new JButton("Submit");
        submitRegistrationButton.setMargin(new Insets(10, 10, 10, 10));

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
        regJPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Algemene Informatie"), margin));
        regJPanel.setLayout(new GridLayout(1, 2, 10, 10));

        submitRegistrationButton.addActionListener(this::submitRegistrationButtonClicked);

        JLabel phoneNumberLabel = new JLabel("Telefoonnummer");
        phoneNumberTextField = new JTextField("");

        regJPanel.add(phoneNumberLabel);
        regJPanel.add(phoneNumberTextField);

        registrationPanel.add(regJPanel);
    }

    private void submitRegistrationButtonClicked(java.awt.event.ActionEvent evt){
        String phoneNumber = phoneNumberTextField.getText();
        if(phoneNumber.equals("")
                || !verifyPhoneNumber(phoneNumber)){
            JOptionPane.showMessageDialog(this, "Gelieve een geldig telefoonnummer op te geven");
        } else {
            try {
                user = new User(phoneNumber);

                this.isAuthenticated = true;
                mainPanel.add("QR Code Generator", readQrParentPanel);
                mainPanel.add("Log informatie", logSendParentPanel);
                mainPanel.setSelectedIndex(1);
            } catch (RuntimeException ex){
                JOptionPane.showMessageDialog(this, "Er is iets foutgelopen bij de server");
                ex.printStackTrace();
            }
            try {
                this.isCritical = user.checkInfected();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
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
        JFrame frame = new UserGUI("Gebruiker");
        frame.setVisible(true);
    }
}

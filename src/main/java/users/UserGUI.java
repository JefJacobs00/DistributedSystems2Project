package users;

import com.google.zxing.NotFoundException;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserGUI extends JFrame {
    private JTabbedPane mainPanel;

    private JPanel registrationParentPanel;

    private JPanel registrationPanel;

    private JPanel readQrParentPanel;

    private JPanel readQrPanel;

    private JPanel regJPanel;

    private JTextField phoneNumberTextField;
    private JButton submitRegistrationButton;

    private JButton openQrButton;

    private BufferedImage qrBufferedImage;

    private JLabel confirmationLabel;

    private JButton leaveFacilityButton;

    private JLabel qrImage;

    private User user;

    public UserGUI(String title) throws ParseException {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(900, 600));
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setResizable(false);

        this.mainPanel = new JTabbedPane();
        this.mainPanel.setBounds(0, 0, 900, 200);
        initRegistrationForm();
        initQrCodeForm();
        mainPanel.add("User Registration", registrationParentPanel);
//        mainPanel.add("QR Code Generator", readQrParentPanel);

        this.add(mainPanel);

        mainPanel.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                switch (mainPanel.getSelectedIndex()) {
                    case 0:
                        user = null;
                        mainPanel.remove(readQrParentPanel);
                        initQrCodeForm();
                        mainPanel.setSize(new Dimension(900, 200));
                        break;
                    case 1:
                        mainPanel.setSize(new Dimension(900, 500));
                        break;
                }
            }
        });
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
        openQrButton.addActionListener(this::openQrButtonClicked);
        leaveFacilityButton.addActionListener(this::leaveFacilityButtonClicked);
    }

    private void leaveFacility(){
        qrBufferedImage = null;
        qrImage.setIcon(null);

        // TODO - add logic to leave facility
    }

    private void readQrCode() throws IOException, NotFoundException, SignatureException, InvalidKeyException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp"));
        int returnVal = fileChooser.showOpenDialog(null);
        if ( returnVal == JFileChooser.APPROVE_OPTION ){
            File file = fileChooser.getSelectedFile();
            qrBufferedImage = ImageIO.read(file);
            qrImage.setIcon(new ImageIcon(qrBufferedImage));
            String confirmationText = user.visitFacility(qrBufferedImage);
            if(!confirmationText.equals("Invalid token")){
                confirmationText = confirmationText.substring(confirmationText.length()-4);
                leaveFacilityButton.setVisible(true);
            }
            confirmationLabel.setText(confirmationText);
        }
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
            user = new User(phoneNumber);
            mainPanel.add("QR Code Generator", readQrParentPanel);
            mainPanel.setSelectedIndex(1);
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

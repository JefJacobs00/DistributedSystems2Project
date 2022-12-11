package users;

import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CateringFacilityGUI extends JFrame{
    private JTabbedPane mainPanel;

    private JPanel registrationParentPanel;

    private JPanel registrationPanel;

    private JPanel generateQrParentPanel;

    private JPanel generateQrPanel;

    private JPanel addressJPanel;

    private JPanel regJPanel;


    private Map<String, JTextField> regJPanelFields;

    private Map<String, JComponent> addrJPanelFields;
    private JButton submitRegistrationButton;

    private JButton saveQrButton;

    private BufferedImage qrBufferedImage;

    private JButton clearQrButton;

    private JLabel qrImage;

    public CateringFacilityGUI(String title) throws ParseException {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(900, 600));
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.setResizable(false);

        this.mainPanel = new JTabbedPane();
        this.mainPanel.setBounds(0, 0, 900, 300);
        initRegistrationForm();
        initQrCodeForm();
        mainPanel.add("Catering Facility Registration", registrationParentPanel);
        mainPanel.add("QR Code Generator", generateQrParentPanel);

        this.add(mainPanel);

        mainPanel.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                switch (mainPanel.getSelectedIndex()) {
                    case 0:
                        mainPanel.setSize(new Dimension(900, 300));
                        break;
                    case 1:
                        mainPanel.setSize(new Dimension(900, 500));
                        break;
                }
            }
        });
    }

    private void initQrCodeForm(){
        this.generateQrPanel = new JPanel();
        this.generateQrParentPanel = new JPanel();

        JPanel generateFormPanel = new JPanel();

        generateFormPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        Border margin = new EmptyBorder(10,10,10,10);

        qrImage = new JLabel();

        saveQrButton = new JButton("Save QR Code");
        clearQrButton = new JButton("Clear QR");

        generateQrParentPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("QR Code Generator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(margin);

        generateQrParentPanel.add(titleLabel, BorderLayout.PAGE_START);
        generateQrParentPanel.add(generateQrPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 2, 10, 10));

        buttonsPanel.add(saveQrButton);
        buttonsPanel.add(clearQrButton);

        generateQrPanel.setLayout(new BorderLayout());
        generateQrPanel.setBorder(margin);
        generateQrPanel.add(qrImage, BorderLayout.CENTER);
        generateQrPanel.add(buttonsPanel, BorderLayout.PAGE_END);
        saveQrButton.setVisible(false);
        clearQrButton.setVisible(false);
        saveQrButton.addActionListener(this::saveQrButtonClicked);
        clearQrButton.addActionListener(this::clearQrButtonClicked);
    }

    private void generateQr(CateringFacility cateringFacility){
        try {
            qrBufferedImage = cateringFacility.requestQrCode();
            qrImage.setIcon(new ImageIcon(qrBufferedImage));
            saveQrButton.setVisible(true);
            clearQrButton.setVisible(true);
        } catch (RuntimeException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Server error: something went wrong");
        }
    }

    private void clearQr(){
        qrBufferedImage = null;
        saveQrButton.setVisible(false);
        clearQrButton.setVisible(false);
        qrImage.setIcon(null);
    }

    private void clearQrButtonClicked(java.awt.event.ActionEvent evt){
        clearQr();
    }

    private void saveQrButtonClicked(java.awt.event.ActionEvent evt){
        try {
            saveQrCode(qrBufferedImage);
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void saveQrCode(BufferedImage qrImage) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        int returnVal = fileChooser.showSaveDialog(null);
        if ( returnVal == JFileChooser.APPROVE_OPTION ){
            File file = fileChooser.getSelectedFile();
            if (!FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("jpg")) {
                file = new File(file.toString() + ".jpg");
                file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName())+".jpg");
            }
            ImageIO.write(qrImage, "jpg", file);
        }
    }

    private void initRegistrationForm(){
        this.registrationPanel = new JPanel();
        this.registrationParentPanel = new JPanel();

        Border margin = new EmptyBorder(10,10,10,10);

        regJPanelFields = new HashMap<>();
        addrJPanelFields = new HashMap<>();

        submitRegistrationButton = new JButton("Submit");
        submitRegistrationButton.setBorder(margin);

        registrationParentPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Catering Facility Registration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(margin);

        registrationParentPanel.add(titleLabel, BorderLayout.PAGE_START);
        registrationParentPanel.add(registrationPanel, BorderLayout.CENTER);
        registrationParentPanel.add(submitRegistrationButton, BorderLayout.PAGE_END);

        registrationPanel.setLayout(new GridLayout(1, 2, 10, 10));
        registrationPanel.setBorder(margin);

        addressJPanel = new JPanel();
        addressJPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Address Information"), margin));
        addressJPanel.setLayout(new GridLayout(4, 2, 10, 10));
        regJPanel = new JPanel();
        regJPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("General Information"), margin));
        regJPanel.setLayout(new GridLayout(4, 2, 10, 10));

        submitRegistrationButton.addActionListener(this::submitRegistrationButtonClicked);

        JLabel businessIdLabel = new JLabel("Business ID");
        JLabel nameLabel = new JLabel("Name");
        JLabel addrLabel = new JLabel("Address (street + nr)");
        JLabel cityLabel = new JLabel("City");
        JLabel countryLabel = new JLabel("Country");
        JLabel zipLabel = new JLabel("ZIP/Postal Code");
        JLabel phoneNumberLabel = new JLabel("Phone Number");

        JTextField businessIdTextField = new JTextField("");
        JTextField nameTextField = new JTextField("");
        JTextField addrTextField = new JTextField("");
        JTextField cityTextField = new JTextField("");
        JComboBox countrySelect= new JComboBox(getAllCountries());
        JTextField zipTextField = new JTextField("");
        JTextField phoneNumberTextField = new JTextField("");

        regJPanelFields.put("businessId", businessIdTextField);
        regJPanelFields.put("name", nameTextField);
        regJPanelFields.put("phoneNumber", phoneNumberTextField);

        addrJPanelFields.put("address", addrTextField);
        addrJPanelFields.put("city", cityTextField);
        addrJPanelFields.put("country", countrySelect);
        addrJPanelFields.put("zip", zipTextField);

        regJPanel.add(businessIdLabel);
        regJPanel.add(businessIdTextField);

        regJPanel.add(nameLabel);
        regJPanel.add(nameTextField);

        addressJPanel.add(addrLabel);
        addressJPanel.add(addrTextField);

        addressJPanel.add(cityLabel);
        addressJPanel.add(cityTextField);

        addressJPanel.add(countryLabel);
        addressJPanel.add(countrySelect);

        addressJPanel.add(zipLabel);
        addressJPanel.add(zipTextField);

        regJPanel.add(phoneNumberLabel);
        regJPanel.add(phoneNumberTextField);

        registrationPanel.add(regJPanel);
        registrationPanel.add(addressJPanel);
    }

    private void submitRegistrationButtonClicked(java.awt.event.ActionEvent evt){
        String businessId = regJPanelFields.get("businessId").getText();
        String name = regJPanelFields.get("name").getText();
        String phoneNumber = regJPanelFields.get("phoneNumber").getText();
        String address = ((JTextField)addrJPanelFields.get("address")).getText();
        String city = ((JTextField)addrJPanelFields.get("city")).getText();
        String country = ((JComboBox)addrJPanelFields.get("country")).getSelectedItem().toString();
        String zipCode = ((JTextField)addrJPanelFields.get("zip")).getText();
        if(businessId.equals("")){
            JOptionPane.showMessageDialog(this, "Please provide a valid Business ID");
        } else if(name.equals("")){
            JOptionPane.showMessageDialog(this, "Please provide a valid Name");
        } else if(phoneNumber.equals("")
                || !verifyPhoneNumber(phoneNumber)){
            JOptionPane.showMessageDialog(this, "Please provide a valid Phone Number");
        } else if(address.equals("")){
            JOptionPane.showMessageDialog(this, "Please provide a valid Address");
        } else if(city.equals("")){
            JOptionPane.showMessageDialog(this, "Please provide a valid City");
        } else if(!Arrays.toString(getAllCountries())
                .contains(country)){
            JOptionPane.showMessageDialog(this, "Please select a valid Country");
        } else if(zipCode.equals("")
                || !verifyStringNumber(zipCode)){
            JOptionPane.showMessageDialog(this, "Please provide a valid ZIP code");
        } else {
            try {
                String fullAddressString = String.format("%s, %s %s, %s", address, zipCode, city, country);
                CateringFacility cateringFacility = new CateringFacility(businessId, name, fullAddressString, phoneNumber, "localhost", 1099);
                mainPanel.setSelectedIndex(1);
                generateQr(cateringFacility);
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

    private boolean verifyStringNumber(String number){
        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher matcher = pattern.matcher(number);
        return matcher.matches();
    }

    private String[] getAllCountries() {
        String[] countries = new String[Locale.getISOCountries().length];
        String[] countryCodes = Locale.getISOCountries();
        for (int i = 0; i < countryCodes.length; i++) {
            Locale obj = new Locale("", countryCodes[i]);
            countries[i] = obj.getDisplayCountry();
        }
        return countries;
    }

    public static void main(String[] args) throws ParseException {
        JFrame frame = new CateringFacilityGUI("Catering Facility");
        frame.setVisible(true);
    }
}



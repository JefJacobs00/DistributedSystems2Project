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

    private Map<String, JTextField> genQrFields;
    private JButton submitRegistrationButton;

    private JButton saveQrButton;

    private BufferedImage qrBufferedImage;

    private JButton generateQrButton;

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

        genQrFields = new HashMap<>();

        JPanel generateFormPanel = new JPanel();

        generateFormPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        Border margin = new EmptyBorder(10,10,10,10);

        JLabel businessIdLabel = new JLabel("Bedrijfs-ID");
        JTextField businessIdTextField = new JTextField();

        genQrFields.put("businessId", businessIdTextField);

        businessIdTextField.setColumns( 20 );

        qrImage = new JLabel();

        saveQrButton = new JButton("QR code opslaan");
        clearQrButton = new JButton("Clear QR");

        generateQrButton = new JButton("Generate");
        generateQrButton.setMargin(new Insets(10, 10, 10, 10));

        generateQrParentPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("QR Code Generator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(margin);

        generateQrParentPanel.add(titleLabel, BorderLayout.PAGE_START);
        generateQrParentPanel.add(generateQrPanel, BorderLayout.CENTER);

        generateFormPanel.add(businessIdLabel);
        generateFormPanel.add(businessIdTextField);
        generateFormPanel.add(generateQrButton);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 2, 10, 10));

        buttonsPanel.add(saveQrButton);
        buttonsPanel.add(clearQrButton);

        generateQrPanel.setLayout(new BorderLayout());
        generateQrPanel.setBorder(margin);
        generateQrPanel.add(generateFormPanel, BorderLayout.PAGE_START);
        generateQrPanel.add(qrImage, BorderLayout.CENTER);
        generateQrPanel.add(buttonsPanel, BorderLayout.PAGE_END);
        saveQrButton.setVisible(false);
        clearQrButton.setVisible(false);
        saveQrButton.addActionListener(this::saveQrButtonClicked);
        generateQrButton.addActionListener(this::generateQrButtonClicked);
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
            JOptionPane.showMessageDialog(this, "Er is iets foutgelopen bij de server");
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

    private void generateQrButtonClicked(java.awt.event.ActionEvent evt){
        String businessId = regJPanelFields.get("businessId").getText();
        if(businessId.equals("")){
            JOptionPane.showMessageDialog(this, "Gelieve een geldig bedrijf ID op te geven");
        } else {
            // TODO - Find catering facility between all registered ones
            // cateringFacilities.find(o -> o.businessId = businessId)
            // TODO - Generate QR
            // generateQr();
        }
    }

    private void initRegistrationForm(){
        this.registrationPanel = new JPanel();
        this.registrationParentPanel = new JPanel();

        Border margin = new EmptyBorder(10,10,10,10);

        regJPanelFields = new HashMap<>();
        addrJPanelFields = new HashMap<>();

        submitRegistrationButton = new JButton("Submit");
        submitRegistrationButton.setMargin(new Insets(10, 10, 10, 10));

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
        addressJPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Adresinformatie"), margin));
        addressJPanel.setLayout(new GridLayout(4, 2, 10, 10));
        regJPanel = new JPanel();
        regJPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Algemene Informatie"), margin));
        regJPanel.setLayout(new GridLayout(4, 2, 10, 10));

        submitRegistrationButton.addActionListener(this::submitRegistrationButtonClicked);

        JLabel businessIdLabel = new JLabel("Bedrijfs-ID");
        JLabel nameLabel = new JLabel("Naam");
        JLabel addrLabel = new JLabel("Adres (straat & huisnummer)");
        JLabel cityLabel = new JLabel("Gemeente");
        JLabel countryLabel = new JLabel("Land");
        JLabel zipLabel = new JLabel("ZIP/Postcode");
        JLabel phoneNumberLabel = new JLabel("Telefoonnummer");

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
            JOptionPane.showMessageDialog(this, "Gelieve een geldig bedrijf ID op te geven");
        } else if(name.equals("")){
            JOptionPane.showMessageDialog(this, "Gelieve een geldige naam op te geven");
        } else if(phoneNumber.equals("")
                || !verifyPhoneNumber(phoneNumber)){
            JOptionPane.showMessageDialog(this, "Gelieve een geldig telefoonnummer op te geven");
        } else if(address.equals("")){
            JOptionPane.showMessageDialog(this, "Gelieve een geldig adres op te geven");
        } else if(city.equals("")){
            JOptionPane.showMessageDialog(this, "Gelieve een geldige gemeente op te geven");
        } else if(!Arrays.toString(getAllCountries())
                .contains(country)){
            JOptionPane.showMessageDialog(this, "Gelieve een geldig land te selecteren");
        } else if(zipCode.equals("")
                || !verifyStringNumber(zipCode)){
            JOptionPane.showMessageDialog(this, "Gelieve een geldige postcode op te geven");
        } else {
            try {
                String fullAddressString = String.format("%s, %s %s, %s", address, zipCode, city, country);
                CateringFacility cateringFacility = new CateringFacility(businessId, name, fullAddressString, phoneNumber, "localhost", 1099);
                mainPanel.setSelectedIndex(1);
                genQrFields.get("businessId").setText(cateringFacility.getBusinessId());
                generateQr(cateringFacility);
            } catch (RuntimeException ex){
                JOptionPane.showMessageDialog(this, "Er is iets foutgelopen bij de server");
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



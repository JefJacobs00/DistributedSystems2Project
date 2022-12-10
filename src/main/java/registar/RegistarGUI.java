package registar;

import Globals.SignedData;
import users.CateringFacility;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistarGUI extends JFrame{
    private JPanel mainPanel;

    private JTable facilitySynonymsPerDayTable;

    private JTable assignedUserTokensTable;

    private JTable cateringFacilitiesTable;

    private Registar registar;

    private DefaultTableModel facilitySynonymsPerDayTableModel;

    private DefaultTableModel assignedUserTokensTableModel;

    private DefaultTableModel cateringFacilitiesTableModel;

    public RegistarGUI(String title){
        super(title);

        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            registar = new Registar();
            registry.bind("Registar", registar);

            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setSize(new Dimension(1000, 600));
            this.setLocationRelativeTo(null);
            this.setLayout(null);
            this.setResizable(false);

            facilitySynonymsPerDayTableModel = new DefaultTableModel();
            assignedUserTokensTableModel = new DefaultTableModel();
            cateringFacilitiesTableModel = new DefaultTableModel();

            this.mainPanel = new JPanel();
            this.mainPanel.setBounds(0, 0, 1000, 400);
            this.facilitySynonymsPerDayTable = new JTable(facilitySynonymsPerDayTableModel);
            facilitySynonymsPerDayTableModel.addColumn("Datum");
            facilitySynonymsPerDayTableModel.addColumn("Facility Synonym");

            this.assignedUserTokensTable = new JTable(assignedUserTokensTableModel);
            assignedUserTokensTableModel.addColumn("Phone Number");

            this.cateringFacilitiesTable = new JTable(cateringFacilitiesTableModel);
            cateringFacilitiesTableModel.addColumn("Business ID");
            cateringFacilitiesTableModel.addColumn("Name");
            cateringFacilitiesTableModel.addColumn("Address");
            cateringFacilitiesTableModel.addColumn("Phone Number");

            refreshRows();

            Border margin = new EmptyBorder(10,10,10,10);
            facilitySynonymsPerDayTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        int row = facilitySynonymsPerDayTable.getSelectedRow();
                        int col = facilitySynonymsPerDayTable.getSelectedColumn();
                        String value = "";
                        if(col == 0){
                            DateTimeFormatter customFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                            value = registar.getFacilitySynonymPairs().get(row).getKey().format(customFormat);
                        } else if (col == 1){
                            value = registar.getFacilitySynonymPairs().get(row).getValue();
                        }
                        JTextArea ta = new JTextArea(value,20,40);
                        ta.setEditable(false);
                        ta.setLineWrap(true);
                        JOptionPane.showMessageDialog(RegistarGUI.this, ta);
                    }
                }
            });

            assignedUserTokensTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        int row = assignedUserTokensTable.getSelectedRow();
                        int col = assignedUserTokensTable.getSelectedColumn();
                        String value = "";
                        value = registar.getUserPhoneNumbers().get(row);
                        SignedData[] assignedTokensPerUser = registar.getAssignedTokensPerUser(value);
                        String[] shortenedAssignedTokens = new String[assignedTokensPerUser.length];
                        for(int i = 0; i < assignedTokensPerUser.length; i++){
                            shortenedAssignedTokens[i] = assignedTokensPerUser[i].toSpecialString();
                        }
                        JList userTokens = new JList(shortenedAssignedTokens);
                        userTokens.setEnabled(false);
                        JScrollPane userTokensScrollablePane = new JScrollPane(userTokens);
//                        JTextArea ta = new JTextArea(value,20,40);
//                        ta.setEditable(false);
//                        ta.setLineWrap(true);
                        JOptionPane.showMessageDialog(RegistarGUI.this, userTokensScrollablePane);
                    }
                }
            });

            cateringFacilitiesTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        int row = cateringFacilitiesTable.getSelectedRow();
                        int col = cateringFacilitiesTable.getSelectedColumn();
                        String value = "";
                        if(col == 0){
                            value = registar.getCateringFacilities().get(row).getBusinessId();
                        } else if (col == 1){
                            value = registar.getCateringFacilities().get(row).getName();
                        } else if (col == 2){
                            value = registar.getCateringFacilities().get(row).getAddress();
                        } else if (col == 3){
                            value = registar.getCateringFacilities().get(row).getPhoneNumber();
                        }
                        JTextArea ta = new JTextArea(value,20,40);
                        ta.setEditable(false);
                        ta.setLineWrap(true);
                        JOptionPane.showMessageDialog(RegistarGUI.this, ta);
                    }
                }
            });

            JScrollPane facilitySynonymsPerDayTableScroller = new JScrollPane(facilitySynonymsPerDayTable);
            JScrollPane assignedUserTokensTableScroller = new JScrollPane(assignedUserTokensTable);
            JScrollPane cateringFacilitiesTableScroller = new JScrollPane(cateringFacilitiesTable);

            JPanel tablesPanel = new JPanel();
            tablesPanel.setLayout(new GridLayout(1, 3, 10, 10));
            mainPanel.setLayout(new BorderLayout());

            JLabel titleLabel = new JLabel("Registar");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setBorder(margin);

            tablesPanel.add(facilitySynonymsPerDayTableScroller);
            tablesPanel.add(assignedUserTokensTableScroller);
            tablesPanel.add(cateringFacilitiesTableScroller);

            facilitySynonymsPerDayTableScroller.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Facility Synonyms"), margin));
            assignedUserTokensTableScroller.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Assigned User tokens"), margin));
            cateringFacilitiesTableScroller.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Catering Facilities"), margin));

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
        facilitySynonymsPerDayTableModel.setRowCount(0);
        for(String[] s : getFacilitySynonymsData(registar.getFacilitySynonymPairs())){
            facilitySynonymsPerDayTableModel.addRow(s);
        }

        assignedUserTokensTableModel.setRowCount(0);
        for(String[] s : getAssignedUserTokensData(registar.getUserPhoneNumbers())){
            assignedUserTokensTableModel.addRow(s);
        }
        try {
            cateringFacilitiesTableModel.setRowCount(0);
            for(String[] s : getCateringFacilitiesData(registar.getCateringFacilities())){
                cateringFacilitiesTableModel.addRow(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String[][] getFacilitySynonymsData(java.util.List<Map.Entry<LocalDate, String>> facilitySynonyms){
        DateTimeFormatter customFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String[][] data = new String[facilitySynonyms.size()][2];
        for (int i = 0; i < facilitySynonyms.size(); i++){
            data[i][0] = facilitySynonyms.get(i).getKey().format(customFormat);
            data[i][1] = facilitySynonyms.get(i).getValue();
        }
        return data;
    }

    private String[][] getAssignedUserTokensData(java.util.List<String> phoneNumbers){
        String[][] data = new String[phoneNumbers.size()][1];
        for (int i = 0; i < phoneNumbers.size(); i++){
            data[i][0] = phoneNumbers.get(i);
        }
        return data;
    }

    private String[][] getCateringFacilitiesData(java.util.List<CateringFacility> cateringFacilities){
        String[][] data = new String[cateringFacilities.size()][4];
        for (int i = 0; i < cateringFacilities.size(); i++){
            data[i][0] = cateringFacilities.get(i).getBusinessId();
            data[i][1] = cateringFacilities.get(i).getName();
            data[i][2] = cateringFacilities.get(i).getAddress();
            data[i][3] = cateringFacilities.get(i).getPhoneNumber();
        }
        return data;
    }


    public static void main(String[] args) throws ParseException {
        JFrame frame = new RegistarGUI("Registar GUI");
        frame.setVisible(true);
    }
}

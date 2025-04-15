package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class Transaction_History_Controller {
    private Stage stage;

    @FXML
    private TableColumn<PaymentRecord, String> account_no;

    @FXML
    private TableColumn<PaymentRecord, String> name;

    @FXML
    private TableColumn<PaymentRecord, String> company;

    @FXML
    private TableColumn<PaymentRecord, String> barangay;

    @FXML
    private TableColumn<PaymentRecord, String> charges;

    @FXML
    private TableColumn<PaymentRecord, Double> amount_paid;

    @FXML
    private TableColumn<PaymentRecord, String> or_number;

    @FXML
    private TableColumn<PaymentRecord, String> payment_date;

    @FXML
    private TableColumn<PaymentRecord, String> status;

    @FXML
    private TableView<PaymentRecord> tableView;

    @FXML
    private Button download_btn;

    @FXML
    private TextField searchField;

    private ObservableList<PaymentRecord> allPaymentRecords;

    @FXML
    public void initialize() {
        String css = this.getClass().getResource("/application/application.css").toExternalForm();
        tableView.getStylesheets().add(css);
        loadTableData();

        download_btn.setOnAction(event -> downloadAsCSV());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> search());
    }

    private void search() {
        String searchQuery = searchField.getText().toLowerCase();
        ObservableList<PaymentRecord> filteredList = FXCollections.observableArrayList();

        if (searchQuery.isEmpty()) {
            tableView.setItems(allPaymentRecords);
        } else {
            for (PaymentRecord record : allPaymentRecords) {
                if ((record.getAccountNo() != null && record.getAccountNo().toLowerCase().contains(searchQuery)) ||
                    (record.getName() != null && record.getName().toLowerCase().contains(searchQuery)) ||
                    (record.getCompany() != null && record.getCompany().toLowerCase().contains(searchQuery)) ||
                    (record.getBarangay() != null && record.getBarangay().toLowerCase().contains(searchQuery)) ||
                    (record.getCharges() != null && record.getCharges().toLowerCase().contains(searchQuery))) {
                        filteredList.add(record);
                }
            }

            tableView.setItems(filteredList);
        }
    }

    @FXML
    private void downloadAsCSV() {
        String userDesktop = System.getProperty("user.home") + "/Desktop";
        File recordsFolder = new File(userDesktop, "Records");

        if (!recordsFolder.exists() && !recordsFolder.mkdir()) {
            System.out.println("Failed to create Records folder.");
            return;
        }

        String baseFileName = "Transaction History";
        String extension = ".csv";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDate = dateFormat.format(new Date());

        File csvFile = new File(recordsFolder, baseFileName + extension);
        if (csvFile.exists()) {
            csvFile = new File(recordsFolder, baseFileName + "_" + currentDate + extension);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            for (TableColumn<PaymentRecord, ?> column : tableView.getColumns()) {
                pw.print(column.getText() + ",");
            }
            pw.println();

            for (PaymentRecord record : tableView.getItems()) {
                for (TableColumn<PaymentRecord, ?> column : tableView.getColumns()) {
                    Object value = column.getCellData(record);
                    pw.print(value == null ? "" : value + ",");
                }
                pw.println();
            }

            System.out.println("CSV file downloaded successfully to: " + csvFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error downloading CSV file: " + e.getMessage());
        }
    }

    @FXML
    public void loadTableData() {
        allPaymentRecords = FXCollections.observableArrayList();

        try {
            String query = "SELECT accountno_field, CONCAT(first_name, ' ', middle_name, ' ', last_name) AS name, company, barangay_field AS barangay, charges, date_applied, meter_fee, official_reciept " +
                           "FROM meter_connection";

            try (Connection conn = DatabaseConnector.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    PaymentRecord record = new PaymentRecord(
                        rs.getString("accountno_field"),
                        rs.getString("name"),
                        rs.getString("company"),
                        rs.getString("barangay"),
                        rs.getString("charges"),
                        rs.getDouble("meter_fee"),
                        rs.getString("date_applied"),
                        rs.getString("official_reciept"),
                        "Completed"
                    );
                    allPaymentRecords.add(record);
                }
            }

            account_no.setCellValueFactory(new PropertyValueFactory<>("accountNo"));
            name.setCellValueFactory(new PropertyValueFactory<>("name"));
            company.setCellValueFactory(new PropertyValueFactory<>("company"));
            barangay.setCellValueFactory(new PropertyValueFactory<>("barangay"));
            charges.setCellValueFactory(new PropertyValueFactory<>("charges"));
            amount_paid.setCellValueFactory(new PropertyValueFactory<>("meterFee"));
            or_number.setCellValueFactory(new PropertyValueFactory<>("orNumber"));
            payment_date.setCellValueFactory(new PropertyValueFactory<>("dateApplied"));
            status.setCellValueFactory(new PropertyValueFactory<>("status"));

            tableView.setItems(allPaymentRecords);
        } catch (SQLException e) {
            System.out.println("Error loading table data: " + e.getMessage());
        }
    }

    public class PaymentRecord {
        private String accountNo;
        private String name;
        private String company;
        private String barangay;
        private String charges;
        private double meterFee;
        private String dateApplied;
        private String orNumber;
        private String status;

        public PaymentRecord(String accountNo, String name, String company, String barangay, String charges, double meterFee, String dateApplied, String orNumber, String status) {
            this.accountNo = accountNo;
            this.name = name;
            this.company = company;
            this.barangay = barangay;
            this.charges = charges;
            this.meterFee = meterFee;
            this.dateApplied = dateApplied;
            this.orNumber = orNumber;
            this.status = status;
        }

        public String getAccountNo() {
            return accountNo;
        }

        public String getName() {
            return name;
        }

        public String getCompany() {
            return company;
        }

        public String getBarangay() {
            return barangay;
        }

        public String getCharges() {
            return charges == null ? "" : charges;
        }

        public double getMeterFee() {
            return meterFee;
        }

        public String getDateApplied() {
            return dateApplied;
        }

        public String getOrNumber() {
            return orNumber;
        }

        public String getStatus() {
            return status;
        }
    }

    public void navigation(String switchPage, ActionEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(newRoot);
        stage.setResizable(false);
    }

    public void back(ActionEvent event) throws IOException {
        navigation("Homepage", event);
    }
}

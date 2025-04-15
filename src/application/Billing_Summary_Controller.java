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
import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;



public class Billing_Summary_Controller {
    private Stage stage;

    @FXML
    private TableView<Billing_Summary_Model> tableview;

    @FXML
    private TableColumn<Billing_Summary_Model, String> account_no;

    @FXML
    private TableColumn<Billing_Summary_Model, String> name;

    @FXML
    private TableColumn<Billing_Summary_Model, String> company;

    @FXML
    private TableColumn<Billing_Summary_Model, String> barangay;

    @FXML
    private TableColumn<Billing_Summary_Model, String> Billing_period;

    @FXML
    private TableColumn<Billing_Summary_Model, String> billing_number;

    @FXML
    private TableColumn<Billing_Summary_Model, Double> payment;

    @FXML
    private TableColumn<Billing_Summary_Model, String> payment_date;
    
    @FXML
    private TableColumn<Billing_Summary_Model, String> status_column;
    
    @FXML
    private TableColumn<UnpaidAccount, String> unpaid_accountno;
    
    @FXML
    private TableColumn<UnpaidAccount, String> unpaid_balance;
    
    @FXML
    private TableColumn<UnpaidAccount, String> unpaid_totalmonth;
    
    @FXML
    private TableColumn<UnpaidAccount, String> unpaid_status;


    @FXML
    private TableColumn<UnpaidAccount2, String> unpaid_company;
    
    @FXML
    private TableColumn<UnpaidAccount2, String> unpaid_contactno;
    
    @FXML
    private TableColumn<UnpaidAccount2, String> unpaid_name;
    
    @FXML
    private TextField search_field;
    
    @FXML
    private Button download_btn, readingList, manualBtn;
    
    @FXML
    private HBox unpaid_container;
    
    @FXML
    private VBox barangay_button_vbox; // VBox for barangay buttons
    
    @FXML
    private ChoiceBox<String> barangay_choicebox;
    
    @FXML
    private TableView<UnpaidAccount> unpaid_tableview;

    private ObservableList<Billing_Summary_Model> data = FXCollections.observableArrayList();
    private ObservableList<String> barangayCache;
    
    private String code;

    @FXML
    public void initialize() {
        account_no.setCellValueFactory(new PropertyValueFactory<>("accountNo"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        company.setCellValueFactory(new PropertyValueFactory<>("company"));
        barangay.setCellValueFactory(new PropertyValueFactory<>("barangay"));
        Billing_period.setCellValueFactory(new PropertyValueFactory<>("billPeriod"));
        billing_number.setCellValueFactory(new PropertyValueFactory<>("billingNumber"));
        payment.setCellValueFactory(new PropertyValueFactory<>("payment"));
        payment_date.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        status_column.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        unpaid_accountno.setCellValueFactory(new PropertyValueFactory<>("accountNo"));
        unpaid_balance.setCellValueFactory(new PropertyValueFactory<>("outstandingBalance"));
        unpaid_company.setCellValueFactory(new PropertyValueFactory<>("company"));
        unpaid_contactno.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        unpaid_name.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        unpaid_name.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        unpaid_totalmonth.setCellValueFactory(new PropertyValueFactory<>("unpaidTotalMonth"));
        unpaid_status.setCellValueFactory(new PropertyValueFactory<>("unpaidStatus"));

        loadBarangayButtons(); // Load barangay buttons
        loadDataFromDatabase();
        loadBarangayChoices();

        // Add listeners for filtering
        search_field.textProperty().addListener((observable, oldValue, newValue) -> filterTableData(newValue));

    }
    
    @FXML 
    private void downloadCSV() {
    	if (unpaid_container.isVisible()) {
    		downloadUnpaidTable();
    		System.out.print("hehe");
    	}else if (tableview.isVisible()) {
    		downloadAsCSV();
    		System.out.print("hihi");
    	}
    }
    @FXML
    private void downloadUnpaidTable() {
        String userDesktop = System.getProperty("user.home") + "/Desktop";
        File recordsFolder = new File(userDesktop, "Records");

        // Create the Records folder if it doesn't exist
        if (!recordsFolder.exists() && !recordsFolder.mkdir()) {
            showAlert(AlertType.ERROR, "Error", "Failed to create the Records folder on the desktop.");
            return;
        }

        String baseFileName = "Unpaid_Billing_Summary ";
        String extension = ".csv";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDate = dateFormat.format(new Date());
        File csvFile = new File(recordsFolder, baseFileName + extension);

        // If the file already exists, append the current date to the filename
        if (csvFile.exists()) {
            csvFile = new File(recordsFolder, baseFileName + "_" + currentDate + extension);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            // Write headers (column names)
            StringBuilder headers = new StringBuilder();
            for (TableColumn<UnpaidAccount, ?> column : unpaid_tableview.getColumns()) {
                headers.append(column.getText()).append(",");
            }
            if (headers.length() > 0) headers.setLength(headers.length() - 1);  // Remove last comma
            pw.println(headers);

            // Write data rows
            for (UnpaidAccount record : unpaid_tableview.getItems()) {
                StringBuilder row = new StringBuilder();
                for (TableColumn<UnpaidAccount, ?> column : unpaid_tableview.getColumns()) {
                    Object value = column.getCellData(record);
                    row.append(value == null ? "" : value.toString().replaceAll(",", " ")).append(",");
                }
                if (row.length() > 0) row.setLength(row.length() - 1);  // Remove last comma
                pw.println(row);
            }

            // Show success alert
            showAlert(AlertType.INFORMATION, "Success", "CSV file downloaded successfully to:\n" + csvFile.getAbsolutePath());
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Error downloading CSV file: " + e.getMessage());
        }
    }
    
    private Map<String, Integer> fetchAccountCount() {
        Map<String, Integer> accountCountMap = new HashMap<>();

        String query = "SELECT account_no, COUNT(*) AS count " +
                       "FROM meter_readings " +
                       "WHERE status = 'Unpaid' AND account_no LIKE '" + code + "%' " +
                       "GROUP BY account_no";

        try (Connection connection = DatabaseConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String connectionNo = resultSet.getString("account_no");
                int count = resultSet.getInt("count");
                accountCountMap.put(connectionNo, count);
            }
        } catch (SQLException e) {
            System.out.println("SQLException occurred: " + e.getMessage()); // Print the exception message
            e.printStackTrace();
        }

        return accountCountMap;
    }
    
    private void fetchAccount() {
        System.out.println("Code: " + code); // Print the code value

        String query = "SELECT account_no, status, SUM(amount_payable) AS total_amount_payable " +
                       "FROM meter_readings " +
                       "WHERE status = 'Unpaid' AND " +
                       "account_no LIKE '" + code + "%' " +
                       "GROUP BY account_no, status";

        try (Connection connection = DatabaseConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            System.out.println("Query executed successfully"); // Print if query execution is successful

            // Clear the existing data in the TableView
            unpaid_tableview.getItems().clear();

            Map<String, Integer> accountCountMap = fetchAccountCount();

            while (resultSet.next()) {
                String connectionNo = resultSet.getString("account_no");
                double totalAmountPayable = resultSet.getDouble("total_amount_payable");

                // Create a new UnpaidAccount object
                UnpaidAccount unpaidAccount = new UnpaidAccount(connectionNo, totalAmountPayable);

                // Fetch account details
                fetchAccountDetails(connectionNo, unpaidAccount);

                // Set the unpaid_totalmonth value
                unpaidAccount.setUnpaidTotalMonth(accountCountMap.get(connectionNo));

                // Set the unpaid_status value
                int count = accountCountMap.get(connectionNo);
                if (count >= 12) {
                    unpaidAccount.setUnpaidStatus("Inactive");
                } else if (count > 2) {
                    unpaidAccount.setUnpaidStatus("Overdue");
                } else if (count == 1) {
                    unpaidAccount.setUnpaidStatus("Active");
                }

                // Add the UnpaidAccount object to the TableView
                unpaid_tableview.getItems().add(unpaidAccount);
            }
        } catch (SQLException e) {
            System.out.println("SQLException occurred: " + e.getMessage()); // Print the exception message
            e.printStackTrace();
        }
    }

    private void fetchAccountDetails(String connectionNo, UnpaidAccount unpaidAccount) {
        String query = "SELECT contact_number, company, first_name, last_name " +
                       "FROM meter_connection " +
                       "WHERE accountno_field = '" + connectionNo + "'";

        try (Connection connection = DatabaseConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String contactNumber = resultSet.getString("contact_number");
                String company = resultSet.getString("company");
                String fullName = resultSet.getString("first_name") + " " + resultSet.getString("last_name");

                // Update the UnpaidAccount object with the fetched details
                unpaidAccount.setContactNumber(contactNumber);
                unpaidAccount.setCompany(company);
                unpaidAccount.setFullName(fullName);
            }
        } catch (SQLException e) {
            System.out.println("SQLException occurred: " + e.getMessage()); // Print the exception message
            e.printStackTrace();
        }
    }

    // Method to dynamically load barangay buttons
    @FXML
    private void unpaid () {
    	readingList.setStyle("-fx-background-color: #2175a1;");
    	manualBtn.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #2175A1; -fx-border-radius: 10px; -fx-text-fill: #000000;");
    	unpaid_container.setVisible(true);
    	tableview.setVisible(false);
    }
    
    @FXML
    private void manual() {
    	manualBtn.setStyle("-fx-background-color: #2175a1;");
    	readingList.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #2175A1; -fx-border-radius: 10px; -fx-text-fill: #000000;");
    	unpaid_container.setVisible(false);
    	tableview.setVisible(true);
    }
   

    
    @FXML
    private void loadBarangayButtons() {
        try (Connection connection = DatabaseConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT barangay_name, brgy_code FROM barangay")) {

            // Clear the existing buttons in the VBox
            barangay_button_vbox.getChildren().clear();

            // Set alignment and padding for the VBox
            barangay_button_vbox.setAlignment(Pos.CENTER);
            barangay_button_vbox.setPadding(new Insets(5, 5, 5, 5));
            barangay_button_vbox.setSpacing(5);

            // Add a button for each barangay name and code
            while (resultSet.next()) {
                String barangayName = resultSet.getString("barangay_name");
                String brgyCode = resultSet.getString("brgy_code");

                // Combine barangay name and code into one string for button label
                String buttonLabel = barangayName + " (" + brgyCode + ")";

                // Create a button for each barangay name and code
                Button barangayButton = new Button(buttonLabel);
                barangayButton.setOnAction(event -> {
                    // Handle click event for each barangay button
                    filterTableByBarangay(barangayName);

                    // Add a custom action (for example, print the name and code of the clicked barangay)
                    System.out.println("Clicked on Barangay: " + barangayName + " (Code: " + brgyCode + ")");
                    code = brgyCode;
                    fetchAccount();
                });

                // Apply the rounded style from the CSS class
                barangayButton.setId("buttonRoundedStyle");
                barangayButton.setPrefWidth(210);
                barangayButton.setStyle("-fx-alignment: center; -fx-pref-height: 50;");

                // Add the button to the VBox
                barangay_button_vbox.getChildren().add(barangayButton);
            }

            // Add a "Show All" button to reset the filter
            Button showAllButton = new Button("Show All");
            showAllButton.setOnAction(event -> {
                // Handle click event for "Show All" button
                filterTableByBarangay("All");

                // Add a custom action (for example, print that "Show All" was clicked)
                System.out.println("Show All button clicked");
            });

            // Apply the rounded style from the CSS class
            showAllButton.getStyleClass().add("buttonRoundedStyle");

            // Make sure the "Show All" button matches the style and width
            showAllButton.setPrefWidth(210);
            showAllButton.setStyle("-fx-alignment: center; -fx-pref-height: 30;");

            // Add the "Show All" button to the VBox
            barangay_button_vbox.getChildren().add(showAllButton);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadBarangayChoices() {
        try (Connection connection = DatabaseConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT barangay_name FROM barangay")) {

            ObservableList<String> barangayNames = FXCollections.observableArrayList();
            
            // Add "All" as the first item
            barangayNames.add("All");
            
            // Add barangay names from the database
            while (resultSet.next()) {
                barangayNames.add(resultSet.getString("barangay_name"));
            }

            // Set the items in the ChoiceBox
            if (barangayCache == null || !barangayCache.equals(barangayNames)) {
                barangayCache = barangayNames;
                barangay_choicebox.setItems(barangayCache);
            }

            // Add a listener to print the selected barangay
            barangay_choicebox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            	filterTableView(newValue);
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

 // Method to filter the table by selected barangay
    private void filterTableView(String selectedBarangay) {
        if (selectedBarangay.equals("All")) {
            // When "All" is selected, show all data
            tableview.setItems(data);
        } else {
            // Filter the data based on the selected barangay
            FilteredList<Billing_Summary_Model> filteredData = new FilteredList<>(data,
                record -> selectedBarangay.equals(record.getBarangay()));
            tableview.setItems(filteredData);
        }
    }



    // Method to filter the table by selected barangay
    private void filterTableByBarangay(String selectedBarangay) {
        if (selectedBarangay.equals("All")) {
            // When "All" is selected, show all data
            tableview.setItems(data);
        } else {
            // Filter the data based on the selected barangay
            FilteredList<Billing_Summary_Model> filteredData = new FilteredList<>(data,
                record -> selectedBarangay.equals(record.getBarangay()));
            tableview.setItems(filteredData);
        }
    }

    // Filter data based on search field input
    private void filterTableData(String searchQuery) {
        String query = searchQuery.toLowerCase();
        FilteredList<Billing_Summary_Model> filteredList = new FilteredList<>(data, record ->
            (record.getAccountNo() != null && record.getAccountNo().toLowerCase().contains(query)) ||
            (record.getName() != null && record.getName().toLowerCase().contains(query)) ||
            (record.getCompany() != null && record.getCompany().toLowerCase().contains(query)) ||
            (record.getBarangay() != null && record.getBarangay().toLowerCase().contains(query)) ||
            (record.getBillPeriod() != null && record.getBillPeriod().toLowerCase().contains(query)) ||
            (record.getBillingNumber() != null && record.getBillingNumber().toLowerCase().contains(query)) ||
            (record.getPaymentDate() != null && record.getPaymentDate().toLowerCase().contains(query)));
        tableview.setItems(filteredList);
    }

    // Download data as CSV
    @FXML
    private void downloadAsCSV() {
        String userDesktop = System.getProperty("user.home") + "/Desktop";
        File recordsFolder = new File(userDesktop, "Records");

        if (!recordsFolder.exists() && !recordsFolder.mkdir()) {
            showAlert(AlertType.ERROR, "Error", "Failed to create the Records folder on the desktop.");
            return;
        }

        String baseFileName = "Billing Summary Paid Accounts ";
        String extension = ".csv";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDate = dateFormat.format(new Date());
        File csvFile = new File(recordsFolder, baseFileName + extension);

        if (csvFile.exists()) {
            csvFile = new File(recordsFolder, baseFileName + "_" + currentDate + extension);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            StringBuilder headers = new StringBuilder();
            for (TableColumn<Billing_Summary_Model, ?> column : tableview.getColumns()) {
                headers.append(column.getText()).append(",");
            }
            if (headers.length() > 0) headers.setLength(headers.length() - 1);
            pw.println(headers);

            for (Billing_Summary_Model record : tableview.getItems()) {
                StringBuilder row = new StringBuilder();
                for (TableColumn<Billing_Summary_Model, ?> column : tableview.getColumns()) {
                    Object value = column.getCellData(record);
                    row.append(value == null ? "" : value.toString().replaceAll(",", " ")).append(",");
                }
                if (row.length() > 0) row.setLength(row.length() - 1);
                pw.println(row);
            }

            showAlert(AlertType.INFORMATION, "Success", "CSV file downloaded successfully to:\n" + csvFile.getAbsolutePath());
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Error", "Error downloading CSV file: " + e.getMessage());
        }
    }

    // Show alert
    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Handle page navigation
    public void navigation(String switchPage, ActionEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(newRoot);
    }

    // Handle back navigation
    public void back(ActionEvent event) throws IOException {
        navigation("Homepage", event);
    }

    // Load data from database
    @FXML
    private void loadDataFromDatabase() {
        String query = "SELECT ph.account_no, ph.account_name, mc.connectiontype_cb AS company, mc.barangay_field AS barangay, " +
                       "ph.bill_period, ph.official_reciept AS billing_number, ph.payment_amount AS payment, ph.payment_date " +
                       "FROM payment_history ph INNER JOIN meter_connection mc ON ph.account_no = mc.accountno_field";

        try (Connection connection = DatabaseConnector.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                data.add(new Billing_Summary_Model(
                    resultSet.getString("account_no"),
                    resultSet.getString("account_name"),
                    resultSet.getString("company"),
                    resultSet.getString("barangay"),
                    resultSet.getString("bill_period"),
                    resultSet.getString("billing_number"),
                    resultSet.getDouble("payment"),
                    resultSet.getString("payment_date")
                ));
            }
            tableview.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    class AccountInfo {
        private String connectionNo;
        private double totalAmountPayable;

        public AccountInfo(String connectionNo, double totalAmountPayable) {
            this.connectionNo = connectionNo;
            this.totalAmountPayable = totalAmountPayable;
        }

        public String getConnectionNo() {
            return connectionNo;
        }

        public double getTotalAmountPayable() {
            return totalAmountPayable;
        }
    }
}

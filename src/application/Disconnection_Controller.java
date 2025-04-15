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
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class Disconnection_Controller {
	Stage stage;
	
	@FXML
	private TableView<ConnectionData> tableView;

	@FXML
	private TableColumn<ConnectionData, String> connection_no;

	@FXML
	private TableColumn<ConnectionData, String> meter_no;

	@FXML
	private TableColumn<ConnectionData, String> connection_type;

	@FXML
	private TableColumn<ConnectionData, String> company;

	@FXML
	private TableColumn<ConnectionData, String> first_name;

	@FXML
	private TableColumn<ConnectionData, String> middle_name;

	@FXML
	private TableColumn<ConnectionData, String> last_name;

	@FXML
	private TableColumn<ConnectionData, String> barangay;
	
	@FXML
	private TextField search_Field;
	
	private ObservableList<ConnectionData> connectionDataCache = FXCollections.observableArrayList();

	
	@FXML
	private void initialize() {
	    fetchConnectionData();
	    
	    search_Field.textProperty().addListener((observable, oldValue, newValue) -> searchConnectionData());
	}
	
	@FXML
	private void searchConnectionData() {
	    String searchString = search_Field.getText().toLowerCase();

	    ObservableList<ConnectionData> filteredData = FXCollections.observableArrayList();

	    for (ConnectionData connectionData : connectionDataCache) {
	        if (connectionData.getConnectionField().toLowerCase().contains(searchString) ||
	            connectionData.getFirstName().toLowerCase().contains(searchString) ||
	            connectionData.getLastName().toLowerCase().contains(searchString) ||
	            connectionData.getBarangay().toLowerCase().contains(searchString)) {
	            filteredData.add(connectionData);
	        }
	    }

	    tableView.setItems(filteredData);
	}
	
	private void fetchConnectionData() {
	    String query = "SELECT connection_field, meter_no, accountno_field, connectiontype_cb, company, first_name, middle_name, last_name, barangay_field FROM meter_connection WHERE disconnect = 't'";

	    try (Connection connection = DatabaseConnector.connect();
	         Statement statement = connection.createStatement();
	         ResultSet resultSet = statement.executeQuery(query)) {

	        connectionDataCache.clear();

	        while (resultSet.next()) {
	            String connectionField = resultSet.getString("connection_field");
	            String meterNo = resultSet.getString("meter_no");
	            String accountNoField = resultSet.getString("accountno_field");
	            String connectionType = resultSet.getString("connectiontype_cb");
	            String company = resultSet.getString("company");
	            String firstName = resultSet.getString("first_name");
	            String middleName = resultSet.getString("middle_name");
	            String lastName = resultSet.getString("last_name");
	            String barangay = resultSet.getString("barangay_field");

	            ConnectionData connectionData = new ConnectionData(connectionField, meterNo, accountNoField, connectionType, company, firstName, middleName, lastName, barangay);

	            connectionDataCache.add(connectionData);
	        }
	    } catch (SQLException e) {
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Error Fetching Connection Data");
	        alert.setHeaderText(null);
	        alert.setContentText("Error fetching connection data: " + e.getMessage());
	        alert.showAndWait();
	    }

	    tableView.setItems(connectionDataCache);

	    // Set the cell value factories for the TableColumn
	    connection_no.setCellValueFactory(new PropertyValueFactory<>("connectionField"));
	    meter_no.setCellValueFactory(new PropertyValueFactory<>("meterNo"));
	    connection_type.setCellValueFactory(new PropertyValueFactory<>("connectionType"));
	    company.setCellValueFactory(new PropertyValueFactory<>("company"));
	    first_name.setCellValueFactory(new PropertyValueFactory<>("firstName"));
	    middle_name.setCellValueFactory(new PropertyValueFactory<>("middleName"));
	    last_name.setCellValueFactory(new PropertyValueFactory<>("lastName"));
	    barangay.setCellValueFactory(new PropertyValueFactory<>("barangay"));
	}

	
    @FXML
    private void downloadAsCSV() {
        String userDesktop = System.getProperty("user.home") + "/Desktop";
        File recordsFolder = new File(userDesktop, "Records");

        if (!recordsFolder.exists() && !recordsFolder.mkdir()) {
            showAlert(AlertType.ERROR, "Error", "Failed to create the Records folder on the desktop.");
            return;
        }

        String baseFileName = "Discconnected Accounts ";
        String extension = ".csv";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDate = dateFormat.format(new Date());
        File csvFile = new File(recordsFolder, baseFileName + extension);

        if (csvFile.exists()) {
            csvFile = new File(recordsFolder, baseFileName + "_" + currentDate + extension);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            StringBuilder headers = new StringBuilder();
            for (TableColumn<ConnectionData, ?> column : tableView.getColumns()) {
                headers.append(column.getText()).append(",");
            }
            if (headers.length() > 0) headers.setLength(headers.length() - 1);
            pw.println(headers);

            for (ConnectionData record : tableView.getItems()) {
                StringBuilder row = new StringBuilder();
                for (TableColumn<ConnectionData, ?> column : tableView.getColumns()) {
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
	
    public void navigation(String switchPage, ActionEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(newRoot);
    }

    public void back(ActionEvent event) throws IOException {
        navigation("Homepage", event);
    }
    
    public class ConnectionData {
        private String connectionField;
        private String meterNo;
        private String accountNoField;
        private String connectionType;
        private String company;
        private String firstName;
        private String middleName;
        private String lastName;
        private String barangay;

        public ConnectionData(String connectionField, String meterNo, String accountNoField, String connectionType, String company, String firstName, String middleName, String lastName, String barangay) {
            this.connectionField = connectionField;
            this.meterNo = meterNo;
            this.accountNoField = accountNoField;
            this.connectionType = connectionType;
            this.company = company;
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.barangay = barangay;
        }

        // Getters and setters
        public String getConnectionField() {
            return connectionField;
        }

        public void setConnectionField(String connectionField) {
            this.connectionField = connectionField;
        }

        public String getMeterNo() {
            return meterNo;
        }

        public void setMeterNo(String meterNo) {
            this.meterNo = meterNo;
        }

        public String getAccountNoField() {
            return accountNoField;
        }

        public void setAccountNoField(String accountNoField) {
            this.accountNoField = accountNoField;
        }

        public String getConnectionType() {
            return connectionType;
        }

        public void setConnectionType(String connectionType) {
            this.connectionType = connectionType;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getBarangay() {
            return barangay;
        }

        public void setBarangay(String barangay) {
            this.barangay = barangay;
        }
    }
}

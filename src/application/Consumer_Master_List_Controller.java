package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;

public class Consumer_Master_List_Controller {
    Stage stage;

    @FXML
    private ChoiceBox<String> barangay_CB;

    @FXML
    private TableView<MeterConnection> tableView;

    @FXML
    private TableColumn<MeterConnection, String> account_no;

    @FXML
    private TableColumn<MeterConnection, String> name;

    @FXML
    private TableColumn<MeterConnection, String> barangay;

    @FXML
    private TableColumn<MeterConnection, String> contact_no;

    @FXML
    private TableColumn<MeterConnection, String> email;

    @FXML
    private TableColumn<MeterConnection, String> meter_no;

    @FXML
    private TableColumn<MeterConnection, String> date_applied;
    
    @FXML
    private TableColumn<MeterConnection, String> connection_type;
    
    @FXML TextField search_Field;

    private ObservableList<MeterConnection> allMeterConnections;

    @FXML
    public void initialize() {
        barangay_CB.setValue("All");

        fetchBarangay();
        allMeterConnections = fetchMeterConnections();
        tableView.setItems(allMeterConnections);

        account_no.setCellValueFactory(new PropertyValueFactory<>("accountNo"));
        name.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        barangay.setCellValueFactory(new PropertyValueFactory<>("barangay"));
        connection_type.setCellValueFactory(new PropertyValueFactory<>("connectionType_cb"));
        contact_no.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        meter_no.setCellValueFactory(new PropertyValueFactory<>("meterNo"));
        date_applied.setCellValueFactory(new PropertyValueFactory<>("dateApplied"));
        
        for (TableColumn<MeterConnection, ?> column : tableView.getColumns()) {
            column.setStyle("-fx-alignment: CENTER;");
        }
        
        search_Field.textProperty().addListener((observable, oldValue, newValue) -> {
            search();
        });
        
        barangay_CB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterByBarangay(newValue);
        });

    }
    
    @FXML
    private void exportToCSV(ActionEvent event) {
        try {
            String desktopPath = System.getProperty("user.home") + "/Desktop/";

            File recordsFolder = new File(desktopPath + "Records");
            if (!recordsFolder.exists()) {
                recordsFolder.mkdirs();
            }


            File file = new File(recordsFolder, "meter_connections.csv");
            FileWriter fileWriter = new FileWriter(file);

            String[] headers = {"Account No", "Full Name", "Barangay", "Connection Type", "Contact Number", "Email", "Meter No", "Date Applied"};
            for (String header : headers) {
                fileWriter.write(header + ",");
            }
            fileWriter.write("\n");

            // Write data rows
            for (MeterConnection meterConnection : tableView.getItems()) {
                fileWriter.write(meterConnection.getAccountNo() + ",");
                fileWriter.write(meterConnection.getFullName() + ",");
                fileWriter.write(meterConnection.getBarangay() + ",");
                fileWriter.write(meterConnection.getConnectionType_cb() + ",");
                fileWriter.write(meterConnection.getContactNumber() + ",");
                fileWriter.write(meterConnection.getEmail() + ",");
                fileWriter.write(meterConnection.getMeterNo() + ",");
                fileWriter.write(meterConnection.getDateApplied() + "\n");
            }

            fileWriter.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText("Meter connections exported to CSV file");
            alert.setContentText("File saved to: " + file.getAbsolutePath());
            alert.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText("Error exporting meter connections to CSV file");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    
    
    private void filterByBarangay(String barangay) {
        ObservableList<MeterConnection> filteredList = FXCollections.observableArrayList();

        if (barangay.equals("All")) {
            tableView.setItems(allMeterConnections);
        } else {
            for (MeterConnection meterConnection : allMeterConnections) {
                if (meterConnection.getBarangay().equals(barangay)) { // Use getBarangay() instead of getConnectionType()
                    filteredList.add(meterConnection);
                }
            }

            tableView.setItems(filteredList);
        }
    }


    private ObservableList<MeterConnection> fetchMeterConnections() {
        try {
            Connection conn = DatabaseConnector.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT accountno_field, (first_name || ' ' || middle_name || ' ' || last_name) AS full_name, "
                    + "barangay_field, connectiontype_cb, contact_number, email, meter_no, date_applied FROM meter_connection");

            ObservableList<MeterConnection> meterConnections = FXCollections.observableArrayList();
            while (rs.next()) {
                String accountNo = rs.getString("accountno_field");
                String fullName = rs.getString("full_name");
                String barangay = rs.getString("barangay_field"); 
                String connectionType = rs.getString("connectiontype_cb");
                String contactNumber = rs.getString("contact_number");
                String email = rs.getString("email");
                String meterNo = rs.getString("meter_no");
                String dateApplied = rs.getString("date_applied");

                meterConnections.add(new MeterConnection(accountNo, fullName, barangay, connectionType, contactNumber, email, meterNo, dateApplied));
            }

            conn.close();
            return meterConnections;
        } catch (SQLException e) {
            System.out.println("Error fetching meter connections: " + e.getMessage());
            return FXCollections.observableArrayList();
        }
    }

    @FXML
    private void search() {
        String searchQuery = search_Field.getText().toLowerCase();

        ObservableList<MeterConnection> filteredList = FXCollections.observableArrayList();

        if (searchQuery.isEmpty()) {
            tableView.setItems(allMeterConnections);
        } else {
            for (MeterConnection meterConnection : allMeterConnections) {
                if (meterConnection.getAccountNo().toLowerCase().contains(searchQuery) ||
                        meterConnection.getFullName().toLowerCase().contains(searchQuery)) {
                    filteredList.add(meterConnection);
                }
            }

            tableView.setItems(filteredList);
        }
    }

    private void fetchBarangay() {
        try {
            Connection conn = DatabaseConnector.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT barangay_name FROM barangay");

            ObservableList<String> barangayList = FXCollections.observableArrayList();
            while (rs.next()) {
                barangayList.add(rs.getString("barangay_name"));
            }

            barangayList.add(0, "All"); // Add "All" option at the beginning

            barangay_CB.setItems(barangayList);

            conn.close();
        } catch (SQLException e) {
            System.out.println("Error fetching barangay: " + e.getMessage());
        }
    }

    public void navigation(String switchPage, ActionEvent event) throws IOException {
        // Load the new FXML page
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));

        // Get the current stage from the event source
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.getScene().setRoot(newRoot); // Set the root of the scene first
        stage.setResizable(false);
        stage.setResizable(true);
        stage.setResizable(false);
    }

    public void back(ActionEvent event) throws IOException {
        navigation("Homepage", event);
    }
    
    //-------method
    public class MeterConnection {
    	private String accountNo;
    	private String fullName;
    	private String barangay;
    	private String connectionType_cb;
    	private String contactNumber;
    	private String email;
    	private String meterNo;
    	private String dateApplied;

    	public MeterConnection(String accountNo, String fullName, String barangay, String connectionType_cb, String contactNumber, String email, String meterNo, String dateApplied) {
    	    this.accountNo = accountNo;
    	    this.fullName = fullName;
    	    this.barangay = barangay;
    	    this.connectionType_cb = connectionType_cb;
    	    this.contactNumber = contactNumber;
    	    this.email = email;
    	    this.meterNo = meterNo;
    	    this.dateApplied = dateApplied;
    	}

    	public String getAccountNo() {
    	    return accountNo;
    	}

    	public String getFullName() {
    	    return fullName;
    	}

    	public String getBarangay() { 
    	    return barangay;
    	}
    	
        public String getConnectionType_cb() {
            return connectionType_cb;
        }

    	public String getContactNumber() {
    	    return contactNumber;
    	}

    	public String getEmail() {
    	    return email;
    	}

    	public String getMeterNo() {
    	    return meterNo;
    	}

    	public String getDateApplied() {
    	    return dateApplied;
    	}
    }
}


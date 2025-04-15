package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Connection_Controller {
    private Stage stage;

    // UI components
    @FXML
    private ChoiceBox<String> connectionTypCB;
    @FXML
    private TextField connection_field, houseNo_field, barangayCode_field, barangay_field;
    @FXML
    private TextField accountNo_field, firstName_field, lastName_field, middleName_field, company_field, contactNumber_field;
    @FXML
    private TextField meterNumber_field, meterBrand_field, meterFee_field, meterfee1_field;
    @FXML
    private DatePicker dateApplied_calendar, dateInstalled_calendar;
    @FXML
    private Button search_account, editBtn, closeOtherCharges_Btn, add_Btn, clear_btn, Undo_Btn;
    @FXML
    private TextField accountNumber, othercharges_field, totalAmount_Field, official_reciept;
    
    @FXML
    private ChoiceBox<String> particular_CB;
    
    @FXML
    private StackPane otherCharges_SP, confim_SP, brgySelect_SP;
    
    @FXML
    private Text connectionFee_Text, inspectionFee_Text, otherFee_Text, saddleClamp_Text, miscellaneous_Text, meterFee_Text;
    
    @FXML TextField amountOtherCharges_Field;
    
    @FXML Text textpayment;
    
    @FXML
    private TableView<Barangay> brgy_tbl;
    @FXML
    private TableColumn<Barangay, String> code_Column;
    @FXML
    private TableColumn<Barangay, String> barangay_Column;

    private Connection connection;
    
    //-------------------
    private boolean update = true;
    
    private int highestConnectionFieldValue = getHighestConnectionFieldValue();
    private int getconnectionno = getconnectionno();
    
    private boolean isPaymentOk = false;
    
    private String other;
    
    String amount;
    
    private Stack<Integer> undoStack = new Stack<>();
    //--------------------
    @FXML
    public void initialize() {
        connectionTypCB.setItems(FXCollections.observableArrayList("Residential", "Commercial"));
        connectionTypCB.setValue("Residential");
        
        connectionTypCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                checkConnectionType();
            }
        });

        connection_field.setText(String.valueOf(highestConnectionFieldValue + 1));
        accountNo_field.setText(String.valueOf(getconnectionno + 1));

        particular_CB.setItems(FXCollections.observableArrayList(
            "Connection Fee", "Inspection Fee", "Others/Materials", "Saddle Clamp", "Miscellaneous"
        ));
        particular_CB.setValue("Connection Fee");
        
        fetchAllBrgy();
        
        System.out.print(highestConnectionFieldValue);
    }
    
    @FXML
    public void clearFields() {
        connection_field.setText(String.valueOf(highestConnectionFieldValue + 1));
        accountNo_field.setText(String.valueOf(getconnectionno + 1));
        houseNo_field.clear();
        barangayCode_field.clear();
        barangay_field.clear();
        firstName_field.clear();
        lastName_field.clear();
        middleName_field.clear();
        company_field.clear();
        contactNumber_field.clear();
        meterNumber_field.clear();
        meterBrand_field.clear();
        meterFee_field.clear();
        dateApplied_calendar.setValue(null);
        dateInstalled_calendar.setValue(null);
        
        update = true;
        editBtn.setText("Edit");
        search_account.setVisible(false);
        accountNumber.setVisible(false);
        
    }
    private void fetchAllBrgy() {
    	// Set up columns
        code_Column.setCellValueFactory(cellData -> cellData.getValue().brgyCodeProperty());
        barangay_Column.setCellValueFactory(cellData -> cellData.getValue().barangayNameProperty());

        // Fetch data from database
        connection = DatabaseConnector.connect();  // Assuming you have a connection method in DatabaseConnector
        ObservableList<Barangay> barangayList = FXCollections.observableArrayList();

        ResultSet rs = DatabaseConnector.getAllBarangays(connection);
        try {
            while (rs.next()) {
                String barangayName = rs.getString("barangay_name");
                String brgyCode = rs.getString("brgy_code");
                barangayList.add(new Barangay(barangayName, brgyCode));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set data to TableView
        brgy_tbl.setItems(barangayList);

        // Add a listener to handle row selection
        brgy_tbl.setOnMouseClicked(event -> printSelectedRow());
	    }
	
	    // Method to print the details of the selected row
	    private void printSelectedRow() {
	        Barangay selectedBarangay = brgy_tbl.getSelectionModel().getSelectedItem();
	        if (selectedBarangay != null) {
	            
	            barangayCode_field.setText(selectedBarangay.getBrgyCode());
	            accountNo_field.setText(String.valueOf(barangayCode_field.getText() + String.valueOf(accountNo_field.getText())));
	            barangay_field.setText(selectedBarangay.getBarangayName());
	            
	            closebrgySelect();
	        }
	    
    
    }

    
    public void fetchBasicCharges() {
        String selectSQL = "SELECT * FROM residential_charges ORDER BY created_at DESC LIMIT 1";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement pstmt = connection.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                // Set the values in the text fields for residential charges
            	String fee = String.valueOf(rs.getLong("meter_fee"));
            	meterFee_field.setText(fee);
            	meterfee1_field.setText(fee);
            	meterFee_Text.setText(fee);
            	totalAmount_Field.setText(fee);
            }
        } catch (SQLException e) {
            System.out.println("Failed to load latest residential charges: " + e.getMessage());
        }
        
    }
    
    public void checkConnectionType() {
        String selectedValue = connectionTypCB.getValue();

        if ("Commercial".equals(selectedValue)) {
            String selectSQL = "SELECT * FROM commercial_charges ORDER BY created_at DESC LIMIT 1";

            try (Connection connection = DatabaseConnector.connect();
                 PreparedStatement pstmt = connection.prepareStatement(selectSQL);
                 ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                	connectionFee_Text.setText(String.valueOf(rs.getLong("connection_fee")));
                	amountOtherCharges_Field.setText(String.valueOf(rs.getLong("connection_fee")));
                }
            } catch (SQLException e) {
                System.out.println("Failed to load commercial charges: " + e.getMessage());
            }
        } else if ("Residential".equals(selectedValue)) {
            String selectSQL = "SELECT * FROM residential_charges ORDER BY created_at DESC LIMIT 1";

            try (Connection connection = DatabaseConnector.connect();
                 PreparedStatement pstmt = connection.prepareStatement(selectSQL);
                 ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                	connectionFee_Text.setText(String.valueOf(rs.getLong("connection_fee")));
                	amountOtherCharges_Field.setText(String.valueOf(rs.getLong("connection_fee")));
                }
            } catch (SQLException e) {
                System.out.println("Failed to load residential charges: " + e.getMessage());
            }
        }
    }



        
	
	public void navigation(String switchPage, ActionEvent event) throws IOException {
        // Load the new FXML page
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));
        
        // Get the current stage from the event source
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        stage.setResizable(false);
        stage.setResizable(true);
        stage.setResizable(false);
        stage.getScene().setRoot(newRoot);
    }
	
	public void confimSP() {
		confim_SP.setVisible(true);
	}
	
	public void clearCharge(ActionEvent event) {
		connectionFee_Text.setText("0");
		inspectionFee_Text.setText("0");
		otherFee_Text.setText("0");
		saddleClamp_Text.setText("0");
		miscellaneous_Text.setText("0");
	}
	
    public void addCharge(ActionEvent event) throws IOException {
        String selectedParticular = particular_CB.getValue();
        amount = amountOtherCharges_Field.getText();

        int totalFee = Integer.parseInt(othercharges_field.getText().isEmpty() ? "0" : othercharges_field.getText()); // Initialize totalFee from the current value in othercharges_field or 0 if empty
        String totalFeeText;

        switch (selectedParticular) {
            case "Connection Fee":
                connectionFee_Text.setText(amount);
                totalFee += Integer.parseInt(amount);  // Accumulate the amount to totalFee
                undoStack.push(1);  // Push identifier onto the stack
                other = "Connection Fee ";
                break;

            case "Inspection Fee":
                inspectionFee_Text.setText(amount);
                totalFee += Integer.parseInt(amount);  // Accumulate the amount to totalFee
                undoStack.push(2);  // Push identifier onto the stack
                other += "Inspection Fee ";
                break;

            case "Others/Materials":
                otherFee_Text.setText(amount);
                totalFee += Integer.parseInt(amount);  // Accumulate the amount to totalFee
                undoStack.push(3);  // Push identifier onto the stack
                other += "Others/Materials ";
                break;

            case "Saddle Clamp":
                saddleClamp_Text.setText(amount);
                totalFee += Integer.parseInt(amount);  // Accumulate the amount to totalFee
                undoStack.push(4);  // Push identifier onto the stack
                other += "Saddle Clamp ";
                break;

            case "Miscellaneous":
                miscellaneous_Text.setText(amount);
                totalFee += Integer.parseInt(amount);  // Accumulate the amount to totalFee
                undoStack.push(5);  // Push identifier onto the stack
                other += "Miscellaneous ";
                break;
        }

        // Update the othercharges_field with the final totalFee
        totalFeeText = String.valueOf(totalFee);
        othercharges_field.setText(totalFeeText);

        // Clear the input field after adding
        amountOtherCharges_Field.setText(null);
    }

    public void undoCharge(ActionEvent event) {
        if (undoStack.isEmpty()) {
            // No operations to undo
            return;
        }

        // Get the last added item
        int lastOperation = undoStack.pop(); // Pop the last added operation identifier

        // Ensure that othercharges_field is not null before parsing its value
        String otherChargesText = othercharges_field.getText();
        otherChargesText = (otherChargesText == null) ? "0" : otherChargesText; // Default to "0" if null
        int totalFee = Integer.parseInt(otherChargesText); // Current total in othercharges_field

        // Deduct the appropriate charge
        switch (lastOperation) {
            case 1:  // Connection Fee
                totalFee -= Integer.parseInt(connectionFee_Text.getText());
                connectionFee_Text.setText("0");
                break;

            case 2:  // Inspection Fee
                totalFee -= Integer.parseInt(inspectionFee_Text.getText());
                inspectionFee_Text.setText("0");
                break;

            case 3:  // Others/Materials
                totalFee -= Integer.parseInt(otherFee_Text.getText());
                otherFee_Text.setText("0");
                break;

            case 4:  // Saddle Clamp
                totalFee -= Integer.parseInt(saddleClamp_Text.getText());
                saddleClamp_Text.setText("0");
                break;

            case 5:  // Miscellaneous
                totalFee -= Integer.parseInt(miscellaneous_Text.getText());
                miscellaneous_Text.setText("0");
                break;
        }

        // Update the othercharges_field with the new total
        String totalFeeText = String.valueOf(totalFee);
        othercharges_field.setText(totalFeeText);
    }
	
	public void back(ActionEvent event) throws IOException {
		navigation("Homepage", event);
	}
	
	public void add(ActionEvent event) {
		otherCharges_SP.setVisible(true);
		
		
	}
	
	public void closeOtherCharges(ActionEvent event) {
		otherCharges_SP.setVisible(false);
		
		int total = Integer.parseInt(meterfee1_field.getText()) + Integer.parseInt(othercharges_field.getText());
		String totalText = String.valueOf(total);
		
		totalAmount_Field.setText(totalText);
	}
	
	private int getHighestValue(String query) {
	    int highestValue = 0;

	    try (Connection conn = DatabaseConnector.connect()) {
	        if (conn != null) {
	            try (PreparedStatement stmt = conn.prepareStatement(query)) {
	                ResultSet rs = stmt.executeQuery();

	                if (rs.next()) {
	                    highestValue = rs.getInt(1);
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        showAlert(AlertType.ERROR, "Database Error", "An error occurred while fetching the highest value.");
	    }

	    return highestValue;
	}

	private int getHighestConnectionFieldValue() {
	    String query = "SELECT MAX(CAST(connection_field AS INTEGER)) AS highest_value FROM meter_connection";
	    return getHighestValue(query);
	}


	private int getconnectionno() {
	    String query = "SELECT MAX(CAST(REGEXP_REPLACE(accountno_field, '\\D', '', 'g') AS INT)) AS highest_accountno FROM meter_connection";
	    System.out.print(getHighestValue(query));
	    return getHighestValue(query);
	}
    
    @FXML
    private void edit() {
        if (update) {
            accountNumber.setVisible(true);
            search_account.setVisible(true);
        } else {
            // Get the updated values from the fields
            String connectionFieldText = connection_field.getText();
            String houseNoFieldText = houseNo_field.getText();
            String barangayCodeFieldText = barangayCode_field.getText();
            String barangay = barangay_field.getText();
            String accountNoFieldText = accountNo_field.getText();
            String firstName = firstName_field.getText();
            String lastName = lastName_field.getText();
            String middleName = middleName_field.getText();
            String company = company_field.getText();
            String contactNumberFieldText = contactNumber_field.getText();
            String meterNumberFieldText = meterNumber_field.getText();
            String meterBrand = meterBrand_field.getText();
            String meterFeeFieldText = meterFee_field.getText();
            String dateApplied = (dateApplied_calendar.getValue() != null) ? dateApplied_calendar.getValue().toString() : null;
            String dateInstalled = (dateInstalled_calendar.getValue() != null) ? dateInstalled_calendar.getValue().toString() : null;

            // Convert string dates to java.sql.Date if not null
            java.sql.Date sqlDateApplied = (dateApplied != null) ? java.sql.Date.valueOf(dateApplied) : null;
            java.sql.Date sqlDateInstalled = (dateInstalled != null) ? java.sql.Date.valueOf(dateInstalled) : null;

            // Validate if required fields are not empty
            if (connectionFieldText.isEmpty() || houseNoFieldText.isEmpty() || barangayCodeFieldText.isEmpty() || barangay.isEmpty() ||
                accountNoFieldText.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || middleName.isEmpty() || company.isEmpty() ||
                contactNumberFieldText.isEmpty() || meterNumberFieldText.isEmpty() || meterBrand.isEmpty() || meterFeeFieldText.isEmpty() ||
                sqlDateApplied == null || sqlDateInstalled == null) {

                showAlert(AlertType.WARNING, "Input Error", "Please fill in all required fields.");
                return; // Exit method if fields are empty
            }

            // Initialize integer fields and their valid flag
            int houseNo = 0, accountNo = 0, contactNumber = 0, meterNo = 0, meterFee = 0;
			String barangayCode = null;
            boolean validIntegerFields = true;

            // Validate and parse integer fields
            try {
                // Only parse if input is valid
                if (!houseNoFieldText.isEmpty() && isInteger(houseNoFieldText)) {
                    houseNo = Integer.parseInt(houseNoFieldText);
                } else {
                    validIntegerFields = false;
                    showAlert(AlertType.WARNING, "Invalid Input", "House No should be a valid number.");
                    return;
                }

                if (!accountNoFieldText.isEmpty() && isInteger(accountNoFieldText)) {
                    accountNo = Integer.parseInt(accountNoFieldText);
                } else {
                    validIntegerFields = false;
                    showAlert(AlertType.WARNING, "Invalid Input", "Account No should be a valid number.");
                    return;
                }

                if (!contactNumberFieldText.isEmpty() && isInteger(contactNumberFieldText)) {
                    contactNumber = Integer.parseInt(contactNumberFieldText);
                } else {
                    validIntegerFields = false;
                    showAlert(AlertType.WARNING, "Invalid Input", "Contact Number should be a valid number.");
                    return;
                }

                if (!meterNumberFieldText.isEmpty() && isInteger(meterNumberFieldText)) {
                    meterNo = Integer.parseInt(meterNumberFieldText);
                } else {
                    validIntegerFields = false;
                    showAlert(AlertType.WARNING, "Invalid Input", "Meter Number should be a valid number.");
                    return;
                }

                if (!meterFeeFieldText.isEmpty() && isInteger(meterFeeFieldText)) {
                    meterFee = Integer.parseInt(meterFeeFieldText);
                } else {
                    validIntegerFields = false;
                    showAlert(AlertType.WARNING, "Invalid Input", "Meter Fee should be a valid number.");
                    return;
                }
            } catch (NumberFormatException e) {
                validIntegerFields = false;
                System.out.println("Error parsing integers: " + e.getMessage());
            }

            // Show error message if any integer fields are invalid
            if (!validIntegerFields) {
                showAlert(AlertType.WARNING, "Input Error", "Please enter valid numbers for fields that require integers.");
                return; // Exit method if integer fields are invalid
            }

            // Continue with database update
            try (Connection conn = DatabaseConnector.connect()) {
                String updateQuery = "UPDATE meter_connection SET " +
                    "connection_field = ?, house_no = ?, barangay_code = ?, barangay_field = ?, accountno_field = ?, " +
                    "first_name = ?, last_name = ?, middle_name = ?, company = ?, contact_number = ?, meter_no = ?, " +
                    "meter_brand = ?, meter_fee = ?, date_applied = ?, date_installed = ? " +
                    "WHERE connection_field = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                    pstmt.setString(1, connectionFieldText);
                    pstmt.setInt(2, houseNo);
                    pstmt.setString(3, barangayCode); 
                    pstmt.setString(4, barangay);
                    pstmt.setInt(5, accountNo);
                    pstmt.setString(6, firstName);
                    pstmt.setString(7, lastName);
                    pstmt.setString(8, middleName);
                    pstmt.setString(9, company);
                    pstmt.setInt(10, contactNumber);
                    pstmt.setInt(11, meterNo);
                    pstmt.setString(12, meterBrand);
                    pstmt.setInt(13, meterFee);

                    // Use java.sql.Date for date fields
                    pstmt.setDate(14, sqlDateApplied);
                    pstmt.setDate(15, sqlDateInstalled);

                    pstmt.setString(16, connectionFieldText);

                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        showAlert(AlertType.INFORMATION, "Success", "Record updated successfully.");
                        accountNumber.setVisible(false);
                        search_account.setVisible(false);
                        editBtn.setText("Edit");
                        update = true;
                        
                        connection_field.setText("");
                        connectionTypCB.setValue("Residential");
                        barangay_field.setText("");
                        accountNo_field.setText("");
                        firstName_field.setText("");
                        lastName_field.setText("");
                        middleName_field.setText("");
                        houseNo_field.setText("");
                        company_field.setText("");
                        contactNumber_field.setText("");
                        meterNumber_field.setText("");
                        meterBrand_field.setText("");
                        meterFee_field.setText("");
                        connection_field.setText(String.valueOf(highestConnectionFieldValue + 1));
                        
                    } else {
                        showAlert(AlertType.INFORMATION, "Not Found", "No record found with the given connection field.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Database Error", "An error occurred while updating the record.");
            }
        }
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    
    @FXML
    private void searchAccount() throws SQLException {
        String accountNo = accountNumber.getText();

        // Check if account number is provided
        if (accountNo.isEmpty()) {
            showAlert(AlertType.WARNING, "Input Error", "Please enter an account number.");
            return;
        }

        // Query to retrieve account details
        String query = "SELECT m.connection_field, m.connectionType_Cb, m.barangay_field, m.accountno_field, " +
                       "m.first_name, m.last_name, m.middle_name, m.house_no, m.company, m.barangay_code, m.contact_number, " +
                       "m.meter_no, m.meter_brand, m.meter_fee, m.date_applied, m.date_installed, b.brgy_code " +
                       "FROM meter_connection m " +
                       "JOIN barangay b ON CAST(m.barangay_field AS TEXT) = b.barangay_name " + // Cast barangay_field if needed
                       "WHERE CAST(m.accountno_field AS TEXT) = ?"; // Cast accountno_field if it's an integer

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, accountNo);
            ResultSet rs = pstmt.executeQuery();

            // Check if the result is not empty
            if (rs.next()) {
                // Set the retrieved values into the respective TextFields and other UI elements
                connection_field.setText(rs.getString("connection_field"));
                connectionTypCB.setValue(rs.getString("connectionType_Cb")); // Assuming it's a ComboBox
                barangay_field.setText(rs.getString("barangay_field"));
                accountNo_field.setText(rs.getString("accountno_field"));
                firstName_field.setText(rs.getString("first_name"));
                lastName_field.setText(rs.getString("last_name"));
                middleName_field.setText(rs.getString("middle_name"));
                houseNo_field.setText(rs.getString("house_no"));
                company_field.setText(rs.getString("company"));
                contactNumber_field.setText(rs.getString("contact_number"));
                meterNumber_field.setText(rs.getString("meter_no"));
                meterBrand_field.setText(rs.getString("meter_brand")); 
                meterFee_field.setText(rs.getString("meter_fee"));

                // Set the DatePickers
                dateApplied_calendar.setValue(rs.getDate("date_applied").toLocalDate());
                dateInstalled_calendar.setValue(rs.getDate("date_installed").toLocalDate());

                // Set the barangay code from the joined query
                barangayCode_field.setText(rs.getString("brgy_code"));
                
                editBtn.setText("Update");
                update = false;
                
            } else {
                showAlert(AlertType.INFORMATION, "Not Found", "No account found for the given account number.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "An error occurred while fetching the account details.");
        }
    }
    
    


    @FXML
    private void searchCode() {
    	
    	brgySelect_SP.setVisible(true);
    	
		/*
		 * String barangayCode = barangayCode_field.getText();
		 * 
		 * if (barangayCode.isEmpty()) { showAlert(AlertType.WARNING, "Input Error",
		 * "Please enter a Barangay Code."); return; }
		 * 
		 * try (Connection conn = DatabaseConnector.connect()) { String barangayName =
		 * DatabaseConnector.getBarangayCode(conn, barangayCode);
		 * 
		 * if (barangayName != null) { barangay_field.setText(barangayName); } else {
		 * showAlert(AlertType.INFORMATION, "Not Found",
		 * "No Barangay found for the given code."); } } catch (SQLException e) {
		 * e.printStackTrace(); showAlert(AlertType.ERROR, "Database Error",
		 * "An error occurred while fetching the barangay details."); }
		 */
    }
    
    @FXML
    private void closebrgySelect () {
    	brgySelect_SP.setVisible(false);

    	
    }


    @FXML
    private void insertDataIntoDatabase() {
    	
   	 textpayment.setText("The payment of " + totalAmount_Field.getText() + " for " + lastName_field.getText() + " (Account No. " + accountNo_field.getText() + ") for the charges. Please confirm to proceed.");
        connection_field.getText();
        houseNo_field.getText();
        barangayCode_field.getText();
        barangay_field.getText();
        accountNo_field.getText();
        firstName_field.getText();
        lastName_field.getText();
        middleName_field.getText();
        company_field.getText();
        contactNumber_field.getText();
        meterNumber_field.getText();
        meterBrand_field.getText();
        meterFee_field.getText();
        String dateApplied = (dateApplied_calendar.getValue() != null) ? dateApplied_calendar.getValue().toString() : null;
        String dateInstalled = (dateInstalled_calendar.getValue() != null) ? dateInstalled_calendar.getValue().toString() : null;

        if (dateApplied != null && !dateApplied.isEmpty()) {
            java.sql.Date.valueOf(dateApplied);
        }

        if (dateInstalled != null && !dateInstalled.isEmpty()) {
            java.sql.Date.valueOf(dateInstalled);
        }

        confim_SP.setVisible(true);
    }

    public void showPaymentConfirmation(ActionEvent event) {
        confim_SP.setVisible(true);
    }
   
    public void confirmPayment(ActionEvent event) {
        confim_SP.setVisible(false);
        isPaymentOk = true;
        System.out.println(isPaymentOk);
        

        if (isPaymentOk) {
            String connectionFieldText = connection_field.getText();
            String houseNoFieldText = houseNo_field.getText();
            String barangayCodeFieldText = barangayCode_field.getText();
            String barangay = barangay_field.getText();
            String accountNoFieldText = accountNo_field.getText();
            String firstName = firstName_field.getText();
            String lastName = lastName_field.getText();
            String middleName = middleName_field.getText();
            String company = company_field.getText();
            String contactNumberFieldText = contactNumber_field.getText().replaceFirst("^0+", "");
            long contactNumber = Long.parseLong(contactNumberFieldText);
            String meterNumberFieldText = meterNumber_field.getText();
            String meterBrand = meterBrand_field.getText();
            String meterFeeFieldText = totalAmount_Field.getText();
            String dateApplied = (dateApplied_calendar.getValue() != null) ? dateApplied_calendar.getValue().toString() : null;
            String dateInstalled = (dateInstalled_calendar.getValue() != null) ? dateInstalled_calendar.getValue().toString() : null;
            String charges = other;
            int or = Integer.parseInt(official_reciept.getText());
            

            // Validation checks
            /*if (connectionFieldText.isEmpty() || houseNoFieldText.isEmpty() || barangayCodeFieldText.isEmpty() || barangay.isEmpty() ||
                    accountNoFieldText.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || middleName.isEmpty() || company.isEmpty() ||
                    contactNumberFieldText.isEmpty() || meterNumberFieldText.isEmpty() || meterBrand.isEmpty() || meterFeeFieldText.isEmpty() ||
                    dateApplied == null || dateInstalled == null) {

                showAlert(AlertType.WARNING, "Input Error", "Please fill in all required fields.");
                return; // Exit method if fields are empty
            }

            // Validate integer fields
            try {
                int connectionfield = Integer.parseInt(connectionFieldText);
                int houseNo = Integer.parseInt(houseNoFieldText);
                int barangayCode = Integer.parseInt(barangayCodeFieldText);
                int accountNo = Integer.parseInt(accountNoFieldText);
                int contactNumber = Integer.parseInt(contactNumberFieldText);
                int meterNo = Integer.parseInt(meterNumberFieldText);
                int meterFee = Integer.parseInt(meterFeeFieldText);
            } catch (NumberFormatException e) {
                showAlert(AlertType.WARNING, "Input Error", "Please enter valid numbers for fields that require integers.");
                return; // Exit method if any number parsing fails
            }*/

            java.sql.Date sqlDateApplied = null;
            java.sql.Date sqlDateInstalled = null;

            if (dateApplied != null && !dateApplied.isEmpty()) {
                sqlDateApplied = java.sql.Date.valueOf(dateApplied);
            }

            if (dateInstalled != null && !dateInstalled.isEmpty()) {
                sqlDateInstalled = java.sql.Date.valueOf(dateInstalled);
            }

            try (Connection conn = DatabaseConnector.connect()) {
                if (conn != null) {
                	long generatedId = DatabaseConnector.meter_Connection_insert(conn, Integer.parseInt(connectionFieldText), connectionTypCB.getValue(),
                	        barangay, accountNoFieldText, firstName, lastName, middleName, Integer.parseInt(houseNoFieldText),
                	        company, barangayCodeFieldText, contactNumber, Integer.parseInt(meterNumberFieldText),
                	        meterBrand, Integer.parseInt(meterFeeFieldText), sqlDateApplied, sqlDateInstalled, charges, or);

                    if (generatedId > 0) {
                        System.out.println("Data inserted successfully with ID: " + generatedId);

                        // Insert data into meter_connection_history table
                        DatabaseConnector.meter_connection_history_insert(conn, 
                        		accountNoFieldText, 
                                Integer.parseInt(connectionFieldText),
                                connectionFee_Text.getText(),
                                meterFee_field.getText(),
                                inspectionFee_Text.getText(),
                                otherFee_Text.getText(),
                                saddleClamp_Text.getText(),
                                miscellaneous_Text.getText());
                        showAlert(AlertType.WARNING, "Success", "Data inserted successfully");
                    } else {
                        showAlert(AlertType.WARNING, "Failed", "Failed to insert Data to Database");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    
    public void closeConfirmPayment(ActionEvent event) {
    	confim_SP.setVisible(false);
    	
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
    public void backToHomepage(ActionEvent event) throws IOException {
        // Get the current stage and its size
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();

        

        stage.setWidth(currentWidth);
        stage.setHeight(currentHeight);
    }
}

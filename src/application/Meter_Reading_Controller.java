package application;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Meter_Reading_Controller {
	 private Stage stage;
	 private Connection connection;
	 
	 @FXML
	 private Button readingList, manualBtn, reading_search_account_Btn;
	 
	 @FXML
	 private BorderPane manual_BP, readingList_BP;

	 @FXML
	 private ScrollPane scrollPane_barangay;  
	 
	 @FXML
	 private VBox container;
	 
	 @FXML
	 private TextField accountNumber_Field, connection_Field, meter_number_Field, 
	 connection_type_Field, name_Field, company_Field, barangay_Field, 
	 previous_reading_Field, previous_date, current_reading_Field, read_by_Field;
	 
	 @FXML
	 private TextField basic_Charge_Field, first_Cubic_Meter_Field, succeeding_Charge_Field,
	 excess_Field, basic_Amount_Field, succeeding_Amount_Field, total_Amount_field, consumption_Field;
	 
	 @FXML
	 private TextField billing_Entry_Field;
	 
	 @FXML 
	 private TableView<?> billingTable;
	 
	 @FXML
	 private DatePicker current_reading_date;

	 @FXML private TableView<Reading> reading_list_table;
	 @FXML private TableColumn<Reading, String> account_no_reading;
	 @FXML private TableColumn<Reading, String> name_reading;
	 @FXML private TableColumn<Reading, String> meter_no_rreading;
	 @FXML private TableColumn<Reading, Integer> previous_reading;
	 @FXML private TableColumn<Reading, Integer> current_reading;
	 @FXML private TableColumn<Reading, Integer> consumption_reading;
	 @FXML private TableColumn<Reading, Double> amount_reading;
	 @FXML private TableColumn<Reading, String> status_reading;
	 @FXML private TableColumn<Reading, Boolean> billing_reading;
	 @FXML private TableColumn<Reading, Integer> billingno;
	 
	 @FXML
	 private CheckBox disconnection_chebox;

	 @FXML
	 private TableView<BillingRecord> billing_TableView;
	 
	 @FXML
	 private TableColumn<BillingRecord, Integer> billing_Number_TV;
	 @FXML
	 private TableColumn<BillingRecord, String> billing_Period_TV;
	 @FXML
	 private TableColumn<BillingRecord, Double> total_Amount_Due_TV;
	 
	 private int consumption;
	 private String connectionType;
	 
	 boolean isDuplicate = false;
	 
	 Object accountNoReading;
	 
	 @FXML
	 public void initialize() {
		 
		    billing_Number_TV.setCellValueFactory(cellData -> cellData.getValue().billingEntryCountProperty().asObject());
		    billing_Period_TV.setCellValueFactory(cellData -> cellData.getValue().billingPeriodProperty());
		    total_Amount_Due_TV.setCellValueFactory(cellData -> cellData.getValue().amountPayableProperty().asObject());
		 
		    connection = (Connection) DatabaseConnector.connect();

		    readingList_BP.setVisible(false);
		    loadDataEntry();

		    account_no_reading.setCellValueFactory(cellData -> cellData.getValue().accountNumberProperty());
		    name_reading.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		    meter_no_rreading.setCellValueFactory(cellData -> cellData.getValue().meterNoProperty());
		    previous_reading.setCellValueFactory(cellData -> cellData.getValue().previousReadingProperty().asObject());
		    current_reading.setCellValueFactory(cellData -> cellData.getValue().currentReadingProperty().asObject());
		    consumption_reading.setCellValueFactory(cellData -> cellData.getValue().consumptionProperty().asObject());
		    amount_reading.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
		    status_reading.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
		    billingno.setCellValueFactory(cellData -> cellData.getValue().billingEntryCountProperty().asObject());
		    
		    billing_reading.setCellValueFactory(cellData -> cellData.getValue().billingReadingProperty());
		    billing_reading.setCellFactory(column -> {
		        TableCell<Reading, Boolean> cell = new TableCell<Reading, Boolean>() {
		            @Override
		            protected void updateItem(Boolean item, boolean empty) {
		                super.updateItem(item, empty);
		                if (empty) {
		                    setText(null);
		                    setGraphic(null);
		                } else {
		                    CheckBox checkBox = new CheckBox();
		                    checkBox.setSelected(item);
		                    Reading reading = getTableRow().getItem();
		                    checkBox.selectedProperty().bindBidirectional(reading.checkedProperty());
		                    
		                    Button button = new Button();
		                    button.setPrefWidth(10); 
		                    button.setPrefHeight(10);
		                    button.setOnMousePressed(event -> {
		                        button.setScaleX(0.9);
		                        button.setScaleY(0.9);
		                    });

		                    button.setOnMouseReleased(event -> {
		                        button.setScaleX(1.0);
		                        button.setScaleY(1.0);
		                    });
		                    button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
		                    ImageView imageView = new ImageView(new Image(getClass().getResource("/images/edit.png").toExternalForm()));
		                    imageView.setFitHeight(15);
		                    imageView.setPreserveRatio(true);
		                    button.setGraphic(imageView);
		                    
		                    button.setOnAction(event -> {
		                        accountNoReading = account_no_reading.getCellData(getTableRow().getItem());
		                        editBillingCheck();
		                    });
		                    HBox hbox = new HBox(button, checkBox);
		                    hbox.setSpacing(10);
		                    hbox.setAlignment(Pos.CENTER);
		                    setGraphic(hbox);
		                }
		            }
		        };
		        return cell;
		    });
		}
	 
	 public void printbill() {
		    try {
		        for (Reading reading : reading_list_table.getItems()) {
		            if (reading.isChecked()) {
		                System.out.println("Printing bill for account number: " + reading.accountNumberProperty().get());

		                // Execute SQL UPDATE statements to update both tables
		                String query1 = "UPDATE meter_readings SET bill_print = 'Billed' WHERE account_no = ?";
		                PreparedStatement pstmt1 = connection.prepareStatement(query1);
		                pstmt1.setString(1, reading.accountNumberProperty().get());
		                pstmt1.executeUpdate();

		                String query2 = "UPDATE reading_list SET status = 'Billed' WHERE account_no = ?";
		                PreparedStatement pstmt2 = connection.prepareStatement(query2);
		                pstmt2.setString(1, reading.accountNumberProperty().get());
		                pstmt2.executeUpdate();

		                // First, move the row data into the history table
		                String query3 = "INSERT INTO reading_list_history (account_no, name, meter_no, previous_reading, current_reading, consumption, amount, status, barangay, billing_entry_count) "
		                                 + "SELECT account_no, name, meter_no, previous_reading, current_reading, consumption, amount, status, barangay, billing_entry_count "
		                                 + "FROM reading_list WHERE account_no = ?";
		                PreparedStatement pstmt3 = connection.prepareStatement(query3);
		                pstmt3.setString(1, reading.accountNumberProperty().get());
		                pstmt3.executeUpdate();

		                // Then delete the row from the reading_list
		                String query4 = "DELETE FROM reading_list WHERE account_no = ?";
		                PreparedStatement pstmt4 = connection.prepareStatement(query4);
		                pstmt4.setString(1, reading.accountNumberProperty().get());
		                pstmt4.executeUpdate();

		                showAlert("Success", "Reccord Billed");
		            }
		        }
		    } catch (SQLException e) {
		        System.out.println("Error updating tables: " + e.getMessage());
		    }
		}

	 
	 public void editBillingCheck() {
		 System.out.println("Account No asdasdasd: " + accountNoReading);
	 }
	 
	 @FXML 
	 public void afterSelectingDate(ActionEvent event) throws IOException {
		 int currentReading = Integer.parseInt(current_reading_Field.getText());
		 int previousReading = 0;
		 
		 if (previous_reading_Field.getText().isEmpty()) {
			 previousReading = 0;
		 } else {
			 previousReading = Integer.parseInt(previous_reading_Field.getText());
		 }
		 
		 consumption = currentReading - previousReading;

		 consumption_Field.setText(String.valueOf(consumption));
		 loadComputationDetails();
	 }
	 
	 @FXML
	 public void loadTableData() {
	     ObservableList<BillingRecord> billingRecords = FXCollections.observableArrayList();

	     double addedAmountPayable = 0;
	     int penalty = 0;

	     // Get the value of the account number from the text field
	     String accountNumberText = accountNumber_Field.getText();
	     if (accountNumberText == null || accountNumberText.trim().isEmpty()) {
	         System.out.println("Please enter a valid account number.");
	         return;
	     }

	     try {
	         // SQL query to fetch overdue readings based on the account number
	         String query = "SELECT billing_entry_count, " +
	                 "CONCAT(reading_last_month_date, ' - ', reading_this_month_date) AS billing_period, " +
	                 "amount_payable " +
	                 "FROM meter_readings " +
	                 "WHERE account_no = ? AND reading_last_month_date < (SELECT reading_last_month_date FROM meter_readings WHERE account_no = ? ORDER BY reading_last_month_date DESC LIMIT 1)";

	         try (Connection conn = DatabaseConnector.connect();
	              PreparedStatement stmt = conn.prepareStatement(query)) {

	             // Set the account number in the prepared statement
	             stmt.setString(1, accountNumberText);
	             stmt.setString(2, accountNumberText);

	             try (ResultSet rs = stmt.executeQuery()) {
	                 while (rs.next()) {
	                     int billingEntryCount = rs.getInt("billing_entry_count");
	                     String billingPeriod = rs.getString("billing_period");
	                     double amountPayable = rs.getDouble("amount_payable");

	                     BillingRecord record = new BillingRecord(billingEntryCount, billingPeriod, amountPayable);
	                     billingRecords.add(record);

	                     System.out.println("Billing Entry: " + billingEntryCount +
	                             ", Period: " + billingPeriod +
	                             ", Amount Payable: " + amountPayable);

	                     addedAmountPayable += amountPayable;
	                     penalty += 2;
	                 }

	                 // Add penalty data
	                 Double totalPenalty = ((double) penalty / 100) * addedAmountPayable;
	                 System.out.print(totalPenalty);

	                 BillingRecord penaltyData = new BillingRecord(0, "BILLING PENALTIES - " + String.valueOf(penalty) + "%", totalPenalty);
	                 billingRecords.add(penaltyData);

	                 // Add the latest reading to the table
	                 String latestQuery = "SELECT billing_entry_count, " +
	                         "CONCAT(reading_last_month_date, ' - ', reading_this_month_date) AS billing_period, " +
	                         "amount_payable " +
	                         "FROM meter_readings " +
	                         "WHERE account_no = ? ORDER BY reading_last_month_date DESC LIMIT 1";

	                 try (PreparedStatement latestStmt = conn.prepareStatement(latestQuery)) {
	                     latestStmt.setString(1, accountNumberText);
	                     try (ResultSet latestRs = latestStmt.executeQuery()) {
	                         if (latestRs.next()) {
	                             int latestBillingEntryCount = latestRs.getInt("billing_entry_count");
	                             String latestBillingPeriod = latestRs.getString("billing_period");
	                             double latestAmountPayable = latestRs.getDouble("amount_payable");

	                             BillingRecord latestRecord = new BillingRecord(latestBillingEntryCount, latestBillingPeriod, latestAmountPayable);
	                             billingRecords.add(latestRecord);
	                         }
	                     }
	                 }

	                 // Set the data to the TableView
	                 billing_TableView.setItems(billingRecords);
	             }
	         }
	     } catch (SQLException e) {
	         System.out.println("Error loading table data: " + e.getMessage());
	         e.printStackTrace();
	     }
	 }


	 @FXML
	 public void loadToTable() {
	     ObservableList<BillingRecord> billingRecords = FXCollections.observableArrayList();

	     double totalAmountDue = 0; // To track total amount due for calculating penalties
	     double totalPenalty = 0; // To track the total penalty amount

	     // Custom date for testing purposes (for example, December 31, 2024)
	     LocalDate customCurrentDate = LocalDate.of(2024, 12, 31);  // Use any date you'd like for testing

	     // Get the value of the account number from the text field
	     String accountNumberText = accountNumber_Field.getText();
	     if (accountNumberText == null || accountNumberText.trim().isEmpty()) {
	         System.out.println("Please enter a valid account number.");
	         return;
	     }

	     try {
	         // Parse the account number as an integer

	         // SQL query to fetch unpaid records based on the account number
	         String query = "SELECT billing_entry_count, " +
	                         "CONCAT(reading_last_month_date, ' - ', reading_this_month_date) AS billing_period, " +
	                         "amount_payable, reading_this_month_date " +  // Include the actual date to check overdue
	                         "FROM meter_readings " +
	                         "WHERE account_no = ? AND status = 'Unpaid'";

	         try (Connection conn = DatabaseConnector.connect();
	              PreparedStatement stmt = conn.prepareStatement(query)) {

	             // Set the account number in the prepared statement
	             stmt.setString(1, accountNumberText);

	             try (ResultSet rs = stmt.executeQuery()) {
	                 boolean hasResults = false; // Flag to track if results are returned
	                 while (rs.next()) {
	                     int billingEntryCount = rs.getInt("billing_entry_count");
	                     String billingPeriod = rs.getString("billing_period");
	                     double amountPayable = rs.getDouble("amount_payable");
	                     String readingThisMonthDateStr = rs.getString("reading_this_month_date");

	                     // Parse the reading_this_month_date string into a LocalDate
	                     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	                     LocalDate readingThisMonthDate = LocalDate.parse(readingThisMonthDateStr, formatter);

	                     // Calculate the difference in days between the custom current date and the reading_this_month_date
	                     long daysDifference = ChronoUnit.DAYS.between(readingThisMonthDate, customCurrentDate);

	                     // If the difference is greater than 31 days, mark it as "Overdue"
	                     boolean isOverdue = daysDifference > 31;

	                     // Add penalty to the total amount due if overdue
	                     if (isOverdue) {
	                         billingPeriod += " (Penalty)";
	                         totalPenalty += amountPayable * 0.02; // Add 2% penalty to the total penalty
	                     }

	                     // Add the amount payable to the total amount due
	                     totalAmountDue += amountPayable;

	                     // Create the billing record
	                     BillingRecord record = new BillingRecord(billingEntryCount, billingPeriod, amountPayable);
	                     billingRecords.add(record);

	                     System.out.println("Billing Entry: " + billingEntryCount +
	                             ", Period: " + billingPeriod +
	                             ", Amount Payable: " + amountPayable +
	                             (isOverdue ? " (Overdue)" : ""));

	                     hasResults = true; // Mark that results were found
	                 }

	                 // If no results are found
	                 if (!hasResults) {
	                     System.out.println("No unpaid records found for this account number.");
	                 }

	                 // Add the penalty record
	                 if (totalPenalty > 0) {
	                     BillingRecord penaltyRecord = new BillingRecord(0, "BILLING PENALTIES", totalPenalty);
	                     billingRecords.add(penaltyRecord);
	                 }

	                 // Add the "TOTAL" record at the end
	                 BillingRecord totalRecord = new BillingRecord(0, "TOTAL", totalAmountDue + totalPenalty);
	                 billingRecords.add(totalRecord);

	                 // Set the data to the TableView
	                 billing_TableView.setItems(billingRecords);


	             }
	         }
	     } catch (NumberFormatException e) {
	         System.out.println("Invalid account number. Please enter a numeric value.");
	     } catch (SQLException e) {
	         System.out.println("Error loading table data: " + e.getMessage());
	         e.printStackTrace();
	     }
	 }


	 
	 private void loadDataEntry() {
		    try (Connection conn = DatabaseConnector.connect()) {
		        int highestBillingEntryCount = DatabaseConnector.getHighestBillingEntryCount(conn);
		        if (highestBillingEntryCount == 0) {
		        	billing_Entry_Field.setText("0");
		        }
		        billing_Entry_Field.setText(String.valueOf(highestBillingEntryCount + 1));
		    } catch (SQLException e) {
		        System.out.println("Error while loading data: " + e.getMessage());
		        e.printStackTrace();
		    }
		}


	 private void loadComputationDetails() {
	        if (connectionType.equals("Residential")) {
	        	String selectSQL = "SELECT * FROM residential_charges ORDER BY created_at DESC LIMIT 1";

		        try (Connection connection = DatabaseConnector.connect();
		             PreparedStatement pstmt = connection.prepareStatement(selectSQL);
		             ResultSet rs = pstmt.executeQuery()) {

		            if (rs.next()) {
		                // Set the values in the text fields for residential charges
		            	basic_Charge_Field.setText(String.valueOf(rs.getLong("monthly_basic")));
		            	succeeding_Charge_Field.setText(String.valueOf(rs.getDouble("additional_per_m3")));
		                first_Cubic_Meter_Field.setText(String.valueOf(rs.getDouble("initial_m3")));
		                
		                if (Integer.parseInt(consumption_Field.getText()) <= Double.parseDouble(first_Cubic_Meter_Field.getText())) {
		                	excess_Field.setText("0");
		                }else {
		                	double excess = Double.parseDouble(consumption_Field.getText()) - Double.parseDouble(first_Cubic_Meter_Field.getText());
			                String excessText = String.valueOf(excess);
			                excess_Field.setText(excessText);
		                }

		            }
		        } catch (SQLException e) {
		            System.out.println("Failed to load latest residential charges: " + e.getMessage());
		            
		        }
	        }else if (connectionType.equals("Commercial")) {
	        	String selectSQL = "SELECT * FROM commercial_charges ORDER BY created_at DESC LIMIT 1";

	            try (Connection connection = DatabaseConnector.connect();
	                 PreparedStatement pstmt = connection.prepareStatement(selectSQL);
	                 ResultSet rs = pstmt.executeQuery()) {

	                if (rs.next()) {
	                    // Set the values in the text fields for commercial charges
	                	basic_Charge_Field.setText(String.valueOf(rs.getLong("monthly_basic")));
		            	succeeding_Charge_Field.setText(String.valueOf(rs.getDouble("additional_per_m3")));
		                first_Cubic_Meter_Field.setText(String.valueOf(rs.getDouble("initial_m3")));
		                double excess = Double.parseDouble(consumption_Field.getText()) - Double.parseDouble(first_Cubic_Meter_Field.getText());
		                String excessText = String.valueOf(excess);
		                excess_Field.setText(excessText);
	                }
	            } catch (SQLException e) {
	                System.out.println("Failed to load latest commercial charges: " + e.getMessage());
	            }
	        }
	        
	        double succ = Double.parseDouble(succeeding_Charge_Field.getText()) * Double.parseDouble(excess_Field.getText());
	        double total = Double.parseDouble(basic_Charge_Field.getText()) + succ;
	        
	        basic_Amount_Field.setText(basic_Charge_Field.getText());
	        succeeding_Amount_Field.setText(String.valueOf(succ));
	        total_Amount_field.setText(String.valueOf(total));
	    }
	 
	 public void updateDisconnect(String accountno) {
		    boolean disconnectStatus = disconnection_chebox.isSelected();

		    // Fetch the current 'disconnect' value from the database (assuming it's a string 'f' or 't')
		    try (Connection conn = DatabaseConnector.connect()) {
		        if (conn != null) {
		            // SQL query to fetch the current disconnect status for the given account
		            String query = "SELECT disconnect FROM meter_connection WHERE accountno_field = ?";
		            PreparedStatement pstmt = conn.prepareStatement(query);
		            pstmt.setString(1, accountno);  // Set the account number as a parameter

		            // Execute the query and retrieve the result
		            ResultSet rs = pstmt.executeQuery();
		            if (rs != null && rs.next()) {
		                String currentDisconnectStatus = rs.getString("disconnect");

		                // Check if the current status is 'f' (false)
		                if ("f".equals(currentDisconnectStatus)) {
		                    // If the checkbox is selected, change the value to 't' (true)
		                    if (disconnectStatus) {
		                        // SQL query to update the disconnect status to 't'
		                        String updateQuery = "UPDATE meter_connection SET disconnect = 't' WHERE accountno_field = ?";
		                        PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
		                        updatePstmt.setString(1, accountno);
		                        updatePstmt.executeUpdate();
		                        System.out.println("Disconnect status updated to 't' for account number: " + accountno);
		                    }
		                } else if ("t".equals(currentDisconnectStatus)) {
		                    // If the current status is already 't', and checkbox is not selected, set it back to 'f'
		                    if (!disconnectStatus) {
		                        // SQL query to update the disconnect status to 'f'
		                        String updateQuery = "UPDATE meter_connection SET disconnect = 'f' WHERE accountno_field = ?";
		                        PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
		                        updatePstmt.setString(1, accountno);
		                        updatePstmt.executeUpdate();
		                        System.out.println("Disconnect status updated to 'f' for account number: " + accountno);
		                    }
		                }
		            }
		        }
		    } catch (SQLException e) {
		        System.out.println("Database error: " + e.getMessage());
		    }
		}

	 
	 @FXML
	 public void updateMeterReadings(ActionEvent event) throws IOException {
	     insertToReadingList();
	     String readingThisMonth = current_reading_Field.getText();
	     LocalDate readingThisMonthDate = current_reading_date.getValue();
	     String accountNumber = accountNumber_Field.getText();
	     String readingLastMonth = previous_reading_Field.getText();
	     String readingLastMonthDate = previous_date.getText();
	     String readby = read_by_Field.getText();
	     double total = Double.parseDouble(total_Amount_field.getText());
	     int billingEntry = Integer.parseInt(billing_Entry_Field.getText());

	     // Check if readingLastMonth and readingLastMonthDate are empty and set default values
	     if (readingLastMonth == null || readingLastMonth.isEmpty()) {
	         readingLastMonth = "0";  // Set default value for last month's reading
	     }
	     if (readingLastMonthDate == null || readingLastMonthDate.isEmpty()) {
	         readingLastMonthDate = "0000-00-00";  // Set default value for last month's date
	     }

	     if (readingThisMonth != null && !readingThisMonth.isEmpty() &&
	         readingThisMonthDate != null && accountNumber != null && !accountNumber.isEmpty() &&
	         readingLastMonth != null && !readingLastMonth.isEmpty() && readingLastMonthDate != null && !readingLastMonthDate.isEmpty()) {

	         try {
	             // Check if reading values are valid numbers
	             if (!readingThisMonth.matches("\\d+") || !readingLastMonth.matches("\\d+")) {
	            	 showAlert("Error: ", "Reading values must be valid numbers.");
	                 return;
	             }

	             long currentReading = Long.parseLong(readingThisMonth);
	             long lastReading = Long.parseLong(readingLastMonth);

	             // Handle invalid last reading date
	             java.sql.Date currentReadingDate = java.sql.Date.valueOf(readingThisMonthDate);
	             java.sql.Date lastReadingDate = null;

	             // Only set lastReadingDate if it's not "0000-00-00"
	             if (!readingLastMonthDate.equals("0000-00-00")) {
	                 lastReadingDate = java.sql.Date.valueOf(readingLastMonthDate);
	             } else {
	                 // Set to null if the date is invalid
	                 lastReadingDate = null;
	             }
	             System.out.println(isDuplicate);

	             if (!isDuplicate) {
	                 try (Connection conn = DatabaseConnector.connect()) {
	                     if (conn != null) {
	                         // Call insertMeterReading with the updated values
	                         DatabaseConnector.insertMeterReading(conn, accountNumber, currentReading, currentReadingDate, lastReading, lastReadingDate, readby, total, billingEntry);
	                         loadDataEntry();
	                         updateDisconnect(accountNumber);
	                         showAlert("Successs", "Reading has been recorded");
	                     }
	                 }
	             } else {
	                 Alert alert = new Alert(Alert.AlertType.ERROR);
	                 alert.setTitle("Error");
	                 alert.setHeaderText("Duplicate Key Error");
	                 alert.setContentText("Account number already exists in the reading list.");
	                 alert.showAndWait();

	                 System.out.print("hello");
	             }
	         } catch (SQLException e) {
	        	    showAlert("Database Error", "Error updating meter readings: " + e.getMessage());
	        	} catch (NumberFormatException e) {
	        	    showAlert("Invalid Input", "Error: Invalid number format.");
	        	} catch (IllegalArgumentException e) {
	        	    showAlert("Invalid Date", "Invalid date format: " + e.getMessage());
	        	}
	        	} else {
	        	    showAlert("Incomplete Form", "Please fill in all fields.");
	        	}
	 }
	 
	 public void showAlert(String title, String message) {
		    Alert alert = new Alert(Alert.AlertType.INFORMATION);
		    alert.setTitle(title);
		    alert.setHeaderText(null);
		    alert.setContentText(message);
		    alert.showAndWait();
		}
	 
	 @FXML
	 public void insertToReadingList() {
	     try {
	         // Get the values from the text fields
	         String accountNumber = accountNumber_Field.getText();
	         String name = name_Field.getText();
	         int meterNumber = Integer.parseInt(meter_number_Field.getText());
	         
	         
	         double previousReading;
	         if (previous_reading_Field.getText().isEmpty()) {
	             previousReading = 0;
	         } else {
	             previousReading = Double.parseDouble(previous_reading_Field.getText());
	         }
	         
	         double currentReading = Double.parseDouble(current_reading_Field.getText());
	         int consumption = Integer.parseInt(consumption_Field.getText());
	         double totalAmount = Double.parseDouble(total_Amount_field.getText());
	         String barangay = barangay_Field.getText();
		     int billingEntry = Integer.parseInt(billing_Entry_Field.getText());

	         String query = "INSERT INTO reading_list (account_no, name, meter_no, previous_reading, current_reading, consumption, amount, barangay, billing_entry_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

	         try (Connection conn = DatabaseConnector.connect();
	              PreparedStatement stmt = conn.prepareStatement(query)) {

	             // Set the values in the prepared statement
	             stmt.setString(1, accountNumber);
	             stmt.setString(2, name);
	             stmt.setInt(3, meterNumber);
	             stmt.setDouble(4, previousReading);
	             stmt.setDouble(5, currentReading);
	             stmt.setInt(6, consumption);
	             stmt.setDouble(7, totalAmount);
	             stmt.setString(8, barangay);
	             stmt.setInt(9, billingEntry);

	             // Execute the query
	             stmt.executeUpdate();

	             System.out.println("Data inserted successfully!");
	         }
	     } catch (SQLException e) {
	         if (e.getMessage().contains("duplicate key value violates unique constraint")) {
	             this.isDuplicate = true;
	             System.out.println("Error inserting data: Account number already exists.");
	         } else {
	             System.out.println("Error inserting data: " + e.getMessage());
	         }
	         e.printStackTrace();
	     } catch (NumberFormatException e) {
	         System.out.println("Invalid number format: " + e.getMessage());
	         e.printStackTrace();
	     }
	 }


	 

	 public void backToHomepage(ActionEvent event) throws IOException {
	        // Get the current stage and its size
	        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	        double currentWidth = stage.getWidth();
	        double currentHeight = stage.getHeight();

	        showLoadingAndSwitchStage("Homepage", event);

	        // After switching, set the stage size back to the captured width and height
	        stage.setWidth(currentWidth);
	        stage.setHeight(currentHeight);
	    }
	 
	 @FXML
	 private void searchByAccountNumber() {
	     String accountNumberText = accountNumber_Field.getText();
	     if (accountNumberText != null && !accountNumberText.isEmpty()) {
	         try {

	             
	             try (Connection conn = DatabaseConnector.connect()) {
	                 if (conn != null) {
	                     // Fetch basic data by account number
	                     ResultSet rs = DatabaseConnector.searchByAccountNumber(conn, accountNumberText);
	                     
	                     if (rs != null && rs.next()) {
	                         System.out.println("Data found for account number: " + accountNumberText);
	                         
	                         // Fetch and set the basic fields
	                         String accountNo = rs.getString("accountno_field");
	                         String connectionType = rs.getString("connection_field");
	                         String meterNo = rs.getString("meter_no");
	                         String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
	                         String company = rs.getString("company");
	                         String barangay = rs.getString("barangay_field");
	                         String connection = rs.getString("connectiontype_cb");
	                         
	                         accountNumber_Field.setText(accountNo);
	                         connection_Field.setText(connectionType);
	                         meter_number_Field.setText(meterNo);
	                         connection_type_Field.setText(connectionType);
	                         name_Field.setText(fullName);
	                         company_Field.setText(company);
	                         barangay_Field.setText(barangay);
	                         this.connectionType = connection;
	                         
	                         System.out.print(connectionType + "\n");
	                         
	                         // After basic data is fetched, retrieve the meter reading data
	                         fetchReadingData(accountNumberText, conn);
	                		 billing_Number_TV.setCellValueFactory(new PropertyValueFactory<>("billingEntryCount"));
	                		 billing_Period_TV.setCellValueFactory(new PropertyValueFactory<>("billingPeriod"));
	                		 total_Amount_Due_TV.setCellValueFactory(new PropertyValueFactory<>("amountPayable"));
	                		 
	                		 
	                		 loadToTable();
	                     } else {
	                         System.out.println("No data found for account number: " + accountNumberText);
	                     }
	                 }
	             } catch (SQLException e) {
	                 System.out.println("Database error: " + e.getMessage());
	             }
	         } catch (NumberFormatException e) {
	             System.out.println("Invalid account number format.");
	         }
	     } else {
	         System.out.println("Please enter an account number.");
	     }
	 }
	 
	 

	 private void fetchReadingData(String accountNumber, Connection conn) {
		    try {
		        // Fetch the latest meter reading data for the given account number
		        String query = "SELECT reading_this_month, reading_this_month_date "
		                + "FROM meter_readings "
		                + "WHERE account_no = ? AND billing_entry_count = ("
		                + "SELECT MAX(billing_entry_count) FROM meter_readings WHERE account_no = ?)";

		        PreparedStatement stmt = conn.prepareStatement(query);
		        stmt.setString(1, accountNumber);
		        stmt.setString(2, accountNumber);

		        ResultSet rs = stmt.executeQuery();

		        if (rs != null && rs.next()) {
		            // Get the reading_this_month and reading_this_month_date
		            long readingThisMonth = rs.getLong("reading_this_month");
		            java.sql.Date readingThisMonthDate = rs.getDate("reading_this_month_date");

		            // Display the values (for demonstration purposes, adjust for your UI)
		            System.out.println("Latest Reading This Month: " + readingThisMonth);
		            System.out.println("Reading Date: " + (readingThisMonthDate != null ? readingThisMonthDate.toString() : "No date available"));

		            // Populate the fields with the latest reading data
		            previous_reading_Field.setText(String.valueOf(readingThisMonth));
		            previous_date.setText(readingThisMonthDate != null ? readingThisMonthDate.toString() : "No date available");
		        } else {
		            System.out.println("No reading data found for account number: " + accountNumber);
		        }
		    } catch (SQLException e) {
		        System.out.println("Error fetching reading data: " + e.getMessage());
		    }
		}


	 
	 public void clickReadingList(ActionEvent event) throws IOException {
		 readingList.setStyle(
		            "-fx-background-color: #2175a1;" +     
		            "-fx-text-fill: #ffffff;" +             
		            "-fx-border-radius: 10px;" +            
		            "-fx-background-radius: 10px;"          
		        );
		 
		 manualBtn.setStyle(
		            "-fx-background-color: #FFFFFF;" +     
		            "-fx-text-fill: #000000;" +                      
		            "-fx-background-radius: 10px;" +
		            "-fx-border-color: #2175A1;" + 
		            "-fx-border-radius: 10px;"
		        );
		 
		 
		 try {
			    if (container.getChildren().isEmpty()) {
			        List<String> barangayNames = new ArrayList<>();
			        ResultSet rs = DatabaseConnector.getBarangays(connection);

			        while (rs.next()) {
			            barangayNames.add(rs.getString("barangay_name"));
			        }

			        barangayNames.sort(String::compareToIgnoreCase);

			        for (String barangayName : barangayNames) {  
			            Button barangayButton = new Button(barangayName);

			            container.setAlignment(Pos.CENTER);
			            container.setPadding(new Insets(5, 0, 5, 0));
			            container.setSpacing(5);

			            barangayButton.setId("buttonRoundedStyle");
			            barangayButton.setPrefWidth(210);
			            barangayButton.setStyle("-fx-alignment: center; -fx-pref-height: 100;");

			            barangayButton.setOnAction(buttonEvent -> handleBarangayButtonClick(barangayName));

			            container.getChildren().add(barangayButton);
			        }
			    } else {
			        System.out.println("Barangay list is already loaded.");
			    }
			} catch (SQLException e) {
			    System.out.println("Error fetching barangay data: " + e.getMessage());
			}
		 
		 manual_BP.setVisible(false);
		 readingList_BP.setVisible(true);

	 }
	 
	 public void handleBarangayButtonClick(String barangayName) {
		    System.out.println("Barangay clicked: " + barangayName);

		    if (barangayName != null && !barangayName.isEmpty()) {
		        System.out.println("Barangay name is valid, proceeding to fetch data.");
		        performActionBasedOnBarangay(barangayName);
		    } else {
		        System.out.println("No barangay selected or invalid name.");
		    }
		}

	 private void performActionBasedOnBarangay(String barangayName) {
		    // Create an observable list to hold the data for the TableView
		    ObservableList<Reading> readings = FXCollections.observableArrayList();

		    try {
		        // Assuming you have a method to fetch the data from your database based on barangayName
		        String query = "SELECT account_no, name, meter_no, previous_reading, current_reading, consumption, amount, status, billing_entry_count "
		                + "FROM reading_list WHERE barangay = ?";

		        System.out.println("Executing query: " + query);  // Debugging the query

		        PreparedStatement stmt = connection.prepareStatement(query);
		        stmt.setString(1, barangayName);

		        ResultSet rs = stmt.executeQuery();
		        System.out.println("Query executed, processing results...");

		        // Check if there are any records in the result set
		        if (!rs.next()) {
		            System.out.println("No records found for barangay: " + barangayName);
		            return;  // Exit if no records are found
		        }

		        // Loop through the result set
		        do {
		            String accountNumber = rs.getString("account_no");
		            String name = rs.getString("name");
		            String meterNo = rs.getString("meter_no");
		            int previousReading = rs.getInt("previous_reading");
		            int currentReading = rs.getInt("current_reading");
		            int consumption = rs.getInt("consumption");
		            double amount = rs.getDouble("amount");
		            String status = rs.getString("status");
		            int billingEntryCount = rs.getInt("billing_entry_count");

		            // Debugging output for each row
		            System.out.println("Fetched data: " + accountNumber + ", " + name + ", " + meterNo + ", "
		                    + previousReading + ", " + currentReading + ", " + consumption + ", " + amount + ", " + status + ", " + billingEntryCount);

		            // Create a new Reading object and add it to the list
		            readings.add(new Reading(accountNumber, name, meterNo, previousReading, currentReading, consumption, amount, status, false, billingEntryCount)); // Initialize billingReading to false
		        } while (rs.next());  // Ensures all records are processed

		        // Debugging the number of items in the list
		        System.out.println("Number of readings fetched: " + readings.size());

		        // Set the data in the TableView
		        reading_list_table.setItems(readings);

		        // Debugging to confirm that data was set to the table
		        System.out.println("TableView populated with readings.");
		    } catch (SQLException e) {
		        System.out.println("Error fetching readings for barangay " + barangayName + ": " + e.getMessage());
		        e.printStackTrace();  // Print the full stack trace for more details
		    }
		}



	 
	 public void clickManual(ActionEvent event) throws IOException {
		 readingList.setStyle(
				 "-fx-background-color: #FFFFFF;" +     
				 "-fx-text-fill: #000000;" +                      
				 "-fx-background-radius: 10px;" +
				 "-fx-border-color: #2175A1;" + 
				 "-fx-border-radius: 10px;"         
		        );
		 
		 manualBtn.setStyle(
				 "-fx-background-color: #2175a1;" +     
				 "-fx-text-fill: #ffffff;" +             
				 "-fx-border-radius: 10px;" +            
				 "-fx-background-radius: 10px;" 
		        );
		 
		 manual_BP.setVisible(true);
		 readingList_BP.setVisible(false);
		 
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
		
		@FXML
	public void back(ActionEvent event) throws IOException {
		navigation("Homepage", event);
	}

	 
    private void showLoadingAndSwitchStage(String fxmlFileName, Event event) {
        StackPane loadingPane = new StackPane();
        loadingPane.setStyle("-fx-background-color: #fffdf2;");
        ProgressIndicator progressIndicator = new ProgressIndicator();
        loadingPane.getChildren().add(progressIndicator);

        // Get the current stage and its size
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();
        
        // Show the loading screen first
        Scene loadingScene = new Scene(loadingPane, currentWidth, currentHeight);
        stage.setScene(loadingScene);
        

        // Start the loading task
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1000); // Simulate some loading time
                return null;
            }

            @Override
            protected void succeeded() { 
                try {
                    // Once loading is done, load the new scene
                    Parent newRoot = FXMLLoader.load(getClass().getResource(fxmlFileName + ".fxml"));
                    newRoot.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
                    Scene newScene = new Scene(newRoot, currentWidth, currentHeight);
                    
                    // Switch to the new scene
                    stage.setScene(newScene);
                    stage.setTitle(fxmlFileName);

                    // Allow interaction with the new scene
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // Run the loading task in a separate thread
        new Thread(loadTask).start();
    }
}
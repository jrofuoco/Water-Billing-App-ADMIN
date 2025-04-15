package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Date;

public class Bills_And_Payment_Controller {
	 
	 @FXML
	 private TableView<BillingRecord> billing_TableView;
	 
	 @FXML
	 private TableColumn<BillingRecord, Integer> billing_Number_TV;
	 @FXML
	 private TableColumn<BillingRecord, String> billing_Period_TV;
	 @FXML
	 private TableColumn<BillingRecord, Double> total_Amount_Due_TV;
	 
	 @FXML
	 private TableView<BillingRecord> outstanding_bills_TableView; 

	 @FXML
	 private TableColumn<BillingRecord, Integer> billing_Number_TV1; 
	 @FXML
	 private TableColumn<BillingRecord, String> billing_Period_TV1;
	 @FXML
	 private TableColumn<BillingRecord, Double> total_Amount_Due_TV1;

	 @FXML
	 private ObservableList<BillingRecord> outstandingBills = FXCollections.observableArrayList(); 


	
	@FXML
	private TextField account_no_field, connection_no_field, meter_no_field, connection_type_field, name_field, paid_by_field, barangay_field, amount_field, penalty_field, total_field, official_reciept_field;
	
	@FXML 
	private Button saveNew;
	
	private Stage stage;
	
	@FXML
	public void initialize() {
		billing_Number_TV.setCellValueFactory(cellData -> cellData.getValue().billingEntryCountProperty().asObject());
		billing_Period_TV.setCellValueFactory(cellData -> cellData.getValue().billingPeriodProperty());
		total_Amount_Due_TV.setCellValueFactory(cellData -> cellData.getValue().amountPayableProperty().asObject());

	    billing_Number_TV1.setCellValueFactory(new PropertyValueFactory<>("billingEntryCount"));
	    billing_Period_TV1.setCellValueFactory(new PropertyValueFactory<>("billingPeriod"));
	    total_Amount_Due_TV1.setCellValueFactory(new PropertyValueFactory<>("amountPayable"));

	    // Set the ObservableList to the second TableView
	    outstanding_bills_TableView.setItems(outstandingBills);

	    // Set a row click listener for the first TableView (billing_TableView)
	    billing_TableView.setOnMouseClicked(event -> {
	        if (event.getClickCount() == 1) {
	            BillingRecord selectedRecord = billing_TableView.getSelectionModel().getSelectedItem();

	            if (selectedRecord != null) {
	                System.out.println("Selected Record: " + selectedRecord);
	                
	                // Prevent adding records with billingEntryCount = 0
	                if (selectedRecord.getBillingEntryCount() == 0) {
	                    System.out.println("Skipping record with billingEntryCount = 0");
	                    return; 
	                }

	                // Add to outstanding bills
	                outstandingBills.add(selectedRecord);
	                System.out.println("Record added to outstandingBills: " + selectedRecord);

	                // Remove from billing_TableView
	                billing_TableView.getItems().remove(selectedRecord);
	                System.out.println("Record removed from billing_TableView: " + selectedRecord);

	                // Update amounts and penalties
	                updateAmountAndPenalty();
	            }
	        }
	    });

	}


	private void updateAmountAndPenalty() {
	    double totalAmountDue = 0;
	    double totalPenalty = 0;

	    // Calculate total amount due and penalty for the records in outstanding_bills_TableView
	    for (BillingRecord record : outstandingBills) {
	        totalAmountDue += record.getAmountPayable();

	        // Check if the record is overdue to apply a penalty (Assuming overdue logic based on your earlier logic)
	        String billingPeriod = record.getBillingPeriod();
	        if (billingPeriod.contains("(Penalty)")) {
	            totalPenalty += record.getAmountPayable() * 0.02; // Assuming 2% penalty
	        }
	    }

	    // Set the values to the text fields
	    amount_field.setText(String.format("%.2f", totalAmountDue));
	    penalty_field.setText(String.format("%.2f", totalPenalty));
	    total_field.setText(String.format("%.2f", totalAmountDue + totalPenalty));
	}


	@FXML
	private void pay() {
	    // Get the values from the text fields
	    String accountNumberText = account_no_field.getText();
	    String accountName = name_field.getText();
	    String paymentAmountText = total_field.getText();
	    String officialReceiptText = official_reciept_field.getText();
	    String paidBy = paid_by_field.getText();

	    // Parse the account number and payment amount as integers
	    String accountNumber;
	    double paymentAmount;
	    int officialReceipt;
	    try {
	        accountNumber = accountNumberText;
	        paymentAmount = Double.parseDouble(paymentAmountText);
	        officialReceipt = Integer.parseInt(officialReceiptText);
	    } catch (NumberFormatException e) {
	        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Invalid account number, payment amount, or receipt format.");
	        return;
	    }

	    // Get the current date
	    LocalDate currentDate = LocalDate.now();
	    Date paymentDate = Date.valueOf(currentDate); // Convert LocalDate to java.sql.Date

	    // Update the status of each record in the `outstanding_bills_TableView` and collect their periods
	    StringBuilder billPeriods = new StringBuilder();
	    try (Connection conn = DatabaseConnector.connect()) {
	        String updateStatusQuery = "UPDATE meter_readings SET status = 'Paid' WHERE billing_entry_count = ?";
	        try (PreparedStatement updateStmt = conn.prepareStatement(updateStatusQuery)) {
	            for (BillingRecord record : outstandingBills) {
	                updateStmt.setInt(1, record.getBillingEntryCount());
	                int updatedRows = updateStmt.executeUpdate();

	                if (updatedRows > 0) {
	                    billPeriods.append(record.getBillingPeriod()).append("\n ");
	                }
	            }
	        }

	        // Trim trailing comma and space from billPeriods
	        if (billPeriods.length() > 0) {
	            billPeriods.setLength(billPeriods.length() - 2);
	        }

	        // Insert the payment information into the payment_history table
	        String insertPaymentQuery = "INSERT INTO payment_history (account_no, account_name, payment_amount, bill_period, official_reciept, payment_date, paid_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
	        try (PreparedStatement insertStmt = conn.prepareStatement(insertPaymentQuery)) {
	            insertStmt.setString(1, accountNumber);
	            insertStmt.setString(2, accountName);
	            insertStmt.setDouble(3, paymentAmount);
	            insertStmt.setString(4, billPeriods.toString());
	            insertStmt.setInt(5, officialReceipt);
	            insertStmt.setDate(6, paymentDate);
	            insertStmt.setString(7, paidBy);

	            int rowsAffected = insertStmt.executeUpdate();
	            if (rowsAffected > 0) {
	                showAlert(Alert.AlertType.INFORMATION, "Success", "Payment recorded and bills updated successfully.");
	                outstandingBills.clear(); // Clear the TableView after payment
	                clearFields(); // Optional: Clear text fields
	            } else {
	                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save payment history.");
	            }
	        }
	    } catch (SQLException e) {
	        showAlert(Alert.AlertType.ERROR, "Database Error", "Error while processing payment: " + e.getMessage());
	    }
	}

	// Optional: Method to clear text fields
	private void clearFields() {
	    account_no_field.clear();
	    connection_no_field.clear();
	    meter_no_field.clear();
	    connection_type_field.clear();
	    name_field.clear();
	    paid_by_field.clear();
	    barangay_field.clear();
	    amount_field.clear();
	    penalty_field.clear();
	    total_field.clear();
	    official_reciept_field.clear();
	}



	 @FXML
	 private void searchByAccountNumber() {
	     String accountNumberText = account_no_field.getText();
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
	                         String barangay = rs.getString("barangay_field");
	                         connection_no_field.setText(accountNo);
	                         connection_type_field.setText(connectionType);
	                         meter_no_field.setText(meterNo);
	                         connection_type_field.setText(connectionType);
	                         name_field.setText(fullName);
	                         barangay_field.setText(barangay);
	                         
	                         System.out.print(connectionType + "\n");
	                         
	                         // After basic data is fetched, retrieve the meter reading data
//	                         fetchReadingData(accountNumber, conn);
//	                		 billing_Number_TV.setCellValueFactory(new PropertyValueFactory<>("billingEntryCount"));
//	                		 billing_Period_TV.setCellValueFactory(new PropertyValueFactory<>("billingPeriod"));
//	                		 total_Amount_Due_TV.setCellValueFactory(new PropertyValueFactory<>("amountPayable"));
//	                		 
	                		 
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

	 @FXML
	 public void loadToTable() {
	     ObservableList<BillingRecord> billingRecords = FXCollections.observableArrayList();

	     double totalAmountDue = 0; // To track total amount due for calculating penalties
	     double totalPenalty = 0; // To track the total penalty amount

	     // Custom date for testing purposes (for example, December 31, 2024)
	     LocalDate customCurrentDate = LocalDate.of(2024, 12, 31);  // Use any date you'd like for testing

	     // Get the value of the account number from the text field
	     String accountNumberText = account_no_field.getText();
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


    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

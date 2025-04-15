package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Posting_Conntroller {
	Stage stage;
	
	@FXML
	private TextField search_field;
	
	@FXML
	private TextField current_reading;

	@FXML
	private TextField current_reading_date;

	@FXML
	private TextField previous_reading;

	@FXML
	private TextField previous_reading_date;

	@FXML
	private TextField reading_by, consumption;
	
	@FXML
	private TextField account_no_field, basic_charges, succeeding_charge, excess, basic_amount, succeeding_amount, total_amount;
	
	@FXML
	private TextField basic_charge1, succeeding_charge1, excess1, basic_ammount1, succeeding_amount1, total_amount1, current, read_by1;
	
	@FXML
	private TextField  previous_reading1, reading_by1, consumption1;
	
	@FXML
	private DatePicker previous_reading_date1, current_reading_date1;
	
	@FXML
	private TextField meter_no_field, connection_type_field, name_field, barangay_field, company, connection_no_field;
	
	private int added_consumption, total;
	private int firstm3;
	
	@FXML
	public void clearFields() {
	    // Clear all TextFields
	    current_reading.clear();
	    current_reading_date.clear();
	    previous_reading.clear();
	    previous_reading_date.clear();
	    reading_by.clear();
	    consumption.clear();
	    account_no_field.clear();
	    basic_charges.clear();
	    succeeding_charge.clear();
	    excess.clear();
	    basic_amount.clear();
	    succeeding_amount.clear();
	    total_amount.clear();
	    basic_charge1.clear();
	    succeeding_charge1.clear();
	    excess1.clear();
	    basic_ammount1.clear();
	    succeeding_amount1.clear();
	    total_amount1.clear();
	    current.clear();
	    read_by1.clear();
	    previous_reading1.clear();
	    reading_by1.clear();
	    consumption1.clear();
	    meter_no_field.clear();
	    connection_type_field.clear();
	    name_field.clear();
	    barangay_field.clear();
	    company.clear();
	    connection_no_field.clear();

	    // Clear all DatePickers
	    previous_reading_date1.setValue(null);
	    current_reading_date1.setValue(null);

	    // Reset integer variables
	    added_consumption = 0;
	    total = 0;
	    firstm3 = 0;
	}

	
	@FXML
	public void search(ActionEvent event) throws SQLException {
	    // Get the account number from the search field
	    String accountNumberStr = search_field.getText();

	    // Validate the input
	    if (accountNumberStr.isEmpty()) {
	        showAlert(AlertType.WARNING, "Notice", "Please enter your account number");
	        return;
	    }

	    // Parse the account number to a long
	    long accountNumber;
	    try {
	        accountNumber = Long.parseLong(accountNumberStr);
	    } catch (NumberFormatException e) {
	        System.out.println("Invalid account number format");
	        showAlert(AlertType.ERROR, "Invalid", "Invalid account number");
	        return;
	    }

	    // Fetch the reading data from the database
	    String query = "SELECT account_no, reading_this_month, reading_this_month_date, reading_last_month, reading_last_month_date, read_by, amount_payable "
	            + "FROM meter_readings "
	            + "WHERE billing_entry_count = ?";

	    Connection conn = DatabaseConnector.connect();
	    PreparedStatement stmt = conn.prepareStatement(query);
	    stmt.setLong(1, accountNumber); // Use setLong instead of setString

	    ResultSet rs = stmt.executeQuery();
	    if (rs != null && rs.next()) {
	        // Get the reading data
	        long readingThisMonth = rs.getLong("reading_this_month");
	        java.sql.Date readingThisMonthDate = rs.getDate("reading_this_month_date");
	        long readingLastMonth = rs.getLong("reading_last_month");
	        java.sql.Date readingLastMonthDate = rs.getDate("reading_last_month_date");
	        String readBy = rs.getString("read_by");
	        total = (int) rs.getDouble("amount_payable");
	        String accountNumberStrFromDB = rs.getString("account_no");// Add this line

	        // Set the reading data to the text fields
	        current_reading.setText(String.valueOf(readingThisMonth));
	        current_reading_date.setText(readingThisMonthDate != null ? readingThisMonthDate.toString() : "No date available");
	        previous_reading.setText(String.valueOf(readingLastMonth));
	        previous_reading_date.setText(readingLastMonthDate != null ? readingLastMonthDate.toString() : "No date available");
	        
	        added_consumption = (int) (readingThisMonth - readingLastMonth);
	        consumption.setText(String.valueOf(added_consumption));
	        
	        current_reading_date1.setValue(readingThisMonthDate != null ? readingThisMonthDate.toLocalDate() : null);
	        previous_reading1.setText(String.valueOf(readingLastMonth));
	        previous_reading_date1.setValue(readingLastMonthDate != null ? readingLastMonthDate.toLocalDate() : null);
	        
	        added_consumption = (int) (readingThisMonth - readingLastMonth);
	        
	        reading_by.setText(readBy);
	        
	        fetchMeterAccount(accountNumberStrFromDB); // Update this line
	    } else {
	        System.out.println("No reading data found for account number: " + accountNumberStr);
	        showAlert(AlertType.ERROR, "Check you ACCOUNT NUMBER", "No reading data found for accoung number" + accountNumberStr);
	    }
	}
	
	private void fetchMeterAccount(String accountNumber) {
	    try {
	        // Fetch the meter account data from the database
	        String query = "SELECT connection_field, meter_no, connectiontype_cb, first_name, middle_name, last_name, barangay_field, company "
	                + "FROM meter_connection "
	                + "WHERE accountno_field = ?";

	        Connection conn = DatabaseConnector.connect();
	        PreparedStatement stmt = conn.prepareStatement(query);
	        stmt.setString(1, accountNumber);

	        ResultSet rs = stmt.executeQuery();

	        if (rs != null && rs.next()) {
	            // Get the meter account data
	            int connectionNo = rs.getInt("connection_field");
	            String meterNo = rs.getString("meter_no");
	            String connectionType = rs.getString("connectiontype_cb");
	            String firstName = rs.getString("first_name");
	            String middleName = rs.getString("middle_name");
	            String lastName = rs.getString("last_name");
	            String barangay = rs.getString("barangay_field");
	            String company1 = rs.getString("company");

	            // Set the meter account data to the fields
	            account_no_field.setText(String.valueOf(accountNumber));
	            connection_no_field.setText(String.valueOf(connectionNo));
	            meter_no_field.setText(meterNo);
	            connection_type_field.setText(connectionType);
	            name_field.setText(firstName + " " + middleName + " " + lastName);
	            barangay_field.setText(barangay);
	            company.setText(company1);

	            fetchBasicCharges(connectionType);
	        } else {
	            System.out.println("No meter account data found for account number: " + accountNumber);
	            showAlert(AlertType.ERROR, "No Date", "No meter account data found for account number: " + accountNumber);
	            
	        }
	    } catch (SQLException e) {
	        System.out.println("Error fetching meter account data: " + e.getMessage());
	    }
	}
	
	private void fetchBasicCharges(String connectionType) {
	    try {
	        // Fetch the basic charges data with the highest `id`
	        String query;
	        if (connectionType.equals("Residential")) {
	            query = "SELECT monthly_basic, initial_m3, additional_per_m3 FROM residential_charges ORDER BY id DESC LIMIT 1";
	        } else if (connectionType.equals("Commercial")) {
	            query = "SELECT monthly_basic, initial_m3, additional_per_m3 FROM commercial_charges ORDER BY id DESC LIMIT 1";
	        } else {
	            System.out.println("Invalid connection type");
	            return;
	        }

	        Connection conn = DatabaseConnector.connect();
	        PreparedStatement stmt = conn.prepareStatement(query);

	        ResultSet rs = stmt.executeQuery();

	        if (rs != null && rs.next()) {
	            // Get the basic charges data
	            double monthlyBasic = rs.getDouble("monthly_basic");
	            double additionalPerM3 = rs.getDouble("additional_per_m3");
	            firstm3 = rs.getInt("initial_m3");

	            // Set the basic charges data to the text fields
	            basic_charges.setText(String.valueOf(monthlyBasic));
	            succeeding_charge.setText(String.valueOf(additionalPerM3));

	            // Calculate and set the excess and amount
	            
	            if (added_consumption <= firstm3) {
	            	excess.setText("0");
	            }else {
		            excess.setText(String.valueOf(firstm3 - added_consumption));
	            }
	            
	            basic_amount.setText(String.valueOf(monthlyBasic));
	            
	            double computed_succeeding_ammount = additionalPerM3 * Double.parseDouble(excess.getText());
	            succeeding_amount.setText(String.valueOf(computed_succeeding_ammount));
	            
	            total_amount.setText(String.valueOf(total));
	        } else {
	            System.out.println("No basic charges data found");
	            showAlert(AlertType.ERROR, "No basic charges data foundl", "Check your database");
	        }
	    } catch (SQLException e) {
	        System.out.println("Error fetching basic charges data: " + e.getMessage());
	        showAlert(AlertType.ERROR, "Error fetching basic charges data", "No Basic Charges Found");
	        
	    }
	}

	public void newTotal() {
			
	        int consumption2 = Integer.parseInt(current.getText()) - Integer.parseInt(previous_reading.getText());
	        
	        basic_charge1.setText(basic_charges.getText());
	        succeeding_charge1.setText(succeeding_charge.getText());
	        
	        consumption1.setText(String.valueOf(consumption2));
	        System.out.print(firstm3);
            if (consumption2 <= firstm3) {
            	excess1.setText("0");
            }else {
	            excess1.setText(String.valueOf(consumption2 - firstm3));
            }
            
	        basic_ammount1.setText(basic_amount.getText());
	        
	        double sceedamt =  Double.parseDouble(succeeding_charge1.getText()) * Double.parseDouble(excess1.getText());
	        succeeding_amount1.setText(String.valueOf(sceedamt));
	        
	        Double tlt = Double.parseDouble(basic_ammount1.getText()) + Double.parseDouble(succeeding_amount1.getText());
	        
	        total_amount1.setText(String.valueOf(tlt));
		}
	
	@FXML
	public void update() {
	    try (Connection conn = DatabaseConnector.connect()) {
	        if (conn != null) {
	            String updateSQL = "UPDATE meter_readings SET "
	                    + "reading_this_month = ?, "
	                    + "reading_this_month_date = ?, "
	                    + "amount_payable = ?, "
	                    + "read_by = ? "
	                    + "WHERE billing_entry_count = ?";

	            PreparedStatement pstmt = conn.prepareStatement(updateSQL);
	            pstmt.setLong(1, Long.parseLong(current.getText()));
	            pstmt.setDate(2, java.sql.Date.valueOf(current_reading_date1.getValue()));
	            pstmt.setLong(3, Long.parseLong(consumption1.getText()));
	            pstmt.setString(4, read_by1.getText());
	            pstmt.setLong(5, Long.parseLong(search_field.getText()));

	            pstmt.executeUpdate();
	            System.out.println("Meter reading updated successfully!");
	            showAlert(AlertType.INFORMATION, "Update Successful", "Meter reading updated successfully!");

	            clearFields();
	        }
	    } catch (SQLException e) {
	        System.out.println("Error updating meter reading: " + e.getMessage());
	    }
	}
	
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
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

}

package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Hompage_Controller {
	private Stage stage;
	
	@FXML
	private HBox reporting;
	@FXML
	private Button allowReading;
	
	private boolean permission;
	
	public void initialize() throws SQLException {
		checkPermission();
	}
	
	public void navigation(String switchPage, ActionEvent event) throws IOException, SQLException {
        // Load the new FXML page
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));
        
        // Get the current stage from the event source
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        stage.setResizable(false);
        stage.setResizable(true);
        stage.setResizable(false);
        stage.getScene().setRoot(newRoot);
        
    }

	
	public void permissioonReading(ActionEvent event) throws IOException {
		checkPermission();
		boolean per = true;
		if (permission) {
			per = false;
		}else if (!permission) {
			per = true;
		}
		
	    try (Connection conn = DatabaseConnector.connect()) { 
	        DatabaseConnector.setPermissionReading(conn, per); 
	        System.out.println("Permission updated successfully " + per);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void gotoConnection(ActionEvent event) throws IOException, SQLException {
		navigation("Connection", event);
	}
	
	public void gotoReading(ActionEvent event) throws IOException, SQLException {
		navigation("Meter_Reading", event);
	}
	
	public void gotoBillsAndPayment(ActionEvent event) throws IOException, SQLException {
		navigation("Bills_And_Payment", event);
	}
	
	public void gotoConsumerInfo(ActionEvent event) throws IOException, SQLException {
		navigation("Consumer_Information", event);
	}
	
	public void gotoUserManagement(ActionEvent event) throws IOException, SQLException {
		navigation("User_Management", event);
	}
	
	public void gotoBasicCharges(ActionEvent event) throws IOException {
		BasicChargesController basicCharges = new BasicChargesController();
		basicCharges.openNewWindow("BasicCharges");
	}
	
	public void gotoReports(ActionEvent event) throws IOException {
		reporting.setVisible(true);
	}
	
	@FXML
	public void gotoAnalytics(ActionEvent event) throws IOException, SQLException {
		navigation("Analytics", event);
	}
	
	@FXML
	public void gotoPosting(ActionEvent event) throws IOException, SQLException {
		navigation("Posting", event);
	}
	
	@FXML
	public void gotoTransaction(ActionEvent event) throws IOException, SQLException {
		navigation("Transaction_History", event);
	}
	
	@FXML 
	public void gotoAnnouncement(ActionEvent event) throws IOException, SQLException {
		navigation("Announcement", event);
	}
	
	@FXML 
	public void billingSummary(ActionEvent event)throws IOException, SQLException {
		navigation("Billing_Summary", event);
	}
	//---------------MASTERLIST-----------------
	public void closeReporting(ActionEvent event) {
		reporting.setVisible(false);
	}
	
	public void gotomasterList(ActionEvent event) throws IOException, SQLException {
		navigation("Consumer_Master_List", event);
	}
	@FXML
	public void gotoBarangayMasterList(ActionEvent event)throws IOException, SQLException {
		navigation("Barangay_Master_List", event);
	}
	
	@FXML
	public void gotoDisconnection(ActionEvent event) throws IOException, SQLException  {
		navigation("Disconnection", event);
	}
	
	@FXML
	public void gotoForDissconnection(ActionEvent event) throws IOException, SQLException  {
		navigation("For_Disconnection", event);
	}
	
	public void logOut(ActionEvent event) throws IOException, SQLException  {
		 navigation("Main", event);
	}
	
	
	public void checkPermission() {
	    try (Connection conn = DatabaseConnector.connect()) {
	        permission = DatabaseConnector.getPermissionReading(conn);
	        System.out.println(permission);

	        // Update button text based on permission value
	        allowReading.setText(permission ? "Allow Reading" : " Disable Reading");
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
}
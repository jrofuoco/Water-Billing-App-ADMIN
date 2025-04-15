package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Barangay_Master_List_Controller {
	
	Stage stage;
	
	@FXML
	private StackPane addBarangay_container;
	
	@FXML
	private TextField barangay_name, brgy_code;
	
	@FXML
	private TableView<Barangay> tableView;

	@FXML
	private TableColumn<Barangay, String> accountColumn;

	@FXML
	private TableColumn<Barangay, String> barangayColumn;
	
	@FXML
	private void initialize() {
	    fetchAllBarangay();
	    accountColumn.setCellValueFactory(new PropertyValueFactory<>("brgyCode"));
	    barangayColumn.setCellValueFactory(new PropertyValueFactory<>("barangayName"));
	}
	
	@FXML
	private void fetchAllBarangay() {
	    String query = "SELECT * FROM barangay";

	    try (Connection connection = DatabaseConnector.connect();
	         Statement statement = connection.createStatement();
	         ResultSet resultSet = statement.executeQuery(query)) {

	        // Clear the existing data in the TableView
	        tableView.getItems().clear();

	        // Set the cell value factories for the TableColumn
	        accountColumn.setCellValueFactory(new PropertyValueFactory<>("brgyCode"));
	        barangayColumn.setCellValueFactory(new PropertyValueFactory<>("barangayName"));

	        while (resultSet.next()) {
	            String brgyCode = resultSet.getString("brgy_code");
	            String barangayName = resultSet.getString("barangay_name");

	            // Create a new Barangay object
	            Barangay barangay = new Barangay(brgyCode, barangayName);

	            // Add the Barangay object to the TableView
	            tableView.getItems().add(barangay);
	        }
	    } catch (SQLException e) {
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Error Fetching Barangay");
	        alert.setHeaderText(null);
	        alert.setContentText("Error fetching barangay: " + e.getMessage());
	        alert.showAndWait();
	    }
	}
	
	@FXML
	private void showAddBarangay() {
		addBarangay_container.setVisible(true);
	}
	
	@FXML
	private void closeAddBarangay() {
		addBarangay_container.setVisible(false);
	}
	
	@FXML
	private void removebarangay() {
	    String barangay_name1 = barangay_name.getText();
	    String brgy_code1 = brgy_code.getText();

	    if (barangay_name1.isEmpty() || brgy_code1.isEmpty()) {
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Error Removing Barangay");
	        alert.setHeaderText(null);
	        alert.setContentText("Please fill in all fields.");
	        alert.showAndWait();
	        return;
	    }

	    try {
	        int brgyCode = Integer.parseInt(brgy_code1);

	        String query = "DELETE FROM barangay WHERE barangay_name = ? AND brgy_code = ?";

	        try (Connection connection = DatabaseConnector.connect();
	             PreparedStatement statement = connection.prepareStatement(query)) {

	            statement.setString(1, barangay_name1);
	            statement.setInt(2, brgyCode);

	            if (statement.executeUpdate() > 0) {
	                Alert alert = new Alert(Alert.AlertType.INFORMATION);
	                alert.setTitle("Barangay Removed");
	                alert.setHeaderText(null);
	                alert.setContentText("Barangay removed successfully!");
	                alert.showAndWait();

	                // Clear the text fields
	                barangay_name.clear();
	                brgy_code.clear();
	            } else {
	                Alert alert = new Alert(Alert.AlertType.ERROR);
	                alert.setTitle("Error Removing Barangay");
	                alert.setHeaderText(null);
	                alert.setContentText("Barangay not found.");
	                alert.showAndWait();
	            }
	        } catch (SQLException e) {
	            Alert alert = new Alert(Alert.AlertType.ERROR);
	            alert.setTitle("Error Removing Barangay");
	            alert.setHeaderText(null);
	            alert.setContentText("Error removing barangay: " + e.getMessage());
	            alert.showAndWait();
	        }
	    } catch (NumberFormatException e) {
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Error Removing Barangay");
	        alert.setHeaderText(null);
	        alert.setContentText("Invalid brgy_code. Please enter a valid integer.");
	        alert.showAndWait();
	    }
	}
	@FXML
	private void addBarangay() {
	    String barangay_name1 = barangay_name.getText();
	    String brgy_code1 = brgy_code.getText();

	    if (barangay_name1.isEmpty() || brgy_code1.isEmpty()) {
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Error Adding Barangay");
	        alert.setHeaderText(null);
	        alert.setContentText("Please fill in all fields.");
	        alert.showAndWait();
	        return;
	    }

	    try {
	        
	        String query = "INSERT INTO barangay (barangay_name, brgy_code) VALUES (?, ?)";

	        try (Connection connection = DatabaseConnector.connect();
	             PreparedStatement statement = connection.prepareStatement(query)) {

	            statement.setString(1, barangay_name1);
	            statement.setString(2, brgy_code1);

	            statement.executeUpdate();

	            Alert alert = new Alert(Alert.AlertType.INFORMATION);
	            alert.setTitle("Barangay Added");
	            alert.setHeaderText(null);
	            alert.setContentText("Barangay added successfully!");
	            alert.showAndWait();

	            // Clear the text fields
	            barangay_name.clear();
	            brgy_code.clear();
	        } catch (SQLException e) {
	            Alert alert = new Alert(Alert.AlertType.ERROR);
	            alert.setTitle("Error Adding Barangay");
	            alert.setHeaderText(null);
	            alert.setContentText("Error adding barangay: " + e.getMessage());
	            alert.showAndWait();
	        }
	    } catch (NumberFormatException e) {
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Error Adding Barangay");
	        alert.setHeaderText(null);
	        alert.setContentText("Invalid brgy_code. Please enter a valid integer.");
	        alert.showAndWait();
	    }
	}
	
    public void navigation(String switchPage, ActionEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(newRoot);
    }

    public void back(ActionEvent event) throws IOException {
        navigation("Homepage", event);
    }
    
    public class Barangay {
        private String brgyCode;
        private String barangayName;

        public Barangay(String brgyCode, String barangayName) {
            this.brgyCode = brgyCode;
            this.barangayName = barangayName;
        }

        // Getters and setters
        public String getBrgyCode() {
            return brgyCode;
        }

        public void setBrgyCode(String brgyCode) {
            this.brgyCode = brgyCode;
        }

        public String getBarangayName() {
            return barangayName;
        }

        public void setBarangayName(String barangayName) {
            this.barangayName = barangayName;
        }
    }
    
}

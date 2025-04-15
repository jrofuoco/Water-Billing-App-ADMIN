package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class BasicChargesController implements Initializable {

    @FXML
    private Stage stage;
    
    // Text fields for residential charges
    @FXML
    private TextField monthly_basic, additional_per_m3, penalty, initial_m3, meter_fee, connectionFee_Field;
    
    // Text fields for commercial charges
    @FXML
    private TextField monthly_basic1, additional_per_m3_1, penalty1, initial_m3_1, meter_fee1, connectionFee_Field1;

    // Method that will be called during initialization
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load the latest data for both residential and commercial charges
        loadLatestResidentialCharges();
        loadLatestCommercialCharges();
    }

    // Method to load the latest residential charges and set the values in the text fields
    private void loadLatestResidentialCharges() {
        String selectSQL = "SELECT * FROM residential_charges ORDER BY created_at DESC LIMIT 1";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement pstmt = connection.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                // Set the values in the text fields for residential charges
                monthly_basic.setText(String.valueOf(rs.getLong("monthly_basic")));
                additional_per_m3.setText(String.valueOf(rs.getDouble("additional_per_m3")));
                penalty.setText(String.valueOf(rs.getDouble("penalty")));
                initial_m3.setText(String.valueOf(rs.getDouble("initial_m3")));
                meter_fee.setText(String.valueOf(rs.getDouble("meter_fee")));
                connectionFee_Field.setText(String.valueOf(rs.getDouble("connection_fee"))); // Added for connectionFee
            }
        } catch (SQLException e) {
            System.out.println("Failed to load latest residential charges: " + e.getMessage());
        }
    }

    // Method to load the latest commercial charges and set the values in the text fields
    private void loadLatestCommercialCharges() {
        String selectSQL = "SELECT * FROM commercial_charges ORDER BY created_at DESC LIMIT 1";

        try (Connection connection = DatabaseConnector.connect();
             PreparedStatement pstmt = connection.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                // Set the values in the text fields for commercial charges
                monthly_basic1.setText(String.valueOf(rs.getLong("monthly_basic")));
                additional_per_m3_1.setText(String.valueOf(rs.getDouble("additional_per_m3")));
                penalty1.setText(String.valueOf(rs.getDouble("penalty")));
                initial_m3_1.setText(String.valueOf(rs.getDouble("initial_m3")));
                meter_fee1.setText(String.valueOf(rs.getDouble("meter_fee")));
                connectionFee_Field1.setText(String.valueOf(rs.getDouble("connection_fee"))); // Added for connectionFee
            }
        } catch (SQLException e) {
            System.out.println("Failed to load latest commercial charges: " + e.getMessage());
        }
    }

    // Method to handle the Save button click event
    @FXML
    private void handleSaveButtonClick(ActionEvent event) {
        try {
            // Fetch values from the residential charges text fields and parse to appropriate types
            long monthlyBasicValueRes = Long.parseLong(monthly_basic.getText());
            double additionalPerM3ValueRes = Double.parseDouble(additional_per_m3.getText());
            double penaltyValueRes = Double.parseDouble(penalty.getText());
            double initialM3ValueRes = Double.parseDouble(initial_m3.getText());
            double meterFeeValueRes = Double.parseDouble(meter_fee.getText());
            double connectionFeeValuesRes = Double.parseDouble(connectionFee_Field.getText()); // From residential TextField

            // Fetch values from the commercial charges text fields and parse to appropriate types
            long monthlyBasicValueCom = Long.parseLong(monthly_basic1.getText());
            double additionalPerM3ValueCom = Double.parseDouble(additional_per_m3_1.getText());
            double penaltyValueCom = Double.parseDouble(penalty1.getText());
            double initialM3ValueCom = Double.parseDouble(initial_m3_1.getText());
            double meterFeeValueCom = Double.parseDouble(meter_fee1.getText());
            double connectionFeeValuesResCom = Double.parseDouble(connectionFee_Field1.getText()); // From commercial TextField

            // Establish connection to the database
            Connection connection = DatabaseConnector.connect();

            // Insert values into the residential_charges table
            insertResidentialCharges(connection, monthlyBasicValueRes, additionalPerM3ValueRes, penaltyValueRes, initialM3ValueRes, meterFeeValueRes, connectionFeeValuesRes);

            // Insert values into the commercial_charges table
            insertCommercialCharges(connection, monthlyBasicValueCom, additionalPerM3ValueCom, penaltyValueCom, initialM3ValueCom, meterFeeValueCom, connectionFeeValuesResCom);

        } catch (NumberFormatException e) {
            System.out.println("Error: One or more fields have invalid input. " + e.getMessage());
        }
    }

    // Method to insert data into the residential_charges table
    private void insertResidentialCharges(Connection connection, long monthlyBasic, double additionalPerM3, double penalty, double initialM3, double meterFee, double connectionFee) {
        String insertSQL = "INSERT INTO residential_charges (monthly_basic, additional_per_m3, penalty, initial_m3, meter_fee, connection_fee) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setLong(1, monthlyBasic);
            pstmt.setDouble(2, additionalPerM3);
            pstmt.setDouble(3, penalty);
            pstmt.setDouble(4, initialM3);
            pstmt.setDouble(5, meterFee);
            pstmt.setDouble(6, connectionFee); // Insert the residential connection fee

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Residential data inserted successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to insert residential data: " + e.getMessage());
        }
    }

    // Method to insert data into the commercial_charges table
    private void insertCommercialCharges(Connection connection, long monthlyBasic, double additionalPerM3, double penalty, double initialM3, double meterFee, double connectionFee) {
        String insertSQL = "INSERT INTO commercial_charges (monthly_basic, additional_per_m3, penalty, initial_m3, meter_fee, connection_fee) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setLong(1, monthlyBasic);
            pstmt.setDouble(2, additionalPerM3);
            pstmt.setDouble(3, penalty);
            pstmt.setDouble(4, initialM3);
            pstmt.setDouble(5, meterFee);
            pstmt.setDouble(6, connectionFee); // Insert the commercial connection fee

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Commercial data inserted successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to insert commercial data: " + e.getMessage());
        }
    }

    public void openNewWindow(String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile + ".fxml"));
        root.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        Stage newStage = new Stage();
        Scene newScene = new Scene(root);
        newStage.setScene(newScene);
        newStage.show();
    }
    
    @FXML
    public void back(ActionEvent event) throws IOException {
        Node source = (Node) event.getSource();
        Stage currentStage = (Stage) source.getScene().getWindow();
        currentStage.close();
    }
}

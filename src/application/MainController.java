package application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.Node;

public class MainController {
    private Stage stage;
    
    @FXML
    private TextField username_textfield;
    
    @FXML
    private PasswordField password_passwordfield;
    
    @FXML
    private void login(ActionEvent event) throws IOException {
        // Get user input
        String username = username_textfield.getText().trim();
        String password = password_passwordfield.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Both fields are required.");
            return;
        }

        // Database interaction
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT password FROM accounts_admin WHERE email = ?")) {

            // Set the username (email) parameter
            pstmt.setString(1, username);

            // Execute query
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");

                String hashedInputPassword = hashPassword(password);

                if (storedHashedPassword.equals(hashedInputPassword)) {
                    //showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + username + "!");
                    System.out.println("Login successful for user: " + username);
                    navigation("HomePage", event); // Pass the actual ActionEvent
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid password.");
                    System.out.println("Invalid password for user: " + username);
                }
            } else {

                showAlert(Alert.AlertType.ERROR, "Login Failed", "Email not found.");
                System.out.println("Email not found: " + username);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while accessing the database.");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error hashing password: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Security Error", "An error occurred while hashing the password.");
        }
    }


    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // Method to hash the password using SHA-256
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Method to handle navigation without changing window size
    public void navigation(String switchPage, ActionEvent event) throws IOException {
        // Load the new FXML page
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));
        
        // Get the current stage from the event source
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // Replace the root of the existing scene without resizing the window
        stage.getScene().setRoot(newRoot);
    }

    // Method to navigate to the homepage (or any other page)
    public void gotoHomePage(ActionEvent event) throws IOException {
        navigation("Homepage", event);
    }
}

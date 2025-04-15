package application;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class User_Management_Controller {

    @FXML
    private TextField account_no_field, first_name_field, middle_initital_field, last_name_field, contact_field, email_field, search_field;

    @FXML
    private PasswordField password_pass, retype_pass;

    @FXML
    private Button add_user_btn, cancel_btn, save_btn;

    @FXML
    private TableView<AccountsAdmin> user_table;

    @FXML
    private TableColumn<AccountsAdmin, Integer> idColumn;

    @FXML
    private TableColumn<AccountsAdmin, String> firstNameColumn;

    @FXML
    private TableColumn<AccountsAdmin, String> lastNameColumn;

    @FXML
    private TableColumn<AccountsAdmin, String> miColumn;

    @FXML
    private TableColumn<AccountsAdmin, String> positionColumn;

    @FXML
    private TableColumn<AccountsAdmin, String> mobileNoColumn;

    @FXML
    private TableColumn<AccountsAdmin, Void> edit_column;

    @FXML
    private ChoiceBox<String> position_CB;
    
    private Stage stage;

    private ObservableList<AccountsAdmin> accountsAdminList = FXCollections.observableArrayList();
    private ObservableList<AccountsAdmin> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(account_no) FROM accounts_admin")) {

            if (rs.next()) {
                int maxConnectionNo = rs.getInt(1);
                account_no_field.setText(String.valueOf(maxConnectionNo + 1)); // increment the max value by 1
            }

        } catch (SQLException e) {
            System.out.println("Error fetching max connection number: " + e.getMessage());
        }
        
        position_CB.setItems(FXCollections.observableArrayList(
                "Admin", "Treasury", "Reader"
        ));
        position_CB.setValue("Admin");

        fetchAccountsAdmin();
        setupSearch();
    }
    
    private void setupSearch() {
        search_field.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTableData(newValue);
        });
    }

    private void filterTableData(String query) {
        filteredList.clear();

        if (query == null || query.isEmpty()) {
            filteredList.addAll(accountsAdminList); // If query is empty, show all data
        } else {
            for (AccountsAdmin accountsAdmin : accountsAdminList) {
                if (accountsAdmin.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    accountsAdmin.getLastName().toLowerCase().contains(query.toLowerCase()) ||
                    accountsAdmin.getPosition().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(accountsAdmin);
                }
            }
        }
        
        user_table.setItems(filteredList);
    }

    @FXML
    private void save() {
        if (
                account_no_field.getText().isEmpty() ||
                first_name_field.getText().isEmpty() ||
                middle_initital_field.getText().isEmpty() ||
                last_name_field.getText().isEmpty() ||
                position_CB.getValue() == null ||
                contact_field.getText().isEmpty() ||
                email_field.getText().isEmpty() ||
                password_pass.getText().isEmpty()
        ) {
            showAlert(AlertType.ERROR, "Error", "Please fill in all fields.");
        } else {
            try (Connection conn = DatabaseConnector.connect();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "UPDATE accounts_admin SET first_name = ?, middle_initial = ?, last_name = ?, contact_number = ?, email = ?, password = ? WHERE account_no = ?")) {

                // Retrieve values from the text fields
                String firstName = first_name_field.getText();
                String middleInitial = middle_initital_field.getText();
                String lastName = last_name_field.getText();
                String contactNumber = contact_field.getText();
                String email = email_field.getText();
                String password = password_pass.getText();
                String reTypedPassword = retype_pass.getText();

                // Validate and hash password
                if (password.equals(reTypedPassword)) {
                    String hashedPassword = hashPassword(password);

                    // Parse account number to integer
                    int accountNo;
                    try {
                        accountNo = Integer.parseInt(account_no_field.getText());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid account number: " + e.getMessage());
                        showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid account number.");
                        return;
                    }

                    // Set parameters for the query
                    pstmt.setString(1, firstName);
                    pstmt.setString(2, middleInitial);
                    pstmt.setString(3, lastName);
                    pstmt.setString(4, contactNumber);
                    pstmt.setString(5, email);
                    pstmt.setString(6, hashedPassword);
                    pstmt.setInt(7, accountNo);

                    // Execute update
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Account details updated successfully!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "No account found with the given account number.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match!");
                }

            } catch (SQLException e) {
                System.out.println("Error updating account: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while updating the account.");
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Error hashing password: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Security Error", "An error occurred while hashing the password.");
            }
        }
    }

    private void fetchAccountsAdmin() {
        ObservableList<AccountsAdmin> accountsAdminList = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT account_no, first_name, middle_initial, last_name, position, contact_number FROM accounts_admin")) {

            while (rs.next()) {
                AccountsAdmin accountsAdmin = new AccountsAdmin(
                        rs.getInt("account_no"),
                        rs.getString("first_name"),
                        rs.getString("middle_initial"),
                        rs.getString("last_name"),
                        rs.getString("position"),
                        rs.getString("contact_number")
                );

                accountsAdminList.add(accountsAdmin);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching accounts admin: " + e.getMessage());
        }

        // Set the items of the TableView to the new list to avoid duplicates
        user_table.setItems(accountsAdminList);

        // Set up columns for the TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("accountNo"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        miColumn.setCellValueFactory(new PropertyValueFactory<>("middleInitial"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        mobileNoColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        // Set up the edit button for each row
        edit_column.setCellFactory(param -> new TableCell<>() {
            private final Button button = new Button();
            BorderPane borderPane = new BorderPane();

            {
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
                    AccountsAdmin accountsAdmin = getTableView().getItems().get(getIndex());
                    if (accountsAdmin != null) {
                        // Populate fields with account data
                        account_no_field.setText(String.valueOf(accountsAdmin.getAccountNo())); // Set the account number field
                        retrieve(accountsAdmin.getAccountNo()); // Retrieve additional details for this account
                    }
                });
                borderPane.setCenter(button);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(borderPane);
                }
                setAlignment(Pos.CENTER);
            }
        });
    }


    private void retrieve(int accountNo) {
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM accounts_admin WHERE account_no = ?")) {

            pstmt.setInt(1, accountNo);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                first_name_field.setText(rs.getString("first_name"));
                middle_initital_field.setText(rs.getString("middle_initial"));
                last_name_field.setText(rs.getString("last_name"));
                contact_field.setText(rs.getString("contact_number"));
                email_field.setText(rs.getString("email"));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving account: " + e.getMessage());
        }
    }

    @FXML
    private void addUser() {
        if (
                account_no_field.getText().isEmpty() ||
                first_name_field.getText().isEmpty() ||
                middle_initital_field.getText().isEmpty() ||
                last_name_field.getText().isEmpty() ||
                position_CB.getValue() == null ||
                contact_field.getText().isEmpty() ||
                email_field.getText().isEmpty() ||
                password_pass.getText().isEmpty()
        ) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
        } else {
            try (Connection conn = DatabaseConnector.connect();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO accounts_admin (account_no, first_name, middle_initial, last_name, position, contact_number, email, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

                pstmt.setInt(1, Integer.parseInt(account_no_field.getText()));
                pstmt.setString(2, first_name_field.getText());
                pstmt.setString(3, middle_initital_field.getText());
                pstmt.setString(4, last_name_field.getText());
                pstmt.setString(5, position_CB.getValue()); 
                pstmt.setString(6, contact_field.getText());
                pstmt.setString(7, email_field.getText());
                pstmt.setString(8, hashPassword(password_pass.getText())); 

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User added successfully!");
                    
                    // Refresh the TableView after adding the user
                    fetchAccountsAdmin(); // Reload the data from the database
                }

            } catch (SQLException | NoSuchAlgorithmException e) {
                System.out.println("Error adding user: " + e.getMessage());
            }
        }
    }



    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(password.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public void navigation(String switchPage, ActionEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setResizable(false);
        stage.getScene().setRoot(newRoot);
    }

    public void back(ActionEvent event) throws IOException {
        navigation("Homepage", event);
    }

    public class AccountsAdmin {
        private int accountNo;
        private String firstName;
        private String middleInitial;
        private String lastName;
        private String position;
        private String contactNumber;

        public AccountsAdmin(int accountNo, String firstName, String middleInitial, String lastName, String position, String contactNumber) {
            this.accountNo = accountNo;
            this.firstName = firstName;
            this.middleInitial = middleInitial;
            this.lastName = lastName;
            this.position = position;
            this.contactNumber = contactNumber;
        }

        public int getAccountNo() {
            return accountNo;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getMiddleInitial() {
            return middleInitial;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPosition() {
            return position;
        }

        public String getContactNumber() {
            return contactNumber;
        }
    }
}

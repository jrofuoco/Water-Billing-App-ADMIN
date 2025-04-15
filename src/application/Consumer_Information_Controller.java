package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Consumer_Information_Controller {
	

	private static ObservableList<Consumer> cachedConsumers;
	
	private Stage stage;
	
	@FXML
	private TableView<Consumer> consumer_TableView;
	
	@FXML
	private TableColumn<Consumer, String> account_no_column;
	
	@FXML
	private TableColumn<Consumer, String> first_name_column;
	
	@FXML
	private TableColumn<Consumer, String> last_name_column;
	
	@FXML
	private TableColumn<Consumer, String> middle_name_column;
	
	@FXML
	private TableColumn<Consumer, String> company_column;
	
	@FXML
	private TableColumn<Consumer, String> mobile_no_column;
	
	@FXML
	private TableColumn<Consumer, String> email_column;
	
	@FXML
	private TableColumn<Consumer, String> barangay_column;
	
	@FXML
	private TableColumn<Consumer, Void> edt_column;
	
	@FXML
	private TextField account_no_field, first_name_field, middle_name_field, last_name_field, company_field, contact_no_field, email_field, barangay_field, barangay_code_field, search_field;

	@FXML
	private Button search_btn;
	
	@FXML
	public void initialize() {
	    if (cachedConsumers == null) {
	        // Fetch data from meter_connection table
	        String query = "SELECT accountno_field, first_name, last_name, middle_name, company, contact_number, barangay_field, email FROM meter_connection";

	        try (Connection conn = DatabaseConnector.connect();
	             Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery(query)) {

	            // Insert data into consumer_TableView
	            cachedConsumers = FXCollections.observableArrayList();

	            while (rs.next()) {
	                Consumer consumer = new Consumer(
	                        rs.getString("accountno_field"),
	                        rs.getString("first_name"),
	                        rs.getString("last_name"),
	                        rs.getString("middle_name"),
	                        rs.getString("company"),
	                        rs.getString("contact_number"),
	                        rs.getString("email"),
	                        rs.getString("barangay_field")
	                );

	                cachedConsumers.add(consumer);
	            }
	        } catch (SQLException e) {
	            System.out.println("Database error: " + e.getMessage());
	        }
	    }

	    // Set the data to the TableView
	    consumer_TableView.setItems(cachedConsumers);

	    // Configure the columns
	    account_no_column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccountNo()));
	    first_name_column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFirstName()));
	    last_name_column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));
	    middle_name_column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMiddleName()));
	    company_column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCompany()));
	    mobile_no_column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMobileNo()));
	    email_column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
	    barangay_column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBarangay()));

	    // Center-align the columns
	    for (TableColumn<Consumer, ?> column : consumer_TableView.getColumns()) {
	        column.setStyle("-fx-alignment: CENTER;");
	    }

	    // Add edit button to the edt_column
	    edt_column.setCellFactory(param -> new TableCell<Consumer, Void>() {
	        Button button = new Button();
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
	                Consumer consumer = (Consumer) getTableRow().getItem();
	                System.out.println(consumer.getAccountNo());
	                retrieve(consumer.getAccountNo());
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
	    
	    search_field.textProperty().addListener((observable, oldValue, newValue) -> search());
	}

	
	@FXML
	private void search() {
	    String searchQuery = search_field.getText().toLowerCase();
	    ObservableList<Consumer> filteredList = FXCollections.observableArrayList();

	    if (searchQuery.isEmpty()) {
	        consumer_TableView.setItems(cachedConsumers);
	    } else {
	        for (Consumer consumer : cachedConsumers) {
	            if ((consumer.getAccountNo() != null && consumer.getAccountNo().toLowerCase().contains(searchQuery)) ||
	                (consumer.getFirstName() != null && consumer.getFirstName().toLowerCase().contains(searchQuery)) ||
	                (consumer.getLastName() != null && consumer.getLastName().toLowerCase().contains(searchQuery)) ||
	                (consumer.getMiddleName() != null && consumer.getMiddleName().toLowerCase().contains(searchQuery)) ||
	                (consumer.getCompany() != null && consumer.getCompany().toLowerCase().contains(searchQuery)) ||
	                (consumer.getMobileNo() != null && consumer.getMobileNo().toLowerCase().contains(searchQuery)) ||
	                (consumer.getEmail() != null && consumer.getEmail().toLowerCase().contains(searchQuery)) ||
	                (consumer.getBarangay() != null && consumer.getBarangay().toLowerCase().contains(searchQuery))) {
	                    filteredList.add(consumer);
	            }
	        }

	        consumer_TableView.setItems(filteredList);
	    }
	}

	@FXML 
	private void save() {
	    String query = "UPDATE meter_connection SET "
	            + "first_name = ?, "
	            + "last_name = ?, "
	            + "middle_name = ?, "
	            + "company = ?, "
	            + "contact_number = ?, "
	            + "barangay_field = ?, "
	            + "email = ? "
	            + "WHERE accountno_field = ?";

	    try (Connection conn = DatabaseConnector.connect();
	         PreparedStatement pstmt = conn.prepareStatement(query)) {

	        pstmt.setString(1, first_name_field.getText());
	        pstmt.setString(2, last_name_field.getText());
	        pstmt.setString(3, middle_name_field.getText());
	        pstmt.setString(4, company_field.getText());
	        pstmt.setLong(5, Long.parseLong(contact_no_field.getText()));
	        pstmt.setString(6, barangay_field.getText());
	        pstmt.setString(7, email_field.getText());
	        pstmt.setString(8, account_no_field.getText());

	        pstmt.executeUpdate();
	        System.out.println("Data updated successfully!");
	        
	        loadData();
	    } catch (SQLException e) {
	        System.out.println("Database error: " + e.getMessage());
	    }
	    
	}
	
	public void loadData() {
	    String query = "SELECT accountno_field, first_name, last_name, middle_name, company, contact_number, barangay_field, email FROM meter_connection";
	    
	    try (Connection conn = DatabaseConnector.connect();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {

	        ObservableList<Consumer> consumers = FXCollections.observableArrayList();

	        while (rs.next()) {
	            Consumer consumer = new Consumer(
	                    rs.getString("accountno_field"),
	                    rs.getString("first_name"),
	                    rs.getString("last_name"),
	                    rs.getString("middle_name"),
	                    rs.getString("company"),
	                    rs.getString("contact_number"),
	                    rs.getString("email"),
	                    rs.getString("barangay_field")
	            );

	            consumers.add(consumer);
	        }

	        // Set the data to the TableView
	        consumer_TableView.setItems(consumers);
	        
	    } catch (SQLException e) {
	        System.out.println("Database error: " + e.getMessage());
	    }
	}

	
	@FXML
	private void cancel() {
		account_no_field.setText("");
    	first_name_field.setText("");
        last_name_field.setText("");
        middle_name_field.setText("");
        company_field.setText("");
        contact_no_field.setText("");
        email_field.setText("");
        barangay_field.setText("");
	}
	
	public void retrieve(String account) {
	    String query = "SELECT * FROM meter_connection WHERE accountno_field = '" + account + "'";

	    try (Connection conn = DatabaseConnector.connect();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {

	        if (rs.next()) {
	        	account_no_field.setText(rs.getString("accountno_field"));
	        	first_name_field.setText(rs.getString("first_name"));
	            last_name_field.setText(rs.getString("last_name"));
	            middle_name_field.setText(rs.getString("middle_name"));
	            company_field.setText(rs.getString("company"));
	            contact_no_field.setText(rs.getString("contact_number"));
	            email_field.setText(rs.getString("email"));
	            barangay_field.setText(rs.getString("barangay_field"));
	        } else {
	            System.out.println("No data found for account " + account);
	        }
	    } catch (SQLException e) {
	        System.out.println("Database error: " + e.getMessage());
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
	
	public void back(ActionEvent event) throws IOException {
		navigation("Homepage", event);
	}
}

// Consumer class
class Consumer {
    private String accountNo;
    private String firstName;
    private String lastName;
    private String middleName;
    private String company;
    private String mobileNo;
    private String email;
    private String barangay;

    public Consumer(String accountNo, String firstName, String lastName, String middleName, String company, String mobileNo, String email, String barangay) {
        this.accountNo = accountNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.company = company;
        this.mobileNo = mobileNo;
        this.email = email;
        this.barangay = barangay;
    }

    // Getters and setters
    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }
}
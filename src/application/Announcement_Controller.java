package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class Announcement_Controller {
	
	Stage stage;
	
	@FXML
	private TextField title_field;
	
	@FXML 
	private TextArea content_textarea;
	
	@FXML
	private DatePicker posted_date, expiration_date;
	
	@FXML
	private ChoiceBox<String> barangay;
	
	@FXML
	private Group container_announcement;
	
	@FXML
	private TableView<Announcement> tableView;

	@FXML
	private TableColumn<Announcement, Integer> id;

	@FXML
	private TableColumn<Announcement, String> title;

	@FXML
	private TableColumn<Announcement, String> content;

	@FXML
	private TableColumn<Announcement, java.sql.Timestamp> date_posted;

	@FXML
	private TableColumn<Announcement, Date> expiration;

	@FXML
	private TableColumn<Announcement, String> audience;

	@FXML
	private TableColumn<Announcement, String> status;
	
	@FXML
	private TextField search;
	
	private ObservableList<Announcement> announcementsCache = FXCollections.observableArrayList();
	
	@FXML
	private void initialize() {
	    // Initialize TableView columns
	    id.setCellValueFactory(new PropertyValueFactory<>("id"));
	    title.setCellValueFactory(new PropertyValueFactory<>("title"));
	    content.setCellValueFactory(new PropertyValueFactory<>("content"));
	    date_posted.setCellValueFactory(new PropertyValueFactory<>("datePosted"));
	    expiration.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
	    audience.setCellValueFactory(new PropertyValueFactory<>("audience"));
	    status.setCellValueFactory(new PropertyValueFactory<>("status"));

	    fetchAndCacheAnnouncements(); // Fetch announcements and cache them

	    // Add a listener to the search field
	    search.textProperty().addListener((observable, oldValue, newValue) -> {
	        filterCachedData(newValue);
	    });
	}

	private void fetchAndCacheAnnouncements() {
	    String query = "SELECT * FROM announcement";

	    try (Connection connection = DatabaseConnector.connect();
	         Statement statement = connection.createStatement();
	         ResultSet resultSet = statement.executeQuery(query)) {

	        // Clear the cache before refetching data
	        announcementsCache.clear();

	        while (resultSet.next()) {
	            int id = resultSet.getInt("id");
	            String title = resultSet.getString("title");
	            String content = resultSet.getString("content");
	            java.sql.Timestamp datePosted = resultSet.getTimestamp("date_posted");
	            Date expirationDate = resultSet.getDate("expiration_date");
	            String audience = resultSet.getString("audience");
	            String status = resultSet.getString("status");

	            // Create a new Announcement object and add it to the cache
	            Announcement announcement = new Announcement(id, title, content, datePosted, expirationDate, audience, status);
	            announcementsCache.add(announcement);
	        }

	        // Initially populate the TableView with all data
	        tableView.setItems(announcementsCache);
	    } catch (SQLException e) {
	        System.out.println("SQLException occurred: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	// Filter the cached data based on the search query
	private void filterCachedData(String filter) {
	    if (filter == null || filter.isEmpty()) {
	        // Reset to the full list if no filter is provided
	        tableView.setItems(announcementsCache);
	        return;
	    }

	    // Filter the cached data
	    ObservableList<Announcement> filteredList = announcementsCache.filtered(announcement ->
	        (announcement.getTitle() != null && announcement.getTitle().toLowerCase().contains(filter.toLowerCase())) ||
	        (announcement.getContent() != null && announcement.getContent().toLowerCase().contains(filter.toLowerCase())) ||
	        (announcement.getAudience() != null && announcement.getAudience().toLowerCase().contains(filter.toLowerCase())) ||
	        (announcement.getStatus() != null && announcement.getStatus().toLowerCase().contains(filter.toLowerCase()))
	    );

	    // Update the TableView with the filtered list
	    tableView.setItems(filteredList);
	}


	
	@FXML
	private void recordAnnouncement() {
	    String title = title_field.getText();
	    String barangay1 = barangay.getSelectionModel().getSelectedItem();
	    String content = content_textarea.getText();
	    LocalDate expirationDate = expiration_date.getValue();

	    // Convert LocalDate to Date
	    Date expirationDateSql = Date.valueOf(expirationDate);

	    // Get the current date and time
	    java.sql.Timestamp postedDate = new java.sql.Timestamp(System.currentTimeMillis());

	    // Set the initial status to "Active"
	    String status = "Active";

	    String query = "INSERT INTO announcement (title, content, date_posted, expiration_date, audience, status) VALUES (?, ?, ?, ?, ?, ?)";

	    try (Connection connection = DatabaseConnector.connect();
	         PreparedStatement statement = connection.prepareStatement(query)) {

	        statement.setString(1, title);
	        statement.setString(2, content);
	        statement.setTimestamp(3, postedDate);
	        statement.setDate(4, expirationDateSql);
	        statement.setString(5, barangay1);
	        statement.setString(6, status); // Set the status to "Active"

	        statement.executeUpdate();

	        Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        alert.setTitle("Announcement Recorded");
	        alert.setHeaderText(null);
	        alert.setContentText("Announcement recorded successfully!");
	        alert.showAndWait();
	    } catch (SQLException e) {
	        Alert alert = new Alert(Alert.AlertType.ERROR);
	        alert.setTitle("Error Recording Announcement");
	        alert.setHeaderText(null);
	        alert.setContentText("Error recording announcement: " + e.getMessage());
	        alert.showAndWait();
	    }
	}

	
	@FXML
	private void makeannouncement(ActionEvent event) {
		fetchBarangay();
		container_announcement.setVisible(true);
	}
	
	@FXML 
	private void closeAnnouncement() {
		container_announcement.setVisible(false);
	}

	private void fetchBarangay() {
	    ObservableList<String> barangayNames = FXCollections.observableArrayList();

	    // Add "All" as the first item
	    barangayNames.add("All");

	    String query = "SELECT barangay_name FROM barangay";

	    try (Connection connection = DatabaseConnector.connect();
	         Statement statement = connection.createStatement();
	         ResultSet resultSet = statement.executeQuery(query)) {

	        while (resultSet.next()) {
	            String barangayName = resultSet.getString("barangay_name");
	            barangayNames.add(barangayName);
	        }
	    } catch (SQLException e) {
	        System.out.println("SQLException occurred: " + e.getMessage());
	        e.printStackTrace();
	    }

	    barangay.setItems(barangayNames);
	}

    public void navigation(String switchPage, ActionEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(getClass().getResource(switchPage + ".fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(newRoot);
    }

    public void back(ActionEvent event) throws IOException {
        navigation("Homepage", event);
    }
    
    public class Announcement {
        private int id;
        private String title;
        private String content;
        private java.sql.Timestamp datePosted;
        private Date expirationDate;
        private String audience;
        private String status;

        public Announcement(int id, String title, String content, java.sql.Timestamp datePosted, Date expirationDate, String audience, String status) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.datePosted = datePosted;
            this.expirationDate = expirationDate;
            this.audience = audience;
            this.status = status;
        }

        // Getters and setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public java.sql.Timestamp getDatePosted() {
            return datePosted;
        }

        public void setDatePosted(java.sql.Timestamp datePosted) {
            this.datePosted = datePosted;
        }

        public Date getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(Date expirationDate) {
            this.expirationDate = expirationDate;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

package application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Analytics_Controller {
    Stage stage;
    
    @FXML
    private Text total_revenue_Text, outstanding_balaance_text, active_customers_text, overdue_accounts_text;
    
    @SuppressWarnings("rawtypes")
	@FXML 
    private StackedBarChart barangay_stackedbarchart;
    @FXML
    private PieChart piechart;
    
    @FXML
    public void initialize() {
    	fetchBarangayData();
    	fetchDataInBackground();
    	piechart();
    	//fetchNumberOfUnpaid();
    }

    private void fetchDataInBackground() {
        
        // Fetch and display data
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                // Fetch all the data in the background
                fetchTotalAmountPayable();
                fetchTotalBalance();
                activeTotalCustomers();
                overdueAccounts();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // Update UI after background task completes (if necessary)
                // JavaFX UI updates must be done on the JavaFX Application Thread
            }

            @Override
            protected void failed() {
                super.failed();
                // Handle failure (optional)
                System.out.println("Error occurred while fetching data.");
            }
        };

        // Start the task in a new background thread
        Thread thread = new Thread(task);
        thread.setDaemon(true); // Ensures that the thread doesn't block app shutdown
        thread.start();
    }

    public void fetchTotalAmountPayable() {
        // SQL query to sum the amount_payable where status = 'Paid'
        String query = "SELECT SUM(amount_payable) AS total_amount_payable FROM meter_readings WHERE status = 'Paid'";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                // Retrieve the sum of amount_payable
                double totalAmountPayable = rs.getDouble("total_amount_payable");
                System.out.println("Total Amount Payable (Paid): " + totalAmountPayable);
                // Update the UI on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    total_revenue_Text.setText(String.valueOf(totalAmountPayable));
                });
            }

        } catch (SQLException e) {
            System.out.println("Error fetching total amount payable: " + e.getMessage());
        }
    }

    public void fetchTotalBalance() {
        // SQL query to sum the amount_payable where status = 'Unpaid'
        String query = "SELECT SUM(amount_payable) AS total_amount_payable FROM meter_readings WHERE status = 'Unpaid'";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                // Retrieve the sum of amount_payable
                double totalAmountPayable = rs.getDouble("total_amount_payable");
                System.out.println("Total Amount Unpaid: " + totalAmountPayable);
                // Update the UI on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    outstanding_balaance_text.setText(String.valueOf(totalAmountPayable));
                });
            }

        } catch (SQLException e) {
            System.out.println("Error fetching total amount payable: " + e.getMessage());
        }
    }

    public void activeTotalCustomers() {
        // SQL query to count the number of active customers (disconnection = 'FALSE')
        String query = "SELECT COUNT(*) AS active_customers FROM meter_connection WHERE disconnection = 'FALSE'";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                // Retrieve the count of active customers
                int activeCustomers = rs.getInt("active_customers");
                System.out.println("Active Customers: " + activeCustomers);

                // Update the UI on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    active_customers_text.setText(String.valueOf(activeCustomers));
                });
            }

        } catch (SQLException e) {
            System.out.println("Error fetching active customers count: " + e.getMessage());
        }
    }


    public void overdueAccounts() {
        // SQL query to count distinct unpaid accounts
        String query = "SELECT COUNT(DISTINCT account_no) AS unpaid_accounts FROM meter_readings WHERE status = 'Unpaid'";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                // Retrieve the count of unpaid accounts
                int unpaidAccounts = rs.getInt("unpaid_accounts");
                System.out.println("Total Unpaid Accounts: " + unpaidAccounts);
                // Update the UI on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    overdue_accounts_text.setText(String.valueOf(unpaidAccounts));
                });
            }

        } catch (SQLException e) {
            System.out.println("Error fetching total unpaid accounts: " + e.getMessage());
        }
    }
    
    public void piechart() {
        // SQL query to count the number of paid and unpaid accounts
        String query = "SELECT status, COUNT(*) as count FROM meter_connection GROUP BY status";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Create pie chart data
            int paidCount = 0;
            int unpaidCount = 0;

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");

                if (status.equals("Paid")) {
                    paidCount = count;
                } else if (status.equals("Unpaid")) {
                    unpaidCount = count;
                }
            }

            // Add data to the pie chart
            PieChart.Data slice1 = new PieChart.Data("Paid", paidCount);
            PieChart.Data slice2 = new PieChart.Data("Unpaid", unpaidCount);

            piechart.getData().addAll(slice1, slice2);
        } catch (SQLException e) {
            System.out.println("Error fetching data for pie chart: " + e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
	public void fetchBarangayData() {
        // Clear existing data
        barangay_stackedbarchart.getData().clear();

        // Set title and labels for the chart
        barangay_stackedbarchart.setTitle("Barangay Data");
        ((CategoryAxis) barangay_stackedbarchart.getXAxis()).setLabel("Barangay");
        ((NumberAxis) barangay_stackedbarchart.getYAxis()).setLabel("Count");

        // SQL query to fetch all account numbers
        String accountQuery = "SELECT barangay_field FROM meter_connection";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement accountStmt = conn.prepareStatement(accountQuery);
             ResultSet accountRs = accountStmt.executeQuery()) {

            // Add data to the chart
            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            series1.setName("Barangay Data");

            Map<String, Integer> barangayCounts = new HashMap<>();

            while (accountRs.next()) {
                String barangay = accountRs.getString("barangay_field");

                // Count the occurrences of each barangay
                barangayCounts.put(barangay, barangayCounts.getOrDefault(barangay, 0) + 1);
            }

            // Add data points to the chart
            for (Map.Entry<String, Integer> entry : barangayCounts.entrySet()) {
                series1.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            barangay_stackedbarchart.getData().add(series1);
        } catch (SQLException e) {
            System.out.println("Error fetching barangay data: " + e.getMessage());
        }
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

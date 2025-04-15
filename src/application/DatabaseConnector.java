package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {
    private static final String URL = "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres";
    private static final String USER = "postgres.qedemugeyctrrpxkcjpr";
    private static final String PASSWORD = "09295832037l.";

    // Method to connect to the database
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database successfully!");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }
    
    
    
    public static String getBarangayCode(Connection connection, String barangayCode) {
        String query = "SELECT barangay_name FROM barangay WHERE brgy_code = ?";
        String barangayName = null;

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, barangayCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    barangayName = rs.getString("barangay_name");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch barangay code: " + e.getMessage());
        }

        return barangayName;
    }
    
    
    
    public static void insertMeterReading(Connection conn, String accountNo, long readingThisMonth, java.sql.Date readingThisMonthDate, long readingLastMonth, 
    		java.sql.Date readingLastMonthDate, String readby, double amountPayable, long readingNumber) {
        String query = "INSERT INTO meter_readings (account_no, reading_this_month, reading_this_month_date, "
        		+ "reading_last_month, reading_last_month_date, read_by, amount_payable, billing_entry_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNo);
            stmt.setLong(2, readingThisMonth);
            stmt.setDate(3, readingThisMonthDate);
            stmt.setLong(4, readingLastMonth);
            stmt.setDate(5, readingLastMonthDate);
            stmt.setString(6, readby);
            stmt.setDouble(7, amountPayable);
            stmt.setLong(8, readingNumber);

            stmt.executeUpdate();
            System.out.println("Meter reading inserted successfully.");
        } catch (SQLException e) {
            System.out.println("Error inserting meter reading: " + e.getMessage());
        }
    }
    
    public static int getHighestBillingEntryCount(Connection conn) throws SQLException {
        String query = "SELECT MAX(billing_entry_count) AS max_billing_entry_count " +
                       "FROM meter_readings";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("max_billing_entry_count");
            } else {
                return 1; // Return 0 if no data found
            }
        }
    }

    public static void setPermissionReading(Connection conn, boolean permission) throws SQLException {
        String query = "UPDATE permission_reading SET permission = ? WHERE id = 1";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, permission);
            stmt.executeUpdate();
        }
    }
    
    public static boolean getPermissionReading(Connection conn) throws SQLException {
        String query = "SELECT permission FROM permission_reading WHERE id = 1";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBoolean("permission");
            } else {
                throw new SQLException("No record found with id = 1");
            }
        }
    }




    public static ResultSet getLatestMeterReadingByAccountNumber(Connection conn, int accountNumber) throws SQLException {
        // Query to fetch the latest reading_this_month and its corresponding reading_this_month_date for the given account_no
        String query = "SELECT reading_this_month, reading_this_month_date " +
                       "FROM meter_readings " +
                       "WHERE account_no = ? " +
                       "ORDER BY reading_this_month_date DESC " +  // Order by date in descending order
                       "LIMIT 1";  // Limit the result to the latest (most recent) entry
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, accountNumber);  // Use setInt() to bind the account number
        return stmt.executeQuery();
    }

    
    public static ResultSet searchByAccountNumber(Connection connection, String accountNumber) throws SQLException {
        // SQL query to fetch details from meter_connection table
        String query = "SELECT mc.accountno_field, mc.connection_field, mc.meter_no, mc.first_name, mc.last_name, mc.company, " +
                       "mc.barangay_field, mc.barangay_code, mc.connectiontype_cb " +
                       "FROM meter_connection mc " +
                       "WHERE mc.accountno_field = ?";

        // Prepare the statement and set the account number as a parameter
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, accountNumber);  // Set the account number as a parameter

        // Execute the query and return the result set
        return pstmt.executeQuery();
    }




    // Method to search data by full name
    public static ResultSet searchByFullName(Connection connection, String fullName) throws SQLException {
        String searchSQL = 
            "SELECT c.connection_no, c.connection_type, c.brgy, c.account_no, " +
            "c.first_name, c.last_name, c.middle_name, c.address, c.company_no, c.company_ad, " +
            "c.company_contact, c.company_email, c.company_website, c.meter_no, c.meter_brand, " +
            "c.meter_fee, c.date_applied, c.installed, c.date_installed, c.installer, c.mode_of_payment " +
            "FROM names n JOIN connection_tb c ON n.connection_id = c.id WHERE n.full_name = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement(searchSQL);
            pstmt.setString(1, fullName);
            rs = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Failed to search data: " + e.getMessage());
        }
        return rs;
    }

    // Method to check if a full name exists
    public static boolean doesFullNameExist(Connection connection, String fullName) throws SQLException {
        String querySQL = "SELECT COUNT(*) FROM names WHERE full_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, fullName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to check full name existence: " + e.getMessage());
        }
        return false;
    }

    // Method to insert data into connection_tb and return the generated ID
    public static long insertData(Connection connection, String connectionNo, String connectionType, String brgy,
                                  String accountNo, String firstName, String lastName, String middleName,
                                  String address, String companyNo, String companyAddress, String companyContact,
                                  String companyEmail, String companyWebsite, String meterNo, String meterBrand,
                                  String meterFee, String dateApplied, boolean installed, String dateInstalled,
                                  String installer, String modeOfPayment) {
        String insertSQL = "INSERT INTO connection_tb (connection_no, connection_type, brgy, account_no, first_name, last_name, middle_name, address, company_no, company_ad, company_contact, company_email, company_website, meter_no, meter_brand, meter_fee, date_applied, installed, date_installed, installer, mode_of_payment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        long id = -1;

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, connectionNo);
            pstmt.setString(2, connectionType);
            pstmt.setString(3, brgy);
            pstmt.setString(4, accountNo);
            pstmt.setString(5, firstName);
            pstmt.setString(6, lastName);
            pstmt.setString(7, middleName);
            pstmt.setString(8, address);
            pstmt.setString(9, companyNo);
            pstmt.setString(10, companyAddress);
            pstmt.setString(11, companyContact);
            pstmt.setString(12, companyEmail);
            pstmt.setString(13, companyWebsite);
            pstmt.setString(14, meterNo);
            pstmt.setString(15, meterBrand);
            pstmt.setString(16, meterFee);
            pstmt.setString(17, dateApplied);
            pstmt.setBoolean(18, installed);
            pstmt.setString(19, dateInstalled);
            pstmt.setString(20, installer);
            pstmt.setString(21, modeOfPayment);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully.");
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        id = generatedKeys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to insert data: " + e.getMessage());
        }
        return id;
    }
  

    // Method to update data in connection_tb
    public static void updateData(Connection connection, String connectionNo, String connectionType, String brgy,
                                  String accountNo, String firstName, String lastName, String middleName,
                                  String address, String companyNo, String companyAddress, String companyContact,
                                  String companyEmail, String companyWebsite, String meterNo, String meterBrand,
                                  String meterFee, String dateApplied, boolean installed, String dateInstalled,
                                  String installer, String modeOfPayment) throws SQLException {
        String updateSQL = "UPDATE connection_tb SET connection_no = ?, connection_type = ?, brgy = ?, account_no = ?, address = ?, company_no = ?, company_ad = ?, company_contact = ?, company_email = ?, company_website = ?, meter_no = ?, meter_brand = ?, meter_fee = ?, date_applied = ?, installed = ?, date_installed = ?, installer = ?, mode_of_payment = ? WHERE first_name = ? AND middle_name = ? AND last_name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, connectionNo);
            pstmt.setString(2, connectionType);
            pstmt.setString(3, brgy);
            pstmt.setString(4, accountNo);
            pstmt.setString(5, address);
            pstmt.setString(6, companyNo);
            pstmt.setString(7, companyAddress);
            pstmt.setString(8, companyContact);
            pstmt.setString(9, companyEmail);
            pstmt.setString(10, companyWebsite);
            pstmt.setString(11, meterNo);
            pstmt.setString(12, meterBrand);
            pstmt.setString(13, meterFee);
            pstmt.setString(14, dateApplied);
            pstmt.setBoolean(15, installed);
            pstmt.setString(16, dateInstalled);
            pstmt.setString(17, installer);
            pstmt.setString(18, modeOfPayment);
            pstmt.setString(19, firstName);
            pstmt.setString(20, middleName);
            pstmt.setString(21, lastName);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data updated successfully.");
            } else {
                System.out.println("No matching record found to update.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to update data: " + e.getMessage());
        }
    }
    
  //a
    public static long meter_Connection_insert(Connection connection, int connectionfield, String connectionType,
            String barangay, String accountNo, String firstName, String lastName,
            String middleName, int houseNo, String company, String barangayCode,
            long contactNumber, int meterNo, String meterBrand, int meterFee,
            java.sql.Date dateApplied, java.sql.Date dateInstalled, String charges, int or) {
        // SQL insert query for meter_connection
        String insertSQL = "INSERT INTO meter_connection (connection_field, connectionType_Cb, barangay_field, accountno_field, " +
                "first_name, last_name, middle_name, house_no, company, barangay_code, contact_number, meter_no, " +
                "meter_brand, meter_fee, date_applied, date_installed, charges, official_reciept) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        long generatedId = -1;

        // Using PreparedStatement to insert data into the meter_connection table
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            // Set the values from method parameters
            pstmt.setInt(1, connectionfield);
            pstmt.setString(2, connectionType);
            pstmt.setString(3, barangay);
            pstmt.setString(4, accountNo);
            pstmt.setString(5, firstName);
            pstmt.setString(6, lastName);
            pstmt.setString(7, middleName);
            pstmt.setInt(8, houseNo);
            pstmt.setString(9, company);
            pstmt.setString(10, barangayCode);
            pstmt.setLong(11, contactNumber);
            pstmt.setInt(12, meterNo);
            pstmt.setString(13, meterBrand);
            pstmt.setInt(14, meterFee);
            pstmt.setDate(15, dateApplied);
            pstmt.setDate(16, dateInstalled);
            pstmt.setString(17, charges);
            pstmt.setInt(18, or);

            // Execute the insert and get generated keys (ID)
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully into meter_connection.");
                // Retrieve the generated ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getLong(1); // Store the generated ID
                    }
                }

                // After successfully inserting into meter_connection, insert into installation_breakdown
                if (generatedId > 0) {
                    insertInstallationBreakdown(connection, generatedId);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to insert data into meter_connection: " + e.getMessage());
        }

        return generatedId;
    }
    
 // Method to insert data into the installation_breakdown table
    private static void insertInstallationBreakdown(Connection connection, long meterConnectionId) {
        // SQL insert query for installation_breakdown table
        String insertSQL = "INSERT INTO installation_breakdown (id) VALUES (?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            // Set the foreign key (meter_connection_id) and installation details
            pstmt.setLong(1, meterConnectionId);  // Foreign key from meter_connection

            // Execute the insert
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully into installation_breakdown.");
            } else {
                System.out.println("Failed to insert data into installation_breakdown.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to insert data into installation_breakdown: " + e.getMessage());
        }
    }


    // Method to insert a full name into the names table
    public static void insertWholeName(Connection connection, String fullName, long connectionId) throws SQLException {
        String insertSQL = "INSERT INTO names (full_name, connection_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, fullName);
            pstmt.setLong(2, connectionId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to insert data: " + e.getMessage());
        }
    }

    // Method to update connection_id in the monthly_bill table
    public static void updateMonthlyBillConnectionId(Connection connection, long connectionId) throws SQLException {
        String insertSQL = "INSERT INTO monthly_bill (connection_id) VALUES (?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setLong(1, connectionId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted successfully Monthly.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to insert data: " + e.getMessage());
        }
    }
    
    
    public static ResultSet getAllConnections(Connection connection) {
        String query = "SELECT first_name, middle_name, last_name, company_contact, brgy, connection_no, meter_no FROM connection_tb";
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Failed to fetch data: " + e.getMessage());
        }
        return rs;
    }
    
    public static ResultSet getBarangays(Connection connection) {
        String query = "SELECT barangay_name FROM barangay";
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Failed to fetch barangays: " + e.getMessage());
        }
        return rs;
    }
    
    public static void meter_connection_history_insert(Connection conn, String accountNo, int connectionNo, 
            String connectionFee, String meterFee, String inspectionFee, String othersMaterials, 
            String saddleClamp, String miscellaneous) throws SQLException {
        
        String query = "INSERT INTO meter_connection_history (account_no, connection_no, connection_fee, meter_fee, inspection_fee, others_materials, saddle_clamp, miscellaneous) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accountNo);
            pstmt.setInt(2, connectionNo);
            
            // Convert string values to integers
            pstmt.setInt(3, Integer.parseInt(connectionFee));
            pstmt.setInt(4, Integer.parseInt(meterFee));
            pstmt.setInt(5, Integer.parseInt(inspectionFee));
            pstmt.setInt(6, Integer.parseInt(othersMaterials));
            pstmt.setInt(7, Integer.parseInt(saddleClamp));
            pstmt.setInt(8, Integer.parseInt(miscellaneous));
            
            pstmt.executeUpdate();
        }
    }
    
    public static ResultSet getAllBarangays(Connection connection) {
        String query = "SELECT brgy_code, barangay_name FROM barangay";
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Failed to fetch barangays: " + e.getMessage());
        }
        return rs;
    }



}

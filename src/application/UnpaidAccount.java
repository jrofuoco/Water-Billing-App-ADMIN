package application;

public class UnpaidAccount {
    private String accountNo;
    private double outstandingBalance;
    private String contactNumber;
    private String company;
    private String fullName;
    private int unpaidTotalMonth;
    private String unpaidStatus;
    private String barangay;
    
    // Constructor
    public UnpaidAccount(String accountNo, double outstandingBalance) {
        this.accountNo = accountNo;
        this.outstandingBalance = outstandingBalance;
    }

    // Getters and Setters
    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public double getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public int getUnpaidTotalMonth() {
        return unpaidTotalMonth;
    }

    public void setUnpaidTotalMonth(int unpaidTotalMonth) {
        this.unpaidTotalMonth = unpaidTotalMonth;
    }
    
    public String getUnpaidStatus() {
        return unpaidStatus;
    }

    public void setUnpaidStatus(String unpaidStatus) {
        this.unpaidStatus = unpaidStatus;
    }
    
    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    // Method to update the balance by adding an additional amount
    public void addOutstandingBalance(double additionalAmount) {
        this.outstandingBalance += additionalAmount;
    }
}
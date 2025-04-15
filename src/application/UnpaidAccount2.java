package application;

public class UnpaidAccount2 {
    private String accountNo;
    private String contactNumber;
    private String company;
    private String fullName;

    public UnpaidAccount2(String accountNo, String contactNumber, String company, String fullName) {
        this.accountNo = accountNo;
        this.contactNumber = contactNumber;
        this.company = company;
        this.fullName = fullName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getCompany() {
        return company;
    }

    public String getFullName() {
        return fullName;
    }
}
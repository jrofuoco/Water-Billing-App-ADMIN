package application;

public class Billing_Summary_Model {
    private String accountNo;
    private String name;
    private String company;
    private String barangay;
    private String billPeriod;
    private String billingNumber;
    private Double payment;
    private String paymentDate;
    private final String status;

    public Billing_Summary_Model(String accountNo, String name, String company, String barangay, String billPeriod, String billingNumber, Double payment, String paymentDate) {
        this.accountNo = accountNo;
        this.name = name;
        this.company = company;
        this.barangay = barangay;
        this.billPeriod = billPeriod;
        this.billingNumber = billingNumber;
        this.payment = payment;
        this.paymentDate = paymentDate;
        this.status = "Completed";
    }
    
    public String getStatus() {
        return status;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getBillPeriod() {
        return billPeriod;
    }

    public void setBillPeriod(String billPeriod) {
        this.billPeriod = billPeriod;
    }

    public String getBillingNumber() {
        return billingNumber;
    }

    public void setBillingNumber(String billingNumber) {
        this.billingNumber = billingNumber;
    }

    public Double getPayment() {
        return payment;
    }

    public void setPayment(Double payment) {
        this.payment = payment;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
}
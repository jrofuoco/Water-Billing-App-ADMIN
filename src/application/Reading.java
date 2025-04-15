package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.BooleanProperty;

public class Reading {
    private final SimpleStringProperty accountNumber;
    private final SimpleStringProperty name;
    private final SimpleStringProperty meterNo;
    private final SimpleIntegerProperty previousReading;
    private final SimpleIntegerProperty currentReading;
    private final SimpleIntegerProperty consumption;
    private final SimpleDoubleProperty amount;
    private final SimpleStringProperty status;  // This is where status is stored
// Add this property
    private final SimpleBooleanProperty billingReading;
    private final BooleanProperty checked;
    private IntegerProperty billingEntryCount;
    
    private int accountNoReading;
    

    public Reading(String accountNumber2, String name, String meterNo, int previousReading, 
            int currentReading, int consumption, double amount, String status, boolean billingReading, int billingEntryCount) {
	 this.accountNumber = new SimpleStringProperty(accountNumber2);
	 this.name = new SimpleStringProperty(name);
	 this.meterNo = new SimpleStringProperty(meterNo);
	 this.previousReading = new SimpleIntegerProperty(previousReading);
	 this.currentReading = new SimpleIntegerProperty(currentReading);
	 this.consumption = new SimpleIntegerProperty(consumption);
	 this.amount = new SimpleDoubleProperty(amount);
	 this.status = new SimpleStringProperty(status);

	 this.billingReading = new SimpleBooleanProperty(billingReading);
	 this.checked = new SimpleBooleanProperty(false);
	 this.billingEntryCount = new SimpleIntegerProperty(billingEntryCount);
}


    public SimpleStringProperty accountNumberProperty() {
        return accountNumber;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty meterNoProperty() {
        return meterNo;
    }

    public SimpleIntegerProperty previousReadingProperty() {
        return previousReading;
    }

    public SimpleIntegerProperty currentReadingProperty() {
        return currentReading;
    }

    public SimpleIntegerProperty consumptionProperty() {
        return consumption;
    }

    public SimpleDoubleProperty amountProperty() {
        return amount;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public SimpleBooleanProperty billingReadingProperty() {
        return billingReading;
    }

    public boolean isBillingReading() {
        return billingReading.get();
    }

    public void setBillingReading(boolean billingReading) {
        this.billingReading.set(billingReading);
    }
    
    public int getAccountNoReading() {
        return accountNoReading;
    }

    public void setAccountNoReading(int accountNoReading) {
        this.accountNoReading = accountNoReading;
    }
    
    public BooleanProperty checkedProperty() {
        return checked;
    }

    public boolean isChecked() {
        return checked.get();
    }

    public void setChecked(boolean checked) {
        this.checked.set(checked);
    }

    // New setStatus method
    public void setStatus(String status) {
        this.status.set(status);  // This updates the status value
    }
    
    public IntegerProperty billingEntryCountProperty() {
        return billingEntryCount;
    }

    public int getBillingEntryCount() {
        return billingEntryCount.get();
    }

    public void setBillingEntryCount(int billingEntryCount) {
        this.billingEntryCount.set(billingEntryCount);
    }
    
    

    // Getter for status
    public String getStatus() {
        return status.get();
    }
}
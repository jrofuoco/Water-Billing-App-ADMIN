package application;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class BillingRecord {
    private final SimpleIntegerProperty billingEntryCount;
    private final SimpleStringProperty billingPeriod;
    private final SimpleDoubleProperty amountPayable;

    public BillingRecord(int billingEntryCount, String billingPeriod, double amountPayable) {
        this.billingEntryCount = new SimpleIntegerProperty(billingEntryCount);
        this.billingPeriod = new SimpleStringProperty(billingPeriod);
        this.amountPayable = new SimpleDoubleProperty(amountPayable);
    }

    public int getBillingEntryCount() {
        return billingEntryCount.get();
    }

    public SimpleIntegerProperty billingEntryCountProperty() {
        return billingEntryCount;
    }

    public String getBillingPeriod() {
        return billingPeriod.get();
    }

    public SimpleStringProperty billingPeriodProperty() {
        return billingPeriod;
    }

    public double getAmountPayable() {
        return amountPayable.get();
    }

    public SimpleDoubleProperty amountPayableProperty() {
        return amountPayable;
    }
}

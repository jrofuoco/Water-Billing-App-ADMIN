package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Barangay {
    private final StringProperty barangayName;
    private final StringProperty brgyCode;

    public Barangay(String barangayName, String brgyCode) {
        this.barangayName = new SimpleStringProperty(barangayName);
        this.brgyCode = new SimpleStringProperty(brgyCode);
    }

    public String getBarangayName() {
        return barangayName.get();
    }

    public void setBarangayName(String barangayName) {
        this.barangayName.set(barangayName);
    }

    public StringProperty barangayNameProperty() {
        return barangayName;
    }

    public String getBrgyCode() {
        return brgyCode.get();
    }

    public void setBrgyCode(String brgyCode) {
        this.brgyCode.set(brgyCode);
    }

    public StringProperty brgyCodeProperty() {
        return brgyCode;
    }
}


package com.mr_rabbit.polishedcityfoodcorner.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;


public class ShiftRecord {

    private final SimpleIntegerProperty id;
    private final SimpleStringProperty employeeCode;
    private final SimpleStringProperty name;
    private final SimpleStringProperty role;
    private final SimpleStringProperty checkIn;    // formatted HH:mm for display
    private final SimpleStringProperty checkOut;   // formatted HH:mm for display, "-" if not checked out yet
    private final SimpleStringProperty workingHour; // formatted HH:mm difference
    private final SimpleStringProperty workDate;   // yyyy-MM-dd, used for filtering

    public ShiftRecord(int id, String employeeCode, String name, String role, String checkIn,
                        String checkOut, String workingHour, String workDate) {
        this.id = new SimpleIntegerProperty(id);
        this.employeeCode = new SimpleStringProperty(employeeCode);
        this.name = new SimpleStringProperty(name);
        this.role = new SimpleStringProperty(role);
        this.checkIn = new SimpleStringProperty(checkIn);
        this.checkOut = new SimpleStringProperty(checkOut);
        this.workingHour = new SimpleStringProperty(workingHour);
        this.workDate = new SimpleStringProperty(workDate);
    }

    public int getId() { return id.get(); }
    public SimpleIntegerProperty idProperty() { return id; }

    public String getEmployeeCode() { return employeeCode.get(); }
    public SimpleStringProperty employeeCodeProperty() { return employeeCode; }

    public String getName() { return name.get(); }
    public SimpleStringProperty nameProperty() { return name; }

    public String getRole() { return role.get(); }
    public SimpleStringProperty roleProperty() { return role; }

    public String getCheckIn() { return checkIn.get(); }
    public SimpleStringProperty checkInProperty() { return checkIn; }

    public String getCheckOut() { return checkOut.get(); }
    public SimpleStringProperty checkOutProperty() { return checkOut; }

    public String getWorkingHour() { return workingHour.get(); }
    public SimpleStringProperty workingHourProperty() { return workingHour; }

    public String getWorkDate() { return workDate.get(); }
    public SimpleStringProperty workDateProperty() { return workDate; }
}

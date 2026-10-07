package com.mr_rabbit.polishedcityfoodcorner.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Employee {

    private final SimpleIntegerProperty id;
    private final SimpleStringProperty employeeCode;   // e.g. CFCE-001, null until admin confirms
    private final SimpleStringProperty name;
    private final SimpleStringProperty email;
    private final SimpleStringProperty mobile;
    private final SimpleStringProperty passwordHash;
    private final SimpleStringProperty role;            // Salesman / Inventory Manager
    private final SimpleStringProperty status;           // Pending / Approved / Blocked
    private final SimpleStringProperty joinedOn;          // formatted date string for display
    private final SimpleBooleanProperty idLocked;         // true once employeeCode has been set once

    public Employee(int id, String employeeCode, String name, String email, String mobile,
                     String passwordHash, String role, String status, String joinedOn, boolean idLocked) {
        this.id = new SimpleIntegerProperty(id);
        this.employeeCode = new SimpleStringProperty(employeeCode);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.mobile = new SimpleStringProperty(mobile);
        this.passwordHash = new SimpleStringProperty(passwordHash);
        this.role = new SimpleStringProperty(role);
        this.status = new SimpleStringProperty(status);
        this.joinedOn = new SimpleStringProperty(joinedOn);
        this.idLocked = new SimpleBooleanProperty(idLocked);
    }

    public int getId() { return id.get(); }
    public SimpleIntegerProperty idProperty() { return id; }

    public String getEmployeeCode() { return employeeCode.get(); }
    public void setEmployeeCode(String value) { employeeCode.set(value); }
    public SimpleStringProperty employeeCodeProperty() { return employeeCode; }

    public String getName() { return name.get(); }
    public SimpleStringProperty nameProperty() { return name; }

    public String getEmail() { return email.get(); }
    public SimpleStringProperty emailProperty() { return email; }

    public String getMobile() { return mobile.get(); }
    public SimpleStringProperty mobileProperty() { return mobile; }

    public String getPasswordHash() { return passwordHash.get(); }
    public SimpleStringProperty passwordHashProperty() { return passwordHash; }

    public String getRole() { return role.get(); }
    public SimpleStringProperty roleProperty() { return role; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public SimpleStringProperty statusProperty() { return status; }

    public String getJoinedOn() { return joinedOn.get(); }
    public SimpleStringProperty joinedOnProperty() { return joinedOn; }

    public boolean isIdLocked() { return idLocked.get(); }
    public void setIdLocked(boolean value) { idLocked.set(value); }
    public SimpleBooleanProperty idLockedProperty() { return idLocked; }
}

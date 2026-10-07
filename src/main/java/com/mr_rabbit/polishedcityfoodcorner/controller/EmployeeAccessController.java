package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.dao.EmployeeDAO;
import com.mr_rabbit.polishedcityfoodcorner.model.Employee;
import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class EmployeeAccessController implements HostAware {

    @FXML private Label totalemployeelable;
    @FXML private Label totalpendingemployeelable;
    @FXML private Label totalapprovedemployeelable;
    @FXML private Label totalblockedepmloyeelable;
    @FXML private Label totalsalesmannumberlable;
    @FXML private Label totalinventorymanagerlable;

    @FXML private TableView<Employee> employeetable;
    @FXML private TableColumn<Employee, String> employeetableid;
    @FXML private TableColumn<Employee, String> employeetablename;
    @FXML private TableColumn<Employee, String> employeetableemail;
    @FXML private TableColumn<Employee, String> employeetablemobile;
    @FXML private TableColumn<Employee, String> employeetablerole;
    @FXML private TableColumn<Employee, String> employeetablestatus;
    @FXML private TableColumn<Employee, String> employeetablejoinedon;

    @FXML private TextField salesmanidfield;
    @FXML private ComboBox<String> employeesortcombox;
    @FXML private TextField emplyeesearchfield;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ObservableList<Employee> masterList = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredEmployees;

    private String currentSearchQuery = "";
    private String currentCategory = null;

    private AdminContentHost host;

    @Override
    public void setHost(AdminContentHost host) {
        this.host = host;
    }

    @FXML
    private void initialize() {
        employeetableid.setCellValueFactory(data -> {
            String code = data.getValue().getEmployeeCode();
            return new javafx.beans.property.SimpleStringProperty(code == null || code.isBlank() ? "-" : code);
        });
        employeetablename.setCellValueFactory(data -> data.getValue().nameProperty());
        employeetableemail.setCellValueFactory(data -> data.getValue().emailProperty());
        employeetablemobile.setCellValueFactory(data -> data.getValue().mobileProperty());
        employeetablerole.setCellValueFactory(data -> data.getValue().roleProperty());
        employeetablestatus.setCellValueFactory(data -> data.getValue().statusProperty());
        employeetablejoinedon.setCellValueFactory(data -> data.getValue().joinedOnProperty());

        filteredEmployees = new FilteredList<>(masterList, e -> true);
        employeetable.setItems(filteredEmployees);

        employeesortcombox.setItems(FXCollections.observableArrayList(
                "Salesman", "Inventory Manager", "Approved", "Blocked", "Pending"));

        // Force prompt text to appear when value is null
        employeesortcombox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(employeesortcombox.getPromptText());
                } else {
                    setText(item);
                }
            }
        });

        emplyeesearchfield.textProperty().addListener((obs, oldVal, newVal) -> {
            currentSearchQuery = newVal == null ? "" : newVal.trim().toLowerCase();
            applyFilters();
        });

        employeesortcombox.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentCategory = newVal;
            applyFilters();
        });

        refreshData();
    }

    private void applyFilters() {
        filteredEmployees.setPredicate(emp -> {
            boolean matchesSearch = currentSearchQuery.isEmpty()
                    || (emp.getEmployeeCode() != null && emp.getEmployeeCode().toLowerCase().contains(currentSearchQuery))
                    || emp.getName().toLowerCase().contains(currentSearchQuery);
            boolean matchesCategory = currentCategory == null
                    || currentCategory.equals(emp.getRole())
                    || currentCategory.equals(emp.getStatus());
            return matchesSearch && matchesCategory;
        });
    }

    private void refreshData() {
        masterList.setAll(employeeDAO.getAllEmployees());
        updateCounts();
    }

    private void updateCounts() {
        int approved = employeeDAO.countByStatus("Approved");
        int blocked = employeeDAO.countByStatus("Blocked");
        totalpendingemployeelable.setText(String.valueOf(employeeDAO.countByStatus("Pending")));
        totalapprovedemployeelable.setText(String.valueOf(approved));
        totalblockedepmloyeelable.setText(String.valueOf(blocked));
        totalemployeelable.setText(String.valueOf(approved + blocked));
        totalsalesmannumberlable.setText(String.valueOf(employeeDAO.countByRoleAndStatus("Salesman", "Approved")));
        totalinventorymanagerlable.setText(String.valueOf(employeeDAO.countByRoleAndStatus("Inventory Manager", "Approved")));
    }

    private Employee getSelectedEmployeeOrWarn() {
        Employee selected = employeetable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.warning("No Employee Selected", "Please select an employee from the table first.");
        }
        return selected;
    }

    @FXML
    private void confirmbtn() {
        Employee selected = getSelectedEmployeeOrWarn();
        if (selected == null) return;

        if (!"Pending".equals(selected.getStatus())) {
            AlertHelper.warning("Not Allowed", "Only employees with status Pending can be confirmed.");
            return;
        }
        if (selected.isIdLocked()) {
            AlertHelper.warning("Not Allowed", "This employee's ID has already been set once and cannot be changed.");
            return;
        }

        String code = salesmanidfield.getText() == null ? "" : salesmanidfield.getText().trim();
        if (code.isEmpty()) {
            AlertHelper.warning("Missing ID", "Please enter an employee id, e.g. CFCE-001.");
            return;
        }
        if (employeeDAO.employeeCodeExists(code)) {
            AlertHelper.warning("ID Already Used", "That employee id is already assigned to someone else.");
            return;
        }

        boolean success = employeeDAO.confirmEmployee(selected.getId(), code);
        if (success) {
            salesmanidfield.clear();
            refreshData();
        } else {
            AlertHelper.error("Failed", "Could not confirm this employee. Please try again.");
        }
    }

    @FXML
    private void blockbtn() {
        Employee selected = getSelectedEmployeeOrWarn();
        if (selected == null) return;

        if (!"Approved".equals(selected.getStatus())) {
            AlertHelper.warning("Not Allowed", "Only employees with status Approved can be blocked.");
            return;
        }
        employeeDAO.updateStatus(selected.getId(), "Blocked");
        refreshData();
    }

    @FXML
    private void rejoinbtn() {
        Employee selected = getSelectedEmployeeOrWarn();
        if (selected == null) return;

        if (!"Blocked".equals(selected.getStatus())) {
            AlertHelper.warning("Not Allowed", "Only employees with status Blocked can re-join.");
            return;
        }
        employeeDAO.updateStatus(selected.getId(), "Approved");
        refreshData();
    }

    @FXML
    private void emopledeletebtn() {
        Employee selected = getSelectedEmployeeOrWarn();
        if (selected == null) return;

        if (!"Blocked".equals(selected.getStatus())) {
            AlertHelper.warning("Not Allowed", "Only employees with status Blocked can be deleted.");
            return;
        }
        if (!AlertHelper.confirm("Confirm Delete", "Permanently delete " + selected.getName() + "?")) {
            return;
        }
        employeeDAO.deleteEmployee(selected.getId());
        refreshData();
    }

    @FXML
    private void resetbtn() {
        emplyeesearchfield.clear();
        employeesortcombox.getSelectionModel().clearSelection();
        employeesortcombox.setValue(null);
        applyFilters();
    }

    @FXML
    private void shiftrecordbtn() {
        if (host != null) {
            host.loadContent("ShiftRecord.fxml");
        }
    }
}


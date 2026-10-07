package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.dao.ShiftDAO;
import com.mr_rabbit.polishedcityfoodcorner.model.ShiftRecord;
import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import com.mr_rabbit.polishedcityfoodcorner.util.SearchFilterUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ShiftRecordController implements HostAware {

    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yy");

    @FXML private TableView<ShiftRecord> shiftrecordtable;
    @FXML private TableColumn<ShiftRecord, String> shiftrecordtableid;
    @FXML private TableColumn<ShiftRecord, String> shiftrecordtablename;
    @FXML private TableColumn<ShiftRecord, String> shiftrecordtablerole;
    @FXML private TableColumn<ShiftRecord, String> shiftrecordtablecheckin;
    @FXML private TableColumn<ShiftRecord, String> shiftrecordtablecheckout;
    @FXML private TableColumn<ShiftRecord, String> shiftrecordtableworkinghour;

    @FXML private TextField shiftrecordsearchfield;
    @FXML private Button shiftrecordsearchbtn;

    @FXML private ComboBox<String> reporttypecombox;
    @FXML private TextField reportstartlable;
    @FXML private TextField reportendlable;
    @FXML private ComboBox<String> reportrolecombox;

    private final ShiftDAO shiftDAO = new ShiftDAO();
    private final ObservableList<ShiftRecord> masterList = FXCollections.observableArrayList();
    private FilteredList<ShiftRecord> filteredRecords;

    private AdminContentHost host;

    @Override
    public void setHost(AdminContentHost host) {
        this.host = host;
    }

    @FXML
    private void initialize() {
        shiftrecordtableid.setCellValueFactory(data -> data.getValue().employeeCodeProperty());
        shiftrecordtablename.setCellValueFactory(data -> data.getValue().nameProperty());
        shiftrecordtablerole.setCellValueFactory(data -> data.getValue().roleProperty());
        shiftrecordtablecheckin.setCellValueFactory(data -> data.getValue().checkInProperty());
        shiftrecordtablecheckout.setCellValueFactory(data -> data.getValue().checkOutProperty());
        shiftrecordtableworkinghour.setCellValueFactory(data -> data.getValue().workingHourProperty());

        reporttypecombox.setItems(FXCollections.observableArrayList("Weekly Report", "Monthly Report"));
        reportrolecombox.setItems(FXCollections.observableArrayList("Inventory Manager", "Salesman"));

        // Force prompt text to appear when value is null for both combo boxes
        reporttypecombox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? reporttypecombox.getPromptText() : item);
            }
        });
        reportrolecombox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? reportrolecombox.getPromptText() : item);
            }
        });

        filteredRecords = new FilteredList<>(masterList, r -> true);
        shiftrecordtable.setItems(filteredRecords);

        SearchFilterUtil.attachLiveFilter(shiftrecordsearchfield, shiftrecordsearchbtn, filteredRecords,
                (record, query) -> record.getEmployeeCode().toLowerCase().contains(query)
                        || record.getName().toLowerCase().contains(query));
        shiftrecordsearchbtn.setOnAction(event -> shiftrecordsearchfield.clear());

        // Each of these can drive the report on its own, in addition to the Apply button.
        reporttypecombox.valueProperty().addListener((obs, oldVal, newVal) -> runReport());
        reportrolecombox.valueProperty().addListener((obs, oldVal, newVal) -> runReport());

        masterList.setAll(shiftDAO.getAllShiftRecords());
    }

    private void runReport() {
        LocalDate startDate = null;
        LocalDate endDate = null;

        String manualStart = textOf(reportstartlable);
        String manualEnd = textOf(reportendlable);

        if (!manualStart.isEmpty() && !manualEnd.isEmpty()) {
            try {
                startDate = LocalDate.parse(manualStart, INPUT_DATE_FORMAT);
                endDate = LocalDate.parse(manualEnd, INPUT_DATE_FORMAT);
            } catch (DateTimeParseException e) {
                AlertHelper.warning("Invalid Date", "Please enter dates as DD/MM/YY.");
                return;
            }
        } else if ("Weekly Report".equals(reporttypecombox.getValue())) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(6);
        } else if ("Monthly Report".equals(reporttypecombox.getValue())) {
            endDate = LocalDate.now();
            startDate = endDate.withDayOfMonth(1);
        }

        String role = reportrolecombox.getValue();
        masterList.setAll(shiftDAO.getFiltered(startDate, endDate, role));
    }

    @FXML
    private void applybtn() {
        runReport();
    }

    @FXML
    private void resetbtn() {
        shiftrecordsearchfield.clear();
        reporttypecombox.getSelectionModel().clearSelection();
        reporttypecombox.setValue(null);
        reportstartlable.clear();
        reportendlable.clear();
        reportrolecombox.getSelectionModel().clearSelection();
        reportrolecombox.setValue(null);
        masterList.setAll(shiftDAO.getAllShiftRecords());
    }

    @FXML
    private void backbtn() {
        if (host != null) {
            host.loadContent("EmployeeAcces.fxml");
        }
    }

    private String textOf(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}


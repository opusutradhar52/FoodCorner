package com.mr_rabbit.polishedcityfoodcorner.controller;

import com.mr_rabbit.polishedcityfoodcorner.dao.CashFlowDAO;
import com.mr_rabbit.polishedcityfoodcorner.dao.EmployeeDAO;
import com.mr_rabbit.polishedcityfoodcorner.dao.SaleDAO;
import com.mr_rabbit.polishedcityfoodcorner.model.Employee;
import com.mr_rabbit.polishedcityfoodcorner.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Map;

public class CashFlowController implements HostAware {

    @FXML private Label totalincomelable;      // Today's Income
    @FXML private Label monthlyincomelable;    // Monthly Income
    @FXML private Label monthlycostlable;      // Monthly Cost
    @FXML private Label totalcostlable;        // Total Reserved (fx:id name kept as-is from the FXML)

    @FXML private LineChart<String, Number> monthlyincomelinechat;
    @FXML private LineChart<String, Number> monthlycostlinechart;

    @FXML private TextField withdralamount;
    @FXML private ComboBox<String> withdralreasoncombox;

    @FXML private TextField bestsalesmansalaryamountfield;
    @FXML private TextField incrimentpercentagefiled;
    @FXML private TextField inventorysalaryammountfield;
    @FXML private Label salesmansalaryamountlable;
    @FXML private Label numofregularsalesmanlable;
    @FXML private Label numofinventorymanagerlable;

    private final SaleDAO saleDAO = new SaleDAO();
    private final CashFlowDAO cashFlowDAO = new CashFlowDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    // Not currently used for navigation (CashFlow has no "go to another admin screen"
    // button of its own) but implemented for consistency with the other dashboard panels.
    private AdminContentHost host;

    @Override
    public void setHost(AdminContentHost host) {
        this.host = host;
    }

    @FXML
    private void initialize() {
        withdralreasoncombox.setItems(FXCollections.observableArrayList(
                CashFlowDAO.CATEGORY_PERSONAL, CashFlowDAO.CATEGORY_INVENTORY));

        bestsalesmansalaryamountfield.textProperty().addListener((obs, oldVal, newVal) ->
                salesmansalaryamountlable.setText(newVal == null || newVal.isBlank() ? "same as previous" : newVal));

        if (monthlyincomelinechat != null && monthlyincomelinechat.getXAxis() instanceof javafx.scene.chart.CategoryAxis) {
            javafx.scene.chart.CategoryAxis xAxis = (javafx.scene.chart.CategoryAxis) monthlyincomelinechat.getXAxis();
            xAxis.setTickLabelRotation(0);
            xAxis.setTickLabelGap(5);
        }
        if (monthlycostlinechart != null && monthlycostlinechart.getXAxis() instanceof javafx.scene.chart.CategoryAxis) {
            javafx.scene.chart.CategoryAxis xAxis = (javafx.scene.chart.CategoryAxis) monthlycostlinechart.getXAxis();
            xAxis.setTickLabelRotation(0);
            xAxis.setTickLabelGap(5);
        }

        loadSavedSalaryConfig();
        refreshDashboard();
    }

    private void loadSavedSalaryConfig() {
        double[] config = cashFlowDAO.loadSalaryConfig();
        if (config[0] > 0) bestsalesmansalaryamountfield.setText(trimZero(config[0]));
        if (config[1] > 0) incrimentpercentagefiled.setText(trimZero(config[1]));
        if (config[2] > 0) inventorysalaryammountfield.setText(trimZero(config[2]));
    }

    private void refreshDashboard() {
        totalincomelable.setText(formatMoney(saleDAO.getTodayIncome()));
        monthlyincomelable.setText(formatMoney(saleDAO.getMonthlyIncome()));
        monthlycostlable.setText(formatMoney(cashFlowDAO.getMonthlyCost()));
        totalcostlable.setText(formatMoney(cashFlowDAO.getTotalReserved()));

        int approvedSalesmen = employeeDAO.countByRoleAndStatus("Salesman", "Approved");
        int regularSalesmen = Math.max(0, approvedSalesmen - 1); // one of them is the best seller
        numofregularsalesmanlable.setText(String.valueOf(regularSalesmen));
        numofinventorymanagerlable.setText(String.valueOf(employeeDAO.countByRoleAndStatus("Inventory Manager", "Approved")));

        buildLineChart(monthlyincomelinechat, saleDAO.getMonthlyIncomeHistory(6), "Income");
        buildLineChart(monthlycostlinechart, cashFlowDAO.getMonthlyCostHistory(6), "Cost");
    }

    private void buildLineChart(LineChart<String, Number> chart, Map<String, Double> data, String seriesName) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        data.forEach((month, value) -> series.getData().add(new XYChart.Data<>(month, value)));
        chart.getData().setAll(series);
    }

    @FXML
    private void withdrawbtn() {
        String reason = withdralreasoncombox.getValue();
        Double amount = parsePositiveDouble(withdralamount.getText());

        if (reason == null) {
            AlertHelper.warning("Missing Reason", "Please choose a withdrawal reason.");
            return;
        }
        if (amount == null) {
            AlertHelper.warning("Invalid Amount", "Please enter a valid positive amount.");
            return;
        }

        cashFlowDAO.addExpense(reason, reason + " withdrawal", amount);
        withdralamount.clear();
        withdralreasoncombox.setValue(null);
        refreshDashboard();
        AlertHelper.info("Withdrawal Recorded", String.format("%.2f taka withdrawn for %s.", amount, reason));
    }

    @FXML
    private void paybtn() {
        Double baseSalary = parsePositiveDouble(bestsalesmansalaryamountfield.getText());
        Double incrementPercent = parseNonNegativeDouble(incrimentpercentagefiled.getText());
        Double inventorySalary = parseNonNegativeDouble(inventorysalaryammountfield.getText());

        if (baseSalary == null) {
            AlertHelper.warning("Missing Salary", "Please enter the salesman salary amount first.");
            return;
        }
        if (incrementPercent == null) incrementPercent = 0.0;
        if (inventorySalary == null) inventorySalary = 0.0;

        cashFlowDAO.saveSalaryConfig(baseSalary, incrementPercent, inventorySalary);

        String[] bestSeller = saleDAO.getBestSellerOfMonth();
        String bestSellerId = bestSeller[0];

        List<Employee> salesmen = employeeDAO.getApprovedByRole("Salesman");
        for (Employee salesman : salesmen) {
            boolean isBestSeller = salesman.getEmployeeCode() != null && salesman.getEmployeeCode().equals(bestSellerId);
            double salary = isBestSeller ? baseSalary + (baseSalary * incrementPercent / 100.0) : baseSalary;
            String label = isBestSeller ? " (Best Seller bonus)" : "";
            cashFlowDAO.addExpense(CashFlowDAO.CATEGORY_SALARY,
                    "Salary - " + salesman.getName() + " (" + salesman.getEmployeeCode() + ")" + label, salary);
        }

        List<Employee> inventoryManagers = employeeDAO.getApprovedByRole("Inventory Manager");
        for (Employee manager : inventoryManagers) {
            cashFlowDAO.addExpense(CashFlowDAO.CATEGORY_SALARY,
                    "Salary - " + manager.getName() + " (" + manager.getEmployeeCode() + ")", inventorySalary);
        }

        refreshDashboard();
        AlertHelper.info("Salaries Paid", "This month's employee salaries have been recorded.");
    }

    @FXML
    private void showMonthlyCostDetails() {
        Map<String, Double> breakdown = cashFlowDAO.getMonthlyCostBreakdown();
        String message = String.format(
                "Employee Salary: %.2f taka%nInventory Update: %.2f taka%nPersonal: %.2f taka",
                breakdown.getOrDefault(CashFlowDAO.CATEGORY_SALARY, 0.0),
                breakdown.getOrDefault(CashFlowDAO.CATEGORY_INVENTORY, 0.0),
                breakdown.getOrDefault(CashFlowDAO.CATEGORY_PERSONAL, 0.0));
        AlertHelper.info("Monthly Cost Details", message);
    }

    private String formatMoney(double value) {
        return String.format("%.2f", value);
    }

    private String trimZero(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private Double parsePositiveDouble(String text) {
        Double value = parseNonNegativeDouble(text);
        return (value == null || value <= 0) ? null : value;
    }

    private Double parseNonNegativeDouble(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            double value = Double.parseDouble(text.trim());
            return value < 0 ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}


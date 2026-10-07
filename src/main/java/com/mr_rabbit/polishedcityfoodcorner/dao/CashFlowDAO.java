package com.mr_rabbit.polishedcityfoodcorner.dao;

import com.mr_rabbit.polishedcityfoodcorner.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;


public class CashFlowDAO {

    public static final String CATEGORY_SALARY = "Employee Salary";
    public static final String CATEGORY_PERSONAL = "Personal";
    public static final String CATEGORY_INVENTORY = "For Inventory Update";

    public boolean addExpense(String category, String description, double amount) {
        String sql = "INSERT INTO cash_expenses (category, description, amount, expense_date) VALUES (?, ?, ?, NOW())";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, category);
            ps.setString(2, description);
            ps.setDouble(3, amount);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getMonthlyCost() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM cash_expenses " +
                "WHERE MONTH(expense_date) = MONTH(CURDATE()) AND YEAR(expense_date) = YEAR(CURDATE())";
        return singleDoubleQuery(sql);
    }

    /** Employee salary / Personal / Inventory Update totals for the current month, for the pop-up breakdown. */
    public Map<String, Double> getMonthlyCostBreakdown() {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put(CATEGORY_SALARY, 0.0);
        breakdown.put(CATEGORY_INVENTORY, 0.0);
        breakdown.put(CATEGORY_PERSONAL, 0.0);

        String sql = "SELECT category, COALESCE(SUM(amount), 0) as total FROM cash_expenses " +
                "WHERE MONTH(expense_date) = MONTH(CURDATE()) AND YEAR(expense_date) = YEAR(CURDATE()) " +
                "GROUP BY category";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                breakdown.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return breakdown;
    }

    /** Cost per month for the last {@code months} months, oldest first - feeds the CashFlow cost line chart. */
    public Map<String, Double> getMonthlyCostHistory(int months) {
        Map<String, Double> result = new LinkedHashMap<>();
        DateTimeFormatter monthKey = DateTimeFormatter.ofPattern("MMM yy");
        LocalDate cursor = LocalDate.now().minusMonths(months - 1L).withDayOfMonth(1);
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM cash_expenses WHERE MONTH(expense_date) = ? AND YEAR(expense_date) = ?";
        try (Connection con = DatabaseConnection.getConnect()) {
            for (int i = 0; i < months; i++) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, cursor.getMonthValue());
                    ps.setInt(2, cursor.getYear());
                    try (ResultSet rs = ps.executeQuery()) {
                        double value = rs.next() ? rs.getDouble(1) : 0;
                        result.put(cursor.format(monthKey), value);
                    }
                }
                cursor = cursor.plusMonths(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private double getIncomeForMonth(YearMonth ym) {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE MONTH(sale_date) = ? AND YEAR(sale_date) = ?";
        return monthlyAmount(sql, ym);
    }

    private double getCostForMonth(YearMonth ym) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM cash_expenses WHERE MONTH(expense_date) = ? AND YEAR(expense_date) = ?";
        return monthlyAmount(sql, ym);
    }

    private double monthlyAmount(String sql, YearMonth ym) {
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ym.getMonthValue());
            ps.setInt(2, ym.getYear());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns the current "Total Reserved" figure, first rolling forward any
     * fully completed month(s) whose leftover (income - cost) has not yet been
     * folded into the reserve.
     */
    public double getTotalReserved() {
        double totalIncome = 0;
        double totalCost = 0;

        try (Connection con = DatabaseConnection.getConnect()) {
            try (java.sql.Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(total_amount), 0) FROM sales")) {
                if (rs.next()) {
                    totalIncome = rs.getDouble(1);
                }
            }
            try (java.sql.Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(amount), 0) FROM cash_expenses")) {
                if (rs.next()) {
                    totalCost = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalIncome - totalCost;
    }

    private void insertOrUpdateReserve(double totalReserved, String monthKey) {
        String sql = "INSERT INTO cash_reserve (id, total_reserved, last_updated_month) VALUES (1, ?, ?) " +
                "ON DUPLICATE KEY UPDATE total_reserved = ?, last_updated_month = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, totalReserved);
            ps.setString(2, monthKey);
            ps.setDouble(3, totalReserved);
            ps.setString(4, monthKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Persists the salary figures typed into CashFlow so they reload next time the screen opens. */
    public void saveSalaryConfig(double baseSalesmanSalary, double incrementPercentage, double inventoryManagerSalary) {
        String sql = "INSERT INTO salary_config (id, base_salesman_salary, increment_percentage, inventory_manager_salary) " +
                "VALUES (1, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE base_salesman_salary = ?, increment_percentage = ?, inventory_manager_salary = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, baseSalesmanSalary);
            ps.setDouble(2, incrementPercentage);
            ps.setDouble(3, inventoryManagerSalary);
            ps.setDouble(4, baseSalesmanSalary);
            ps.setDouble(5, incrementPercentage);
            ps.setDouble(6, inventoryManagerSalary);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Returns {baseSalesmanSalary, incrementPercentage, inventoryManagerSalary}, all 0 if never saved before. */
    public double[] loadSalaryConfig() {
        String sql = "SELECT base_salesman_salary, increment_percentage, inventory_manager_salary FROM salary_config WHERE id = 1";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new double[]{
                        rs.getDouble("base_salesman_salary"),
                        rs.getDouble("increment_percentage"),
                        rs.getDouble("inventory_manager_salary")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new double[]{0, 0, 0};
    }

    private double singleDoubleQuery(String sql) {
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

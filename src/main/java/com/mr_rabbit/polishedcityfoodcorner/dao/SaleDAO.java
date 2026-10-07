package com.mr_rabbit.polishedcityfoodcorner.dao;

import com.mr_rabbit.polishedcityfoodcorner.db.DatabaseConnection;
import com.mr_rabbit.polishedcityfoodcorner.model.HistoryRecord;
import com.mr_rabbit.polishedcityfoodcorner.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SaleDAO {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public int recordSale(String customerName, String customerMobile, double total, double amountPaid,
                           double change, String sellerId, String sellerName, List<OrderItem> items) {
        String insertSale = "INSERT INTO sales (customer_name, customer_mobile, total_amount, amount_paid, " +
                "change_amount, seller_id, seller_name, sale_date) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        String insertItem = "INSERT INTO sale_items (sale_id, product_id, product_name, quantity, unit_price, line_total) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String reduceStock = "UPDATE products SET stock = stock - ? WHERE product_id = ? AND stock >= ?";

        Connection con = null;
        try {
            con = DatabaseConnection.getConnect();
            con.setAutoCommit(false);

            int saleId;
            try (PreparedStatement ps = con.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, customerName);
                ps.setString(2, customerMobile);
                ps.setDouble(3, total);
                ps.setDouble(4, amountPaid);
                ps.setDouble(5, change);
                ps.setString(6, sellerId);
                ps.setString(7, sellerName);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        con.rollback();
                        return -1;
                    }
                    saleId = keys.getInt(1);
                }
            }

            try (PreparedStatement itemPs = con.prepareStatement(insertItem);
                 PreparedStatement stockPs = con.prepareStatement(reduceStock)) {
                for (OrderItem item : items) {
                    itemPs.setInt(1, saleId);
                    itemPs.setString(2, item.getProductId());
                    itemPs.setString(3, item.getProductName());
                    itemPs.setInt(4, item.getQuantity());
                    itemPs.setDouble(5, item.getUnitPrice());
                    itemPs.setDouble(6, item.getLineTotal());
                    itemPs.addBatch();

                    stockPs.setInt(1, item.getQuantity());
                    stockPs.setString(2, item.getProductId());
                    stockPs.setInt(3, item.getQuantity());
                    stockPs.addBatch();
                }
                itemPs.executeBatch();
                stockPs.executeBatch();
            }

            con.commit();
            return saleId;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return -1;
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public double getTodayIncome() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE DATE(sale_date) = CURDATE()";
        return singleDoubleQuery(sql);
    }

    public double getMonthlyIncome() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales " +
                "WHERE MONTH(sale_date) = MONTH(CURDATE()) AND YEAR(sale_date) = YEAR(CURDATE())";
        return singleDoubleQuery(sql);
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

    /** Income per month for the last {@code months} months, oldest first - feeds the CashFlow line chart. */
    public Map<String, Double> getMonthlyIncomeHistory(int months) {
        Map<String, Double> result = new LinkedHashMap<>();
        DateTimeFormatter monthKey = DateTimeFormatter.ofPattern("MMM yy");
        LocalDate cursor = LocalDate.now().minusMonths(months - 1L).withDayOfMonth(1);
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales " +
                "WHERE MONTH(sale_date) = ? AND YEAR(sale_date) = ?";
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

    /** The employee (seller) whose sales added up to the highest taka this month. */
    public String[] getBestSellerOfMonth() {
        String sql = "SELECT seller_id, seller_name FROM sales " +
                "WHERE MONTH(sale_date) = MONTH(CURDATE()) AND YEAR(sale_date) = YEAR(CURDATE()) " +
                "GROUP BY seller_id, seller_name ORDER BY SUM(total_amount) DESC LIMIT 1";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new String[]{rs.getString("seller_id"), rs.getString("seller_name")};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[]{"-", "-"};
    }

    /** Top 3 best selling products this month, ranked by total quantity sold. */
    public List<String> getTop3SellingItems() {
        List<String> names = new ArrayList<>();
        String sql = "SELECT si.product_name, SUM(si.quantity) as qty FROM sale_items si " +
                "JOIN sales s ON si.sale_id = s.sale_id " +
                "WHERE MONTH(s.sale_date) = MONTH(CURDATE()) AND YEAR(s.sale_date) = YEAR(CURDATE()) " +
                "GROUP BY si.product_name ORDER BY qty DESC LIMIT 3";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                names.add(rs.getString("product_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    /** Number of orders per month for the last N months - feeds the Customer Overview bar chart. */
    public Map<String, Integer> getMonthlyOrderCounts(int months) {
        Map<String, Integer> result = new LinkedHashMap<>();
        DateTimeFormatter monthKey = DateTimeFormatter.ofPattern("MMM");
        LocalDate cursor = LocalDate.now().minusMonths(months - 1L).withDayOfMonth(1);
        String sql = "SELECT COUNT(*) FROM sales WHERE MONTH(sale_date) = ? AND YEAR(sale_date) = ?";
        try (Connection con = DatabaseConnection.getConnect()) {
            for (int i = 0; i < months; i++) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, cursor.getMonthValue());
                    ps.setInt(2, cursor.getYear());
                    try (ResultSet rs = ps.executeQuery()) {
                        int count = rs.next() ? rs.getInt(1) : 0;
                        result.put(cursor.format(monthKey), count);
                    }
                }
                cursor = cursor.plusMonths(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * History table rows. Defaults to the current month when both dates are null,
     * otherwise returns rows inside [startDate, endDate].
     */
    public List<HistoryRecord> getHistory(String nameOrMobileQuery, LocalDate startDate, LocalDate endDate) {
        List<HistoryRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.sale_id, s.customer_name, s.customer_mobile, s.total_amount, s.sale_date, s.seller_id, " +
                        "GROUP_CONCAT(CONCAT(si.product_name, ' x', si.quantity) SEPARATOR ', ') AS items " +
                        "FROM sales s JOIN sale_items si ON s.sale_id = si.sale_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (startDate != null && endDate != null) {
            sql.append(" AND DATE(s.sale_date) BETWEEN ? AND ?");
            params.add(java.sql.Date.valueOf(startDate));
            params.add(java.sql.Date.valueOf(endDate));
        } else {
            sql.append(" AND MONTH(s.sale_date) = MONTH(CURDATE()) AND YEAR(s.sale_date) = YEAR(CURDATE())");
        }

        if (nameOrMobileQuery != null && !nameOrMobileQuery.isBlank()) {
            sql.append(" AND (LOWER(s.customer_name) LIKE ? OR s.customer_mobile LIKE ? OR LOWER(s.seller_id) LIKE ?)");
            String like = "%" + nameOrMobileQuery.toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        sql.append(" GROUP BY s.sale_id ORDER BY s.sale_date DESC");

        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String dateDisplay = rs.getTimestamp("sale_date").toLocalDateTime().toLocalDate().format(DISPLAY_DATE);
                    list.add(new HistoryRecord(
                            String.valueOf(rs.getInt("sale_id")),
                            rs.getString("customer_name"),
                            rs.getString("customer_mobile"),
                            rs.getString("items"),
                            String.format("%.2f", rs.getDouble("total_amount")),
                            dateDisplay,
                            rs.getString("seller_id")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

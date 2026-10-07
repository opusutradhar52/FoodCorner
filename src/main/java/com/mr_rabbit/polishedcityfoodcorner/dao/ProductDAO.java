package com.mr_rabbit.polishedcityfoodcorner.dao;

import com.mr_rabbit.polishedcityfoodcorner.db.DatabaseConnection;
import com.mr_rabbit.polishedcityfoodcorner.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class ProductDAO {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public boolean productIdExists(String productId) {
        String sql = "SELECT product_id FROM products WHERE product_id = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Works out the next free product id in the form CFC-001, CFC-002, ... */
    public String generateNextProductId() {
        String sql = "SELECT product_id FROM products ORDER BY id DESC LIMIT 1";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int next = 1;
            if (rs.next()) {
                String lastId = rs.getString("product_id"); // e.g. CFC-007
                String numberPart = lastId.replaceAll("[^0-9]", "");
                if (!numberPart.isEmpty()) {
                    next = Integer.parseInt(numberPart) + 1;
                }
            }
            return String.format("CFC-%03d", next);
        } catch (SQLException e) {
            e.printStackTrace();
            return "CFC-001";
        }
    }

    public boolean addProduct(Product p) {
        String sql = "INSERT INTO products (product_id, product_name, type, stock, price, status, image_path, created_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getProductId());
            ps.setString(2, p.getProductName());
            ps.setString(3, p.getType());
            ps.setInt(4, p.getStock());
            ps.setDouble(5, p.getPrice());
            ps.setString(6, p.getStatus());
            ps.setString(7, p.getImagePath());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(Product p) {
        String sql = "UPDATE products SET product_name = ?, type = ?, stock = ?, price = ?, status = ?, image_path = ? " +
                "WHERE product_id = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getType());
            ps.setInt(3, p.getStock());
            ps.setDouble(4, p.getPrice());
            ps.setString(5, p.getStatus());
            ps.setString(6, p.getImagePath());
            ps.setString(7, p.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Reduces stock after a sale is paid for. */
    public boolean reduceStock(String productId, int quantitySold) {
        String sql = "UPDATE products SET stock = stock - ? WHERE product_id = ? AND stock >= ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quantitySold);
            ps.setString(2, productId);
            ps.setInt(3, quantitySold);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(String productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id DESC";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Only products with stock > 0 - used to populate the food cards on the SaleItems screen. */
    public List<Product> getSellableProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock > 0 ORDER BY product_name";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Product findByName(String productName) {
        String sql = "SELECT * FROM products WHERE product_name = ? LIMIT 1";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, productName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        String dateDisplay = "";
        if (rs.getTimestamp("created_date") != null) {
            dateDisplay = rs.getTimestamp("created_date").toLocalDateTime().toLocalDate().format(DISPLAY_FORMAT);
        }
        return new Product(
                rs.getString("product_id"),
                rs.getString("product_name"),
                rs.getString("type"),
                rs.getInt("stock"),
                rs.getDouble("price"),
                rs.getString("status"),
                dateDisplay,
                rs.getString("image_path")
        );
    }
}

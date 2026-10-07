package com.mr_rabbit.polishedcityfoodcorner.dao;

import com.mr_rabbit.polishedcityfoodcorner.db.DatabaseConnection;
import com.mr_rabbit.polishedcityfoodcorner.model.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Inserts a brand new employee with status = Pending. Returns true on success. */
    public boolean registerEmployee(String name, String email, String mobile, String passwordHash, String role) {
        String sql = "INSERT INTO employees (name, email, mobile, password, role, status, joined_on) " +
                "VALUES (?, ?, ?, ?, ?, 'Pending', NOW())";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, mobile);
            ps.setString(4, passwordHash);
            ps.setString(5, role);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT id FROM employees WHERE email = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean employeeCodeExists(String code) {
        String sql = "SELECT id FROM employees WHERE employee_code = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Used by the sign-in screen. Returns null if no employee has this email + role. */
    public Employee findForLogin(String email, String role) {
        String sql = "SELECT * FROM employees WHERE email = ? AND role = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, role);
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

    public Employee findByCode(String employeeCode) {
        String sql = "SELECT * FROM employees WHERE employee_code = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeCode);
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

    public Employee findByEmailAndMobile(String email, String mobile) {
        String sql = "SELECT * FROM employees WHERE email = ? AND mobile = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, mobile);
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

    public boolean updatePassword(int id, String newPasswordHash) {
        String sql = "UPDATE employees SET password = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY id DESC";
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

    /** Sets the permanent employee code and locks it - can only ever be called once per employee. */
    public boolean confirmEmployee(int id, String employeeCode) {
        String sql = "UPDATE employees SET employee_code = ?, status = 'Approved', id_locked = TRUE WHERE id = ? AND id_locked = FALSE";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeCode);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int id, String newStatus) {
        String sql = "UPDATE employees SET status = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM employees WHERE status = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countByRoleAndStatus(String role, String status) {
        String sql = "SELECT COUNT(*) FROM employees WHERE role = ? AND status = ?";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Approved employees of a given role - used by CashFlow to work out salary payouts. */
    public List<Employee> getApprovedByRole(String role) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE role = ? AND status = 'Approved' ORDER BY employee_code";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        String joinedOnDisplay = "";
        if (rs.getTimestamp("joined_on") != null) {
            joinedOnDisplay = rs.getTimestamp("joined_on").toLocalDateTime().toLocalDate().format(DISPLAY_FORMAT);
        }
        return new Employee(
                rs.getInt("id"),
                rs.getString("employee_code"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("mobile"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getString("status"),
                joinedOnDisplay,
                rs.getBoolean("id_locked")
        );
    }
}

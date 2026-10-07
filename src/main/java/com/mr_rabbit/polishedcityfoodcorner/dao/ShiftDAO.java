package com.mr_rabbit.polishedcityfoodcorner.dao;

import com.mr_rabbit.polishedcityfoodcorner.db.DatabaseConnection;
import com.mr_rabbit.polishedcityfoodcorner.model.ShiftRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class ShiftDAO {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public boolean hasCheckedInToday(String employeeCode) {
        String sql = "SELECT id FROM shift_records WHERE employee_code = ? AND work_date = CURDATE()";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Creates a new shift record with check_in = now and no check_out yet. */
    public boolean checkIn(String employeeCode, String employeeName, String role) {
        String sql = "INSERT INTO shift_records (employee_code, employee_name, role, check_in, work_date) " +
                "VALUES (?, ?, ?, NOW(), CURDATE())";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeCode);
            ps.setString(2, employeeName);
            ps.setString(3, role);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Sets check_out = now on this employee's most recent still-open shift record. */
    public boolean checkOut(String employeeCode) {
        String sql = "UPDATE shift_records SET check_out = NOW() " +
                "WHERE employee_code = ? AND check_out IS NULL " +
                "ORDER BY id DESC LIMIT 1";
        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employeeCode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ShiftRecord> getAllShiftRecords() {
        return getFiltered(null, null, null);
    }

    public List<ShiftRecord> getFiltered(LocalDate startDate, LocalDate endDate, String role) {
        List<ShiftRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM shift_records WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (startDate != null) {
            sql.append(" AND work_date >= ?");
            params.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND work_date <= ?");
            params.add(java.sql.Date.valueOf(endDate));
        }
        if (role != null && !role.isBlank()) {
            sql.append(" AND role = ?");
            params.add(role);
        }
        sql.append(" ORDER BY id DESC");

        try (Connection con = DatabaseConnection.getConnect();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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

    private ShiftRecord mapRow(ResultSet rs) throws SQLException {
        Timestamp checkInTs = rs.getTimestamp("check_in");
        Timestamp checkOutTs = rs.getTimestamp("check_out");

        String checkInStr = checkInTs != null ? checkInTs.toLocalDateTime().format(TIME_FORMAT) : "-";
        String checkOutStr = checkOutTs != null ? checkOutTs.toLocalDateTime().format(TIME_FORMAT) : "-";
        String workingHourStr = "-";

        if (checkInTs != null && checkOutTs != null) {
            LocalDateTime in = checkInTs.toLocalDateTime();
            LocalDateTime out = checkOutTs.toLocalDateTime();
            Duration duration = Duration.between(in, out);
            if (!duration.isNegative()) {
                long hours = duration.toHours();
                long minutes = duration.toMinutesPart();
                workingHourStr = String.format("%02d:%02d", hours, minutes);
            }
        }

        String workDateStr = rs.getDate("work_date") != null
                ? rs.getDate("work_date").toLocalDate().format(DATE_FORMAT) : "";

        return new ShiftRecord(
                rs.getInt("id"),
                rs.getString("employee_code"),
                rs.getString("employee_name"),
                rs.getString("role"),
                checkInStr,
                checkOutStr,
                workingHourStr,
                workDateStr
        );
    }
}

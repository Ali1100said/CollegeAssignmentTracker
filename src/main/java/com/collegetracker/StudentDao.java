package com.collegetracker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDao {

    public record Profile(String fullName, String email) {}

    public Profile findProfile(int studentId) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT full_name, email FROM students WHERE id = ?")) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return new Profile("", "");
                }

                return new Profile(
                    valueOrEmpty(rs.getString("full_name")),
                    valueOrEmpty(rs.getString("email"))
                );
            }
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}

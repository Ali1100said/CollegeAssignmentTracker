package com.collegetracker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

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

    public boolean updateProfile(int studentId, String fullName, String email)
            throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE students SET full_name = ?, email = ? WHERE id = ?")) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setInt(3, studentId);
            return stmt.executeUpdate() == 1;
        }
    }

    public Integer createUnverified(String username, String passwordHash,
                                    String email, String verificationTokenHash,
                                    Timestamp verificationTokenExpires)
            throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO students (username, password_hash, email, " +
                 "email_verified, verification_token_hash, " +
                 "verification_token_expires) VALUES (?, ?, ?, FALSE, ?, ?)",
                 java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, email);
            stmt.setString(4, verificationTokenHash);
            stmt.setTimestamp(5, verificationTokenExpires);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public void deleteUnverified(int studentId) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM students WHERE id = ? AND email_verified = FALSE")) {
            stmt.setInt(1, studentId);
            stmt.executeUpdate();
        }
    }

    public record LoginRecord(int id, String passwordHash, String email,
                              boolean emailVerified) {}

    public LoginRecord findLogin(String username) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id, password_hash, email, email_verified " +
                 "FROM students WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return new LoginRecord(rs.getInt("id"),
                    rs.getString("password_hash"), rs.getString("email"),
                    rs.getBoolean("email_verified"));
            }
        }
    }

    public boolean verifyEmail(String email, String tokenHash)
            throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE students SET email_verified = TRUE, " +
                 "verification_token_hash = NULL, verification_token_expires = NULL " +
                 "WHERE LOWER(email) = ? AND verification_token_hash = ? " +
                 "AND verification_token_expires > CURRENT_TIMESTAMP " +
                 "AND email_verified = FALSE")) {
            stmt.setString(1, email);
            stmt.setString(2, tokenHash);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean setResetToken(String email, String tokenHash,
                                 Timestamp expires) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE students SET reset_token_hash = ?, " +
                 "reset_token_expires = ? WHERE LOWER(email) = ?")) {
            stmt.setString(1, tokenHash);
            stmt.setTimestamp(2, expires);
            stmt.setString(3, email);
            return stmt.executeUpdate() == 1;
        }
    }

    public void clearResetToken(String email) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE students SET reset_token_hash = NULL, " +
                 "reset_token_expires = NULL WHERE LOWER(email) = ?")) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        }
    }

    public boolean resetPassword(String email, String tokenHash,
                                 String passwordHash) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE students SET password_hash = ?, reset_token_hash = NULL, " +
                 "reset_token_expires = NULL WHERE LOWER(email) = ? " +
                 "AND reset_token_hash = ? " +
                 "AND reset_token_expires > CURRENT_TIMESTAMP")) {
            stmt.setString(1, passwordHash);
            stmt.setString(2, email);
            stmt.setString(3, tokenHash);
            return stmt.executeUpdate() == 1;
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}

package com.collegetracker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {

    public record CourseView(int id, String name) {}

    public List<CourseView> findByStudent(int studentId)
            throws SQLException {

        List<CourseView> courses = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id, course_name FROM courses " +
                 "WHERE student_id = ? ORDER BY course_name, id")) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(new CourseView(
                        rs.getInt("id"),
                        rs.getString("course_name")
                    ));
                }
            }
        }

        return courses;
    }

    public void add(int studentId, String name) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO courses (student_id, course_name) VALUES (?, ?)")) {
            stmt.setInt(1, studentId);
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
    }

    public boolean update(int studentId, int courseId, String name)
            throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE courses SET course_name = ? " +
                 "WHERE id = ? AND student_id = ?")) {
            stmt.setString(1, name);
            stmt.setInt(2, courseId);
            stmt.setInt(3, studentId);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean delete(int studentId, int courseId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM assignments WHERE course_id = ? " +
                        "AND course_id IN (SELECT id FROM courses " +
                        "WHERE student_id = ?)")) {
                    stmt.setInt(1, courseId);
                    stmt.setInt(2, studentId);
                    stmt.executeUpdate();
                }
                int deleted;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM courses WHERE id = ? AND student_id = ?")) {
                    stmt.setInt(1, courseId);
                    stmt.setInt(2, studentId);
                    deleted = stmt.executeUpdate();
                }
                conn.commit();
                return deleted == 1;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}

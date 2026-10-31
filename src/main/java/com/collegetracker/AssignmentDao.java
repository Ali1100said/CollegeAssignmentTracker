package com.collegetracker;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDao {

    public record AssignmentView(
            int id,
            int courseId,
            String title,
            Date dueDate,
            boolean completed,
            String courseName) {}

    public List<AssignmentView> findByStudent(int studentId)
            throws SQLException {

        List<AssignmentView> assignments = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT a.id, a.course_id, a.title, a.due_date, " +
                 "a.completed, c.course_name " +
                 "FROM assignments a JOIN courses c " +
                 "ON a.course_id = c.id " +
                 "WHERE c.student_id = ? " +
                 "ORDER BY a.due_date ASC NULLS LAST, a.id")) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assignments.add(new AssignmentView(
                        rs.getInt("id"),
                        rs.getInt("course_id"),
                        rs.getString("title"),
                        rs.getDate("due_date"),
                        rs.getBoolean("completed"),
                        rs.getString("course_name")
                    ));
                }
            }
        }

        return assignments;
    }

    public boolean add(int studentId, int courseId, String title, Date dueDate)
            throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO assignments (course_id, title, due_date) " +
                 "SELECT id, ?, ? FROM courses WHERE id = ? AND student_id = ?")) {
            stmt.setString(1, title);
            if (dueDate == null) stmt.setNull(2, java.sql.Types.DATE);
            else stmt.setDate(2, dueDate);
            stmt.setInt(3, courseId);
            stmt.setInt(4, studentId);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean update(int studentId, int assignmentId, int courseId,
                          String title, Date dueDate) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE assignments SET course_id = ?, title = ?, due_date = ? " +
                 "WHERE id = ? AND course_id IN " +
                 "(SELECT id FROM courses WHERE student_id = ?) " +
                 "AND EXISTS (SELECT 1 FROM courses WHERE id = ? AND student_id = ?)")) {
            stmt.setInt(1, courseId);
            stmt.setString(2, title);
            if (dueDate == null) stmt.setNull(3, java.sql.Types.DATE);
            else stmt.setDate(3, dueDate);
            stmt.setInt(4, assignmentId);
            stmt.setInt(5, studentId);
            stmt.setInt(6, courseId);
            stmt.setInt(7, studentId);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean setCompleted(int studentId, int assignmentId,
                                boolean completed) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE assignments SET completed = ? WHERE id = ? " +
                 "AND course_id IN (SELECT id FROM courses WHERE student_id = ?)")) {
            stmt.setBoolean(1, completed);
            stmt.setInt(2, assignmentId);
            stmt.setInt(3, studentId);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean delete(int studentId, int assignmentId) throws SQLException {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM assignments WHERE id = ? AND course_id IN " +
                 "(SELECT id FROM courses WHERE student_id = ?)")) {
            stmt.setInt(1, assignmentId);
            stmt.setInt(2, studentId);
            return stmt.executeUpdate() == 1;
        }
    }
}

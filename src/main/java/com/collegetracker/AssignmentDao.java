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
}

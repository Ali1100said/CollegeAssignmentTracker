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
}

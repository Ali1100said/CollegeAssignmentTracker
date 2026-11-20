package com.collegetracker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.time.Instant;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.collegetracker.service.DashboardFacade;

class DashboardFacadeTest {
    @BeforeAll
    static void initializeDatabase() throws Exception {
        System.setProperty("TRACKER_DB_URL",
                "jdbc:hsqldb:mem:tracker-tests;shutdown=false");
        DatabaseSetup.initialize();
    }

    @Test
    void facadeCoordinatesStudentScopedDataAndSelectedStrategy() throws Exception {
        StudentDao students = new StudentDao();
        int studentId = students.createUnverified("facade-student", "hash",
                "facade@example.test", "token",
                Timestamp.from(Instant.now().plusSeconds(900)));
        students.updateProfile(studentId, "Facade Student", "facade@example.test");

        CourseDao courses = new CourseDao();
        courses.add(studentId, "Architecture");
        int courseId = courses.findByStudent(studentId).get(0).id();

        AssignmentDao assignments = new AssignmentDao();
        assignments.add(studentId, courseId, "Zulu", null);
        assignments.add(studentId, courseId, "Alpha", null);

        var data = new DashboardFacade().loadDashboard(studentId, "title");

        assertEquals("Facade Student", data.profile().fullName());
        assertEquals("Architecture", data.courses().get(0).name());
        assertEquals("title", data.sortKey());
        assertEquals("Alpha", data.assignments().get(0).title());
        assertEquals("Zulu", data.assignments().get(1).title());
    }
}

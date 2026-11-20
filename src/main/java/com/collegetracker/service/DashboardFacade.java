package com.collegetracker.service;

import java.sql.SQLException;
import java.util.List;

import com.collegetracker.AssignmentDao;
import com.collegetracker.AssignmentDao.AssignmentView;
import com.collegetracker.CourseDao;
import com.collegetracker.CourseDao.CourseView;
import com.collegetracker.StudentDao;
import com.collegetracker.StudentDao.Profile;
import com.collegetracker.strategy.AssignmentSortingService;
import com.collegetracker.strategy.AssignmentSortingService.SortResult;

/**
 * Facade for the dashboard use case. Controllers make one coordinated call
 * instead of knowing how profile, course, and assignment data are retrieved.
 */
public final class DashboardFacade {
    private final StudentDao studentDao;
    private final CourseDao courseDao;
    private final AssignmentDao assignmentDao;
    private final AssignmentSortingService sortingService;

    public DashboardFacade() {
        this(new StudentDao(), new CourseDao(), new AssignmentDao(),
                new AssignmentSortingService());
    }

    public DashboardFacade(StudentDao studentDao, CourseDao courseDao,
            AssignmentDao assignmentDao, AssignmentSortingService sortingService) {
        this.studentDao = studentDao;
        this.courseDao = courseDao;
        this.assignmentDao = assignmentDao;
        this.sortingService = sortingService;
    }

    public DashboardData loadDashboard(int studentId, String requestedSort)
            throws SQLException {
        Profile profile = studentDao.findProfile(studentId);
        List<CourseView> courses = courseDao.findByStudent(studentId);
        SortResult sorted = sortingService.sort(
                assignmentDao.findByStudent(studentId), requestedSort);
        return new DashboardData(profile, courses, sorted.assignments(), sorted.key());
    }

    public record DashboardData(Profile profile, List<CourseView> courses,
            List<AssignmentView> assignments, String sortKey) {}
}

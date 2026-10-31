package com.collegetracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.Instant;

import javax.mail.MessagingException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PersistenceIsolationTest {
    private static int studentOne;
    private static int studentTwo;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        System.setProperty("TRACKER_DB_URL",
            "jdbc:hsqldb:mem:tracker-tests;shutdown=false");
        DatabaseSetup.initialize();
        StudentDao dao = new StudentDao();
        Timestamp future = Timestamp.from(Instant.now().plusSeconds(900));
        studentOne = dao.createUnverified("student-one", "hash-one",
            "one@example.test", "verification-one", future);
        studentTwo = dao.createUnverified("student-two", "hash-two",
            "two@example.test", "verification-two", future);
    }

    @Test
    void profileUpdatePersistsAndDoesNotChangeAnotherStudent() throws Exception {
        StudentDao writer = new StudentDao();
        assertTrue(writer.updateProfile(studentOne, "Student One",
            "one-updated@example.test"));

        // A fresh DAO/connection represents dashboard refresh or a later login.
        StudentDao reader = new StudentDao();
        assertEquals(new StudentDao.Profile("Student One",
            "one-updated@example.test"), reader.findProfile(studentOne));
        assertEquals(new StudentDao.Profile("", "two@example.test"),
            reader.findProfile(studentTwo));
    }

    @Test
    void courseAndAssignmentOperationsAreIsolatedByStudent() throws Exception {
        CourseDao courses = new CourseDao();
        AssignmentDao assignments = new AssignmentDao();
        courses.add(studentOne, "CST8288");
        courses.add(studentTwo, "CST8284");
        int courseOne = courses.findByStudent(studentOne).get(0).id();
        int courseTwo = courses.findByStudent(studentTwo).get(0).id();

        assertTrue(assignments.add(studentOne, courseOne, "Capstone", null));
        assertFalse(assignments.add(studentOne, courseTwo, "Cross-account", null));
        int assignmentOne = assignments.findByStudent(studentOne).get(0).id();

        assertFalse(assignments.setCompleted(studentTwo, assignmentOne, true));
        assertFalse(assignments.delete(studentTwo, assignmentOne));
        assertFalse(courses.update(studentTwo, courseOne, "Hijacked"));
        assertFalse(courses.delete(studentTwo, courseOne));

        assertEquals("CST8288", courses.findByStudent(studentOne).get(0).name());
        assertFalse(assignments.findByStudent(studentOne).get(0).completed());

        assertTrue(courses.update(studentOne, courseOne, "CST8288 Capstone"));
        assertTrue(assignments.update(studentOne, assignmentOne, courseOne,
            "Stage 2", java.sql.Date.valueOf("2026-11-01")));
        assertTrue(assignments.setCompleted(studentOne, assignmentOne, true));
        assertEquals("Stage 2",
            assignments.findByStudent(studentOne).get(0).title());
        assertTrue(assignments.findByStudent(studentOne).get(0).completed());
        assertTrue(assignments.delete(studentOne, assignmentOne));
        assertTrue(courses.delete(studentOne, courseOne));
        assertTrue(assignments.findByStudent(studentOne).isEmpty());
        assertTrue(courses.findByStudent(studentOne).isEmpty());
    }

    @Test
    void expiredResetTokenCannotChangePassword() throws Exception {
        StudentDao dao = new StudentDao();
        dao.setResetToken("two@example.test", "expired-token",
            Timestamp.from(Instant.now().minusSeconds(1)));
        assertFalse(dao.resetPassword("two@example.test", "expired-token",
            "new-password-hash"));
        assertEquals("hash-two", dao.findLogin("student-two").passwordHash());
    }

    @Test
    void expiredVerificationTokenCannotVerifyAccount() throws Exception {
        StudentDao dao = new StudentDao();
        dao.createUnverified("expired-verification", "hash",
            "expired@example.test", "expired-verification-hash",
            Timestamp.from(Instant.now().minusSeconds(1)));
        assertFalse(dao.verifyEmail("expired@example.test",
            "expired-verification-hash"));
        assertFalse(dao.findLogin("expired-verification").emailVerified());
    }

    @Test
    void registrationVerificationAndPasswordResetUseDeliveredCodes()
            throws Exception {
        CapturingMailService mail = new CapturingMailService();
        AuthService auth = new AuthService(new StudentDao(), mail);

        auth.register("auth-student", "initial-password",
            "auth@example.test");
        assertTrue(mail.verificationCode != null
            && !mail.verificationCode.isBlank());
        assertFalse(auth.authenticate("auth-student", "initial-password")
            .emailVerified());
        assertTrue(auth.verify("auth@example.test", mail.verificationCode));
        assertTrue(auth.authenticate("auth-student", "initial-password")
            .emailVerified());

        auth.requestPasswordReset("auth@example.test");
        assertTrue(mail.resetCode != null && !mail.resetCode.isBlank());
        assertTrue(auth.resetPassword("auth@example.test", mail.resetCode,
            "replacement-password"));
        assertTrue(auth.authenticate("auth-student", "replacement-password")
            != null);
        assertTrue(auth.authenticate("auth-student", "initial-password")
            == null);
    }

    private static class CapturingMailService extends MailService {
        private String verificationCode;
        private String resetCode;

        @Override
        public void sendVerificationCode(String recipient, String code)
                throws MessagingException {
            verificationCode = code;
        }

        @Override
        public void sendPasswordResetCode(String recipient, String code)
                throws MessagingException {
            resetCode = code;
        }
    }
}

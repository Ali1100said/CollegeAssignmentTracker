package com.collegetracker;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

import javax.mail.MessagingException;

public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TOKEN_LIFETIME_SECONDS = 900;

    private final StudentDao studentDao;
    private final MailService mailService;

    public AuthService() {
        this(new StudentDao(), new MailService());
    }

    AuthService(StudentDao studentDao, MailService mailService) {
        this.studentDao = studentDao;
        this.mailService = mailService;
    }

    public void register(String username, String password, String email)
            throws SQLException, MessagingException {
        String token = generateToken();
        Timestamp expires = Timestamp.from(
            Instant.now().plusSeconds(TOKEN_LIFETIME_SECONDS));
        Integer studentId = studentDao.createUnverified(
            username, hash(password), email, hash(token), expires);
        if (studentId == null) throw new SQLException("Student was not created.");
        try {
            mailService.sendVerificationCode(email, token);
        } catch (MessagingException e) {
            studentDao.deleteUnverified(studentId);
            throw e;
        }
    }

    public StudentDao.LoginRecord authenticate(String username, String password)
            throws SQLException {
        StudentDao.LoginRecord record = studentDao.findLogin(username);
        return record != null && MessageDigest.isEqual(
            record.passwordHash().getBytes(StandardCharsets.UTF_8),
            hash(password).getBytes(StandardCharsets.UTF_8)) ? record : null;
    }

    public boolean verify(String email, String token) throws SQLException {
        return studentDao.verifyEmail(email, hash(token));
    }

    public void requestPasswordReset(String email)
            throws SQLException, MessagingException {
        String token = generateToken();
        boolean found = studentDao.setResetToken(email, hash(token),
            Timestamp.from(Instant.now().plusSeconds(TOKEN_LIFETIME_SECONDS)));
        if (!found) return; // Do not reveal whether the address is registered.
        try {
            mailService.sendPasswordResetCode(email, token);
        } catch (MessagingException e) {
            studentDao.clearResetToken(email);
            throw e;
        }
    }

    public boolean resetPassword(String email, String token, String newPassword)
            throws SQLException {
        return studentDao.resetPassword(email, hash(token), hash(newPassword));
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(
                digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

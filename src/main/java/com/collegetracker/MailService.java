package com.collegetracker;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/** Sends account codes without exposing them in application logs. */
public class MailService {

    public void sendVerificationCode(String recipient, String code)
            throws MessagingException {
        send(recipient, "College Assignment Tracker verification code",
            "Your verification code is: " + code +
            "\n\nThis code expires in 15 minutes. If you did not register, ignore this email.");
    }

    public void sendPasswordResetCode(String recipient, String code)
            throws MessagingException {
        send(recipient, "College Assignment Tracker password reset code",
            "Your password reset code is: " + code +
            "\n\nThis code expires in 15 minutes. If you did not request it, ignore this email.");
    }

    private void send(String recipient, String subject, String body)
            throws MessagingException {
        String host = required("SMTP_HOST");
        String from = required("SMTP_FROM");
        String username = setting("SMTP_USERNAME", "");
        String password = setting("SMTP_PASSWORD", "");
        boolean authenticate = !username.isBlank();

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", setting("SMTP_PORT", "587"));
        properties.put("mail.smtp.auth", Boolean.toString(authenticate));
        properties.put("mail.smtp.starttls.enable",
            setting("SMTP_STARTTLS", "true"));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        Authenticator authenticator = authenticate ? new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        } : null;

        Session session = Session.getInstance(properties, authenticator);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipient(Message.RecipientType.TO,
            new InternetAddress(recipient));
        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");
        Transport.send(message);
    }

    private static String required(String name) throws MessagingException {
        String value = setting(name, "");
        if (value.isBlank()) {
            throw new MessagingException(name + " is not configured");
        }
        return value;
    }

    private static String setting(String name, String defaultValue) {
        String systemValue = System.getProperty(name);
        if (systemValue != null) return systemValue;
        String environmentValue = System.getenv(name);
        return environmentValue == null ? defaultValue : environmentValue;
    }
}

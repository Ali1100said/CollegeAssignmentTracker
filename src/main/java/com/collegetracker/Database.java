package com.collegetracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    // Separate database for Stage 2 testing.
    // Your original database remains untouched.
    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");

        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "HyperSQL database driver is missing.", e
            );
        }

        return DriverManager.getConnection(
            setting("TRACKER_DB_URL",
                "jdbc:hsqldb:file:collegetrackerdb;shutdown=true"),
            setting("TRACKER_DB_USER", "SA"),
            setting("TRACKER_DB_PASSWORD", "")
        );
    }

    private static String setting(String name, String defaultValue) {
        String systemValue = System.getProperty(name);
        if (systemValue != null) {
            return systemValue;
        }
        String environmentValue = System.getenv(name);
        return environmentValue == null ? defaultValue : environmentValue;
    }
}


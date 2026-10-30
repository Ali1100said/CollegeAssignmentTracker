package com.collegetracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    // Separate database for Stage 2 testing.
    // Your original database remains untouched.
    private static final String URL =
        "jdbc:hsqldb:file:collegetrackerdb;shutdown=true";

    private static final String USER = "SA";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");

        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "HyperSQL database driver is missing.", e
            );
        }

        return DriverManager.getConnection(
            URL, USER, PASSWORD
        );
    }
}


package com.collegetracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

class DatabaseSchemaTest {
    @Test
    void standaloneDdlInitializesFreshDatabaseWithExpectedTablesAndIndexes()
            throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:hsqldb:mem:schema-test;shutdown=false", "SA", "")) {
            String ddl = Files.readString(Path.of("database.sql"))
                    .replaceAll("(?m)^\\s*--.*$", "");
            for (String statementText : ddl.split(";")) {
                if (!statementText.isBlank()) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(statementText);
                    }
                }
            }

            assertEquals(3, count(connection,
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME IN " +
                    "('STUDENTS', 'COURSES', 'ASSIGNMENTS')"));
            assertTrue(count(connection,
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SYSTEM_INDEXINFO " +
                    "WHERE TABLE_SCHEM = 'PUBLIC' AND INDEX_NAME IN " +
                    "('IDX_STUDENTS_EMAIL', 'IDX_COURSES_STUDENT', " +
                    "'IDX_ASSIGNMENTS_COURSE', 'IDX_ASSIGNMENTS_DUE_DATE')") >= 4);
        }
    }

    private static int count(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }
}

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.collegetracker.Database" %>
<%@ page import="com.collegetracker.DatabaseSetup" %>

<%!
    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>

<%
    if (session.getAttribute("studentId") == null) {
        response.sendRedirect("index.jsp");
        return;
    }

    int studentId = (Integer) session.getAttribute("studentId");
    DatabaseSetup.initialize();
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>College Assignment Tracker</title>
<style>
    body {
        font-family: Arial, sans-serif;
        background: #eef2f7;
        margin: 0;
    }
    header {
        background: #244b8a;
        color: white;
        padding: 20px 35px;
    }
    main {
        max-width: 1000px;
        margin: 30px auto;
        background: white;
        padding: 30px;
        border-radius: 12px;
    }
    h2 { color: #244b8a; }
    input, select {
        padding: 9px;
        margin: 4px;
        border: 1px solid #ccc;
        border-radius: 6px;
    }
    button {
        padding: 9px 15px;
        background: #244b8a;
        color: white;
        border: none;
        border-radius: 6px;
        cursor: pointer;
    }
    .section {
        border: 1px solid #ddd;
        padding: 20px;
        margin-top: 20px;
        border-radius: 8px;
    }
    li { padding: 10px 0; }
    .item {
        margin-bottom: 12px;
        border-bottom: 1px solid #eee;
        padding-bottom: 12px;
    }
    .actions {
        margin-top: 8px;
    }
    .actions form {
        display: inline-block;
        margin: 3px;
    }
    .green { background: #27864a; }
    .red { background: #b53d3d; }
    .gray { background: #777; }
    .completed { color: #27864a; font-weight: bold; }
    .pending { color: #ad6511; font-weight: bold; }
    .edit-form {
        display: none;
        margin-top: 10px;
        padding: 10px;
        background: #f3f6fb;
        border-radius: 6px;
    }
</style>

<script>
    function toggleEdit(id) {
        const element = document.getElementById(id);
        element.style.display =
            element.style.display === "block" ? "none" : "block";
    }

    function confirmDelete(message) {
        return confirm(message);
    }
</script>
</head>

<body>
<header>
    <h1>College Assignment Tracker</h1>
</header>

<main>
<h2>Student Dashboard</h2>
<p>Manage your courses and assignments.</p>

<div class="section">
    <h2>Add a Course</h2>
    <form action="courses" method="post">
        <input type="hidden" name="action" value="add">
        <input type="text" name="courseName"
               placeholder="Course name" maxlength="150" required>
        <button type="submit">Add Course</button>
    </form>
</div>

<div class="section">
<h2>My Courses</h2>
<ul>
<%
try (Connection conn = Database.getConnection();
     PreparedStatement stmt = conn.prepareStatement(
         "SELECT id, course_name FROM courses " +
         "WHERE student_id = ? ORDER BY id")) {

    stmt.setInt(1, studentId);

    try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            int courseId = rs.getInt("id");
            String courseName = escapeHtml(
                rs.getString("course_name"));
%>
<li class="item">
    <strong><%= courseName %></strong>

    <div class="actions">
        <button type="button"
                onclick="toggleEdit('course-<%= courseId %>')">
            Edit
        </button>

        <form action="courses" method="post"
              onsubmit="return confirmDelete('Delete this course and all its assignments?');">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="courseId"
                   value="<%= courseId %>">
            <button type="submit" class="red">Delete</button>
        </form>
    </div>

    <form id="course-<%= courseId %>"
          class="edit-form"
          action="courses" method="post">

        <input type="hidden" name="action" value="edit">
        <input type="hidden" name="courseId"
               value="<%= courseId %>">

        <input type="text" name="courseName"
               value="<%= courseName %>"
               maxlength="150" required>

        <button type="submit" class="green">
            Save Changes
        </button>
    </form>
</li>
<%
        }
    }
}
%>
</ul>
</div>

<div class="section">
<h2>Add an Assignment</h2>

<form action="assignments" method="post">
    <input type="hidden" name="action" value="add">

    <select name="courseId" required>
        <option value="">Select a course</option>
<%
try (Connection conn = Database.getConnection();
     PreparedStatement stmt = conn.prepareStatement(
         "SELECT id, course_name FROM courses " +
         "WHERE student_id = ? ORDER BY course_name")) {

    stmt.setInt(1, studentId);

    try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
%>
        <option value="<%= rs.getInt("id") %>">
            <%= escapeHtml(rs.getString("course_name")) %>
        </option>
<%
        }
    }
}
%>
    </select>

    <input type="text" name="title"
           placeholder="Assignment title"
           maxlength="200" required>
    <input type="date" name="dueDate">
    <button type="submit">Add Assignment</button>
</form>
</div>

<div class="section">
<h2>My Assignments</h2>
<ul>
<%
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
            int assignmentId = rs.getInt("id");
            int currentCourseId = rs.getInt("course_id");
            String title = escapeHtml(rs.getString("title"));
            String courseName = escapeHtml(
                rs.getString("course_name"));
            Date due = rs.getDate("due_date");
            String dueDate = due == null ? "" : due.toString();
            boolean completed = rs.getBoolean("completed");
%>
<li class="item">
    <strong><%= title %></strong>
    — <%= courseName %>
    — Due: <%= dueDate.isEmpty() ? "No date" : dueDate %>
    —
    <% if (completed) { %>
        <span class="completed">Completed</span>
    <% } else { %>
        <span class="pending">Pending</span>
    <% } %>

    <div class="actions">
        <form action="assignments" method="post">
            <input type="hidden" name="action"
                   value="<%= completed ? "pending" : "complete" %>">
            <input type="hidden" name="assignmentId"
                   value="<%= assignmentId %>">
            <button type="submit" class="green">
                <%= completed ? "Mark Pending" : "Mark Complete" %>
            </button>
        </form>

        <button type="button"
                onclick="toggleEdit('assignment-<%= assignmentId %>')">
            Edit
        </button>

        <form action="assignments" method="post"
              onsubmit="return confirmDelete('Delete this assignment?');">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="assignmentId"
                   value="<%= assignmentId %>">
            <button type="submit" class="red">Delete</button>
        </form>
    </div>

    <form id="assignment-<%= assignmentId %>"
          class="edit-form"
          action="assignments" method="post">

        <input type="hidden" name="action" value="edit">
        <input type="hidden" name="assignmentId"
               value="<%= assignmentId %>">

        <select name="courseId" required>
<%
        try (Connection courseConn = Database.getConnection();
             PreparedStatement courseStmt =
                 courseConn.prepareStatement(
                 "SELECT id, course_name FROM courses " +
                 "WHERE student_id = ? ORDER BY course_name")) {

            courseStmt.setInt(1, studentId);

            try (ResultSet courseRs = courseStmt.executeQuery()) {
                while (courseRs.next()) {
                    int optionId = courseRs.getInt("id");
%>
            <option value="<%= optionId %>"
                <%= optionId == currentCourseId ? "selected" : "" %>>
                <%= escapeHtml(courseRs.getString("course_name")) %>
            </option>
<%
                }
            }
        }
%>
        </select>

        <input type="text" name="title"
               value="<%= title %>"
               maxlength="200" required>

        <input type="date" name="dueDate"
               value="<%= dueDate %>">

        <button type="submit" class="green">
            Save Changes
        </button>
    </form>
</li>
<%
        }
    }
}
%>
</ul>
</div>

<form action="auth" method="post">
    <input type="hidden" name="action" value="logout">
    <button type="submit">Logout</button>
</form>

</main>
</body>
</html>

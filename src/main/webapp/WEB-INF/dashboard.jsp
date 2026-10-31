<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.collegetracker.StudentDao" %>
<%@ page import="com.collegetracker.CourseDao" %>
<%@ page import="com.collegetracker.AssignmentDao" %>

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
    StudentDao.Profile profile =
        (StudentDao.Profile) request.getAttribute("profile");

    List<CourseDao.CourseView> courses =
        (List<CourseDao.CourseView>) request.getAttribute("courses");

    List<AssignmentDao.AssignmentView> assignments =
        (List<AssignmentDao.AssignmentView>)
            request.getAttribute("assignments");
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

    .actions { margin-top: 8px; }
    .actions form { display: inline-block; margin: 3px; }
    .green { background: #27864a; }
    .red { background: #b53d3d; }
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
<header><h1>College Assignment Tracker</h1></header>

<main>
<h2>Student Dashboard</h2>
<p>Manage your courses and assignments.</p>

<div class="section">
    <h2>My Profile</h2>

    <form action="profile" method="post">
        <label for="fullName">Full name</label>
        <input id="fullName" type="text" name="fullName"
               value="<%= escapeHtml(profile.fullName()) %>"
               maxlength="150" required>

        <label for="email">Email</label>
        <input id="email" type="email" name="email"
               value="<%= escapeHtml(profile.email()) %>"
               maxlength="255" required>

        <button type="submit">Save Profile</button>
    </form>

    <% if ("updated".equals(request.getParameter("profile"))) { %>
        <p class="completed">Profile saved.</p>
    <% } %>
</div>

<div class="section">
    <h2>Add a Course</h2>
    <form action="courses" method="post">
        <input type="hidden" name="action" value="add">
        <input type="text" name="courseName" placeholder="Course name"
               maxlength="150" required>
        <button type="submit">Add Course</button>
    </form>
</div>

<div class="section">
<h2>My Courses</h2>
<ul>
<% for (CourseDao.CourseView course : courses) { %>
    <li class="item">
        <strong><%= escapeHtml(course.name()) %></strong>

        <div class="actions">
            <button type="button"
                    onclick="toggleEdit('course-<%= course.id() %>')">
                Edit
            </button>

            <form action="courses" method="post"
                  onsubmit="return confirmDelete('Delete this course and all its assignments?');">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="courseId"
                       value="<%= course.id() %>">
                <button type="submit" class="red">Delete</button>
            </form>
        </div>

        <form id="course-<%= course.id() %>" class="edit-form"
              action="courses" method="post">
            <input type="hidden" name="action" value="edit">
            <input type="hidden" name="courseId"
                   value="<%= course.id() %>">
            <input type="text" name="courseName"
                   value="<%= escapeHtml(course.name()) %>"
                   maxlength="150" required>
            <button type="submit" class="green">Save Changes</button>
        </form>
    </li>
<% } %>
</ul>
</div>

<div class="section">
<h2>Add an Assignment</h2>
<form action="assignments" method="post">
    <input type="hidden" name="action" value="add">
    <select name="courseId" required>
        <option value="">Select a course</option>
        <% for (CourseDao.CourseView course : courses) { %>
            <option value="<%= course.id() %>">
                <%= escapeHtml(course.name()) %>
            </option>
        <% } %>
    </select>
    <input type="text" name="title" placeholder="Assignment title"
           maxlength="200" required>
    <input type="date" name="dueDate">
    <button type="submit">Add Assignment</button>
</form>
</div>

<div class="section">
<h2>My Assignments</h2>
<ul>
<% for (AssignmentDao.AssignmentView assignment : assignments) {
       String dueDate = assignment.dueDate() == null
           ? "" : assignment.dueDate().toString();
%>
    <li class="item">
        <strong><%= escapeHtml(assignment.title()) %></strong>
        — <%= escapeHtml(assignment.courseName()) %>
        — Due: <%= dueDate.isEmpty() ? "No date" : dueDate %>
        —

        <% if (assignment.completed()) { %>
            <span class="completed">Completed</span>
        <% } else { %>
            <span class="pending">Pending</span>
        <% } %>

        <div class="actions">
            <form action="assignments" method="post">
                <input type="hidden" name="action"
                       value="<%= assignment.completed() ? "pending" : "complete" %>">
                <input type="hidden" name="assignmentId"
                       value="<%= assignment.id() %>">
                <button type="submit" class="green">
                    <%= assignment.completed() ? "Mark Pending" : "Mark Complete" %>
                </button>
            </form>

            <button type="button"
                    onclick="toggleEdit('assignment-<%= assignment.id() %>')">
                Edit
            </button>

            <form action="assignments" method="post"
                  onsubmit="return confirmDelete('Delete this assignment?');">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="assignmentId"
                       value="<%= assignment.id() %>">
                <button type="submit" class="red">Delete</button>
            </form>
        </div>

        <form id="assignment-<%= assignment.id() %>" class="edit-form"
              action="assignments" method="post">
            <input type="hidden" name="action" value="edit">
            <input type="hidden" name="assignmentId"
                   value="<%= assignment.id() %>">
            <select name="courseId" required>
                <% for (CourseDao.CourseView course : courses) { %>
                    <option value="<%= course.id() %>"
                        <%= course.id() == assignment.courseId()
                            ? "selected" : "" %>>
                        <%= escapeHtml(course.name()) %>
                    </option>
                <% } %>
            </select>
            <input type="text" name="title"
                   value="<%= escapeHtml(assignment.title()) %>"
                   maxlength="200" required>
            <input type="date" name="dueDate" value="<%= dueDate %>">
            <button type="submit" class="green">Save Changes</button>
        </form>
    </li>
<% } %>
</ul>
</div>

<form action="auth" method="post">
    <input type="hidden" name="action" value="logout">
    <button type="submit">Logout</button>
</form>
</main>
</body>
</html>

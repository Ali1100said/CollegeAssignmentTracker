<%@ page contentType="text/html; charset=UTF-8" %>
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
            padding: 40px 15px;
        }

        .container {
            max-width: 430px;
            margin: auto;
            background: white;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 4px 15px #0002;
        }

        h1, h2 {
            text-align: center;
        }

        h1 {
            color: #244b8a;
            font-size: 25px;
        }

        input {
            width: 100%;
            box-sizing: border-box;
            padding: 12px;
            margin: 8px 0;
            border: 1px solid #ccc;
            border-radius: 6px;
        }

        button {
            width: 100%;
            padding: 12px;
            background: #244b8a;
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
        }

        hr {
            margin: 30px 0;
        }

        .message {
            color: green;
            text-align: center;
        }

        .error {
            color: red;
            text-align: center;
        }
    </style>
</head>

<body>
<div class="container">

    <h1>College Assignment Tracker</h1>

    <p style="text-align:center">
        Manage your courses and assignments.
    </p>

    <% if ("true".equals(request.getParameter("registered"))) { %>
        <p class="message">
            Registration submitted. Check your email to verify your account.
        </p>
    <% } %>

    <% if ("login".equals(request.getParameter("error"))) { %>
        <p class="error">
            Incorrect username or password.
        </p>
    <% } %>

    <% if ("verified".equals(request.getParameter("status"))) { %>
        <p class="message">Email verified successfully.</p>
    <% } %>

    <% if ("reset".equals(request.getParameter("status"))) { %>
        <p class="message">Password updated successfully.</p>
    <% } %>

    <h2>Login</h2>

    <form action="auth" method="post">
        <input type="hidden" name="action" value="login">

        <input type="text"
               name="username"
               placeholder="Username"
               required>

        <input type="password"
               name="password"
               placeholder="Password"
               required>

        <button type="submit">Login</button>
    </form>

    <hr>

    <h2>Create Account</h2>

    <form action="auth" method="post">
        <input type="hidden" name="action" value="register">

        <input type="text"
               name="username"
               placeholder="Choose username"
               required>

        <input type="email"
               name="email"
               placeholder="Email address"
               required>

        <input type="password"
               name="password"
               placeholder="Choose password"
               required>

        <button type="submit">Register</button>
    </form>

    <hr>

    <h2>Verify Email</h2>

    <form action="auth" method="post">
        <input type="hidden" name="action" value="verify">

        <input type="email"
               name="email"
               placeholder="Registered email"
               required>

        <input type="text"
               name="token"
               placeholder="Verification code"
               required>

        <button type="submit">Verify Email</button>
    </form>

    <hr>

    <h2>Forgot Password?</h2>

    <form action="auth" method="post">
        <input type="hidden" name="action" value="forgot">

        <input type="email"
               name="email"
               placeholder="Registered email"
               required>

        <button type="submit">Request Password Reset</button>
    </form>

    <hr>

    <h2>Reset Password</h2>

    <form action="auth" method="post">
        <input type="hidden" name="action" value="reset">

        <input type="email"
               name="email"
               placeholder="Registered email"
               required>

        <input type="text"
               name="token"
               placeholder="Password reset code"
               required>

        <input type="password"
               name="newPassword"
               placeholder="New password"
               required>

        <button type="submit">Reset Password</button>
    </form>

</div>
</body>
</html>

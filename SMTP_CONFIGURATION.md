# SMTP configuration

The application sends verification and password-reset codes by SMTP. Codes are
never printed to the Tomcat console. Configure these environment variables in
the account that starts Tomcat:

| Variable | Required | Example |
|---|---:|---|
| `SMTP_HOST` | Yes | `smtp.example.com` |
| `SMTP_PORT` | No (default `587`) | `587` |
| `SMTP_FROM` | Yes | `tracker@example.com` |
| `SMTP_USERNAME` | For authenticated SMTP | SMTP account name |
| `SMTP_PASSWORD` | For authenticated SMTP | App password or SMTP secret |
| `SMTP_STARTTLS` | No (default `true`) | `true` |

Never commit SMTP credentials. Restart Tomcat after changing its environment.
If delivery is not configured or fails, registration is rolled back and the
user receives HTTP 503. A failed reset delivery clears the new reset token.

## Safe local development

Run a local SMTP capture server such as MailHog or Mailpit, then use:

```text
SMTP_HOST=localhost
SMTP_PORT=1025
SMTP_FROM=tracker@localhost
SMTP_STARTTLS=false
```

Leave `SMTP_USERNAME` and `SMTP_PASSWORD` unset. Read messages only in the
capture server's local web interface. This exercises real SMTP delivery without
using a personal mailbox or exposing codes in application logs.

## Optional database configuration

`TRACKER_DB_URL`, `TRACKER_DB_USER`, and `TRACKER_DB_PASSWORD` can override the
default local HSQLDB connection. Do not commit production database credentials.

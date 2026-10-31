ALTER TABLE students ADD COLUMN email VARCHAR(255);
ALTER TABLE students ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE students ADD COLUMN full_name VARCHAR(150);
ALTER TABLE students ADD COLUMN reset_token_hash VARCHAR(255);
ALTER TABLE students ADD COLUMN reset_token_expires TIMESTAMP;
ALTER TABLE students ADD COLUMN verification_token_hash VARCHAR(255);
ALTER TABLE students ADD COLUMN verification_token_expires TIMESTAMP;
CREATE UNIQUE INDEX idx_students_email ON students(email);

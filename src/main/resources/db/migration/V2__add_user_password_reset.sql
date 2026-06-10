-- V2: Add Password Reset Columns to user Table
ALTER TABLE "user" ADD COLUMN password_reset_token VARCHAR(255);
ALTER TABLE "user" ADD COLUMN password_reset_expiry TIMESTAMPTZ;

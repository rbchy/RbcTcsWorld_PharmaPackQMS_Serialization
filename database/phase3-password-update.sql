-- Phase 3A follow-up: force the 5 demo users onto the new BCrypt password hashes.
-- Safe to re-run any number of times. Unlike seed.sql's INSERT (which silently no-ops
-- on a duplicate username if the users already exist), these UPDATEs always take effect.
-- Demo passwords are unchanged: admin/admin123, qa_user/qa123, supervisor/super123,
-- operator/operator123, inspector/inspect123.
USE pharmapack_qms;
UPDATE users SET password_hash='$2b$10$lL4Y.L/nYdNuk.zkt01RCeddonjZyrRDESRNN3Q/0BFwLstRLL6gq' WHERE username='admin';
UPDATE users SET password_hash='$2b$10$XXUsv1KagKXINU9AhNFZgOqk7GG.wh2.tGV43eErNY5Jl3K2aY7vC' WHERE username='qa_user';
UPDATE users SET password_hash='$2b$10$xGvFQDfwN3iBBKboYL1O4O.l8Cx/cL2.TRyTzHwlwCunLFGEqSdva' WHERE username='supervisor';
UPDATE users SET password_hash='$2b$10$B.yHNG8HFBLQSegOQ7wmBuLfZTamCaVgMYTYQlM7aInZZZFL06Kz6' WHERE username='operator';
UPDATE users SET password_hash='$2b$10$q8GmJHnkL.JjpP7urVXGi.88rtkXftr5PzVa5R9I6KQ7midAjmtZS' WHERE username='inspector';
SELECT id, username, password_hash, status FROM users ORDER BY id;

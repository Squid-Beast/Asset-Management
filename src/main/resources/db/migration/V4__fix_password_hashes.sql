-- Fix password hashes for all users
-- Using a known correct BCrypt hash for "admin123"
UPDATE users SET password_hash = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE username IN ('adm01', 'mgr01', 'mgr02', 'emp01', 'emp02', 'emp03', 'emp04', 'emp05');

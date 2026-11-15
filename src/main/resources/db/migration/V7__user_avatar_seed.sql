ALTER TABLE users RENAME COLUMN avatar TO avatar_seed;
ALTER TABLE users ALTER COLUMN avatar_seed TYPE VARCHAR(64);

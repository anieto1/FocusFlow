-- 1. Rename `password_hash` to `password` for alignment with entity
ALTER TABLE users
    RENAME COLUMN password_hash TO password;

-- 2. Add `first_name` column (temporarily nullable for safe migration)
ALTER TABLE users
    ADD COLUMN first_name VARCHAR(100);

-- 3. Add `last_name` column (temporarily nullable)
ALTER TABLE users
    ADD COLUMN last_name VARCHAR(100);

-- 4. Add `profile_picture_url` column (nullable by design)
ALTER TABLE users
    ADD COLUMN profile_picture_url TEXT;

-- 5. Add `role` column with default, temporarily nullable for existing rows
ALTER TABLE users
    ADD COLUMN role VARCHAR(50) DEFAULT 'USER';

-- 6. Update existing rows with default role
UPDATE users
SET role = 'USER'
WHERE role IS NULL;

-- 7. Make `role` NOT NULL after data is patched
ALTER TABLE users
    ALTER COLUMN role SET NOT NULL;

-- 8. Ensure `created_at` and `updated_at` are NOT NULL
ALTER TABLE users
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN updated_at SET NOT NULL;

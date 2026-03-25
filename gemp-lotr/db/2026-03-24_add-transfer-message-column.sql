-- Add message column to transfer table for league notifications and other user-facing messages.
-- This column is nullable; existing rows will have NULL (no message).
ALTER TABLE transfer ADD COLUMN message TEXT DEFAULT NULL AFTER contents;

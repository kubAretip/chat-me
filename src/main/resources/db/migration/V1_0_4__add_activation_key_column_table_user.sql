ALTER TABLE user
    ADD COLUMN `activation_key` VARCHAR(124) NULL DEFAULT NULL AFTER `activated`;

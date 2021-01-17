ALTER TABLE user
    ADD COLUMN `friend_request_code` VARCHAR(64) NOT NULL AFTER `activation_key`,
    ADD UNIQUE INDEX `unique_user_friend_request_code` (`friend_request_code` ASC) VISIBLE;
;

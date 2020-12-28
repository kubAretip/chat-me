CREATE TABLE IF NOT EXISTS user_authority
(
    user_id        BIGINT      NOT NULL,
    authority_name VARCHAR(50) NOT NULL,
    INDEX `user_fk_idx` (`user_id` ASC) VISIBLE,
    INDEX `authority_fk_idx` (`authority_name` ASC) VISIBLE,
    UNIQUE INDEX `no_duplicate_user_roles` (`user_id` ASC, `authority_name` ASC) VISIBLE,
    CONSTRAINT `user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    CONSTRAINT `authority_fk` FOREIGN KEY (`authority_name`) REFERENCES `authority` (`name`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION
);
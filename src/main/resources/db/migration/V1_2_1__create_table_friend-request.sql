CREATE TABLE IF NOT EXISTS friend_request
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    user_sender_id    BIGINT       NOT NULL,
    user_recipient_id BIGINT       NOT NULL,
    sent_time          DATETIME(6)  NOT NULL,
    status            VARCHAR(125) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `friend_request_user_sender_fk_idx` (`user_sender_id` ASC) VISIBLE,
    INDEX `friend_request_user_recipient_fk_idx` (`user_recipient_id` ASC) VISIBLE,
    UNIQUE INDEX `friend_request_sender_recipient_constraints` (`user_sender_id` ASC, `user_recipient_id` ASC) VISIBLE,
    CONSTRAINT `friend_request_user_sender_fk`
        FOREIGN KEY (`user_sender_id`)
            REFERENCES `chatmedb`.`user` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `friend_request_user_recipient_fk`
        FOREIGN KEY (`user_recipient_id`)
            REFERENCES `chatmedb`.`user` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
);

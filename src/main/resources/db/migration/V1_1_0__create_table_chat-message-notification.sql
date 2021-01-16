CREATE TABLE IF NOT EXISTS chat_message_notification
(
    id            BIGINT              NOT NULL auto_increment,
    user_sender_id        BIGINT      NOT NULL,
    CONSTRAINT `chat_message_notification_user_fk` FOREIGN KEY (`user_sender_id`) REFERENCES `user` (`id`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    PRIMARY KEY (id)
);
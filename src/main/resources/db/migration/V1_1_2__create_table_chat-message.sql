CREATE TABLE IF NOT EXISTS conversation_message
(
    id                BIGINT       NOT NULL auto_increment,
    user_sender_id    BIGINT       NOT NULL,
    user_recipient_id BIGINT       NOT NULL,
    conversation_id   BIGINT       NOT NULL,
    content           varchar(254),
    time              datetime(6)  NOT NULL,
    message_status    varchar(125) NOT NULL,
    CONSTRAINT `chat_message_conversation_with_fk` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`id`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    CONSTRAINT `chat_message_user_sender_fk` FOREIGN KEY (`user_sender_id`) REFERENCES `user` (`id`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    CONSTRAINT `chat_message_user_recipient_fk` FOREIGN KEY (`user_recipient_id`) REFERENCES `user` (`id`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    PRIMARY KEY (id)
);
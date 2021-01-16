CREATE TABLE IF NOT EXISTS conversation
(
    id                   BIGINT NOT NULL auto_increment,
    user_sender_id       BIGINT NOT NULL,
    user_recipient_id    BIGINT NOT NULL,
    conversation_with_id BIGINT NOT NULL,
    CONSTRAINT `conversation_conversation_with_fk` FOREIGN KEY (`conversation_with_id`) REFERENCES `conversation` (`id`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    CONSTRAINT `conversation_user_sender_fk` FOREIGN KEY (`user_sender_id`) REFERENCES `user` (`id`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    CONSTRAINT `conversation_user_recipient_fk` FOREIGN KEY (`user_recipient_id`) REFERENCES `user` (`id`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    PRIMARY KEY (id)
);
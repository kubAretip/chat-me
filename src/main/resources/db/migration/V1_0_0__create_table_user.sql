CREATE TABLE IF NOT EXISTS user
(
    id            BIGINT              NOT NULL auto_increment,
    login         varchar(50)         not null unique,
    password_hash varchar(60)         not null,
    first_name    varchar(50),
    last_name     varchar(50),
    email         varchar(254) unique not null,
    activated     boolean             not null default 0,
    PRIMARY KEY (id)
);
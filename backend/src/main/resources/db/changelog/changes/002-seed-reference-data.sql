--liquibase formatted sql

--changeset codex:002-seed-reference-data
INSERT INTO roles (code, name) VALUES
    ('ADMIN', 'Administrator'),
    ('USER', 'User'),
    ('SHOP_OWNER', 'Shop owner'),
    ('CONTENT_CREATOR', 'Content creator');

INSERT INTO product_categories (code, name) VALUES
    ('MANGA_COMIC', 'Manga and comic'),
    ('TRADING_CARD', 'Trading card'),
    ('FIGURE', 'Figure'),
    ('VIDEOGAME', 'Videogame'),
    ('MERCHANDISING', 'Merchandising'),
    ('MOVIE_SERIES', 'Movie and series');

--rollback DELETE FROM product_categories WHERE code IN ('MANGA_COMIC', 'TRADING_CARD', 'FIGURE', 'VIDEOGAME', 'MERCHANDISING', 'MOVIE_SERIES');
--rollback DELETE FROM roles WHERE code IN ('ADMIN', 'USER', 'SHOP_OWNER', 'CONTENT_CREATOR');

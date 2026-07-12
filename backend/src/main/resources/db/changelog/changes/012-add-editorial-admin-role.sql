--liquibase formatted sql

--changeset codex:012-add-editorial-admin-role
INSERT INTO roles (code, name)
SELECT 'EDITORIAL_ADMIN', 'Editorial administrator'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE code = 'EDITORIAL_ADMIN'
);

--rollback DELETE FROM roles WHERE code = 'EDITORIAL_ADMIN' AND NOT EXISTS (SELECT 1 FROM user_roles ur JOIN roles r ON r.id = ur.role_id WHERE r.code = 'EDITORIAL_ADMIN');

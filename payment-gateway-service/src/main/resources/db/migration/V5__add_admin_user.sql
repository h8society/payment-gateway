-- Вставка пользователя admin
INSERT INTO users (username, email, hashed_password)
VALUES (
           'admin',
           'admin@example.com',
           '$2b$12$tDUMiCqfXi3TRx0OeiLdO.BHTi.y3mt8JB2ObpVpPAjscUQt6z6WG'
       )
    ON CONFLICT (username) DO NOTHING;

-- Назначение роли ADMIN этому пользователю
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.username = 'admin' AND r.role_name = 'ADMIN'
    ON CONFLICT DO NOTHING;

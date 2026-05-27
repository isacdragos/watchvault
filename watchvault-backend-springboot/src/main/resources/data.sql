INSERT INTO roles (name)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

INSERT INTO roles (name)
SELECT 'USER'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER');

INSERT INTO permissions (name)
SELECT seeded.permission_name
FROM (
    VALUES
        ('SHOW_READ'),
        ('SHOW_WRITE'),
        ('USER_MANAGE')
) AS seeded(permission_name)
WHERE NOT EXISTS (
    SELECT 1
    FROM permissions existing_permissions
    WHERE existing_permissions.name = seeded.permission_name
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, permissions.id
FROM roles
JOIN permissions ON permissions.name IN ('SHOW_READ', 'SHOW_WRITE', 'USER_MANAGE')
WHERE roles.name = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions
      WHERE role_permissions.role_id = roles.id
        AND role_permissions.permission_id = permissions.id
  );

INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, permissions.id
FROM roles
JOIN permissions ON permissions.name IN ('SHOW_READ', 'SHOW_WRITE')
WHERE roles.name = 'USER'
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions
      WHERE role_permissions.role_id = roles.id
        AND role_permissions.permission_id = permissions.id
  );

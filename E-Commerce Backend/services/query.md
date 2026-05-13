CREATE SCHEMA IF NOT EXISTS user_schema;

SELECT r.*, p.*
FROM roles r
LEFT JOIN role_permissions rp
ON r.role_id = rp.role_id
LEFT JOIN permissions p
ON rp.permission_id = p.permission_id
WHERE r.role_name = 'CUSTOMER';



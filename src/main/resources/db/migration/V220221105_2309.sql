ALTER TABLE authority RENAME COLUMN authority_name TO name;
ALTER TABLE authority RENAME TO role;
ALTER TABLE user_authorities RENAME COLUMN authority_id TO role_id;
ALTER TABLE user_authorities RENAME TO user_roles;

UPDATE role SET name = 'ADMIN' WHERE name = 'ROLE_ADMIN';
UPDATE role SET name = 'USER' WHERE name = 'ROLE_USER';
UPDATE role SET name = 'SPECIALIST' WHERE name = 'ROLE_SPECIALIST';
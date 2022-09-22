INSERT INTO user_authorities(user_id, authority_id)
SELECT u.id, a.id FROM user_account u JOIN authority a ON a.id != 2
WHERE EXISTS (SELECT 1 FROM user_authorities ua WHERE ua.user_id = u.id AND ua.authority_id = 2)
  AND NOT EXISTS (SELECT 1 FROM user_authorities ua WHERE ua.authority_id = a.id AND ua.user_id = u.id)
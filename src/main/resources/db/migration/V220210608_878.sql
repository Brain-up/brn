DO $$
DECLARE
    r record;
    table_max_value bigint;
    sequence_cur_value bigint;
    sequence_name varchar;
    sequence_postfix varchar = '_id_seq';
BEGIN
    FOR r IN SELECT table_name FROM information_schema.tables
             WHERE table_name in ('signal', 'task', 'exercise', 'resource', 'sub_group', 'series', 'study_history')
        LOOP
            sequence_name = quote_ident(r.table_name || sequence_postfix);
            EXECUTE 'SELECT max(id) FROM ' || quote_ident(r.table_name) INTO table_max_value;
            EXECUTE 'SELECT last_value FROM ' || sequence_name  INTO sequence_cur_value;
            IF (sequence_cur_value <= table_max_value) THEN
                EXECUTE 'ALTER SEQUENCE ' || sequence_name || ' RESTART ' || table_max_value + 1 ;
            END IF;
        END LOOP;
END$$;

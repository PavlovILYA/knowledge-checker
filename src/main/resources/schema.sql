-- DROP table solving;
-- DROP table users_tests;
-- DROP table open_answers;
-- DROP table open_questions;
-- DROP table variable_questions_answers;
-- DROP table variable_questions;
-- DROP table variable_answers;
-- DROP table tests;
-- DROP table users;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNIQUE, -- from tg chat
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    username VARCHAR(255) UNIQUE,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS users_username
    ON users(username);

CREATE TABLE IF NOT EXISTS tests (
    id BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
    unique_title VARCHAR(500) UNIQUE,
    creator_id BIGINT,
    title VARCHAR(500),
    file_id BIGINT, -- from tg
    max_questions_number INT,
    test_type VARCHAR(255), -- enum
    PRIMARY KEY (unique_title),
    FOREIGN KEY (creator_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS variable_answers (
    id BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
    text VARCHAR(30) UNIQUE,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS variable_answer_text
    ON variable_answers(text);

CREATE TABLE IF NOT EXISTS variable_questions (
    id BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
    test_id BIGINT,
    text VARCHAR(5000),
    image_id BIGINT, -- from tg
    audio_id BIGINT, -- from tg
    file_id BIGINT,  -- from tg
    max_answer_number INT,
    correct_answer_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (test_id) REFERENCES tests(id),
    FOREIGN KEY (correct_answer_id) REFERENCES variable_answers(id)
);

CREATE TABLE IF NOT EXISTS variable_questions_answers (
    question_id BIGINT,
    answer_id BIGINT,
    PRIMARY KEY (question_id, answer_id),
    FOREIGN KEY (question_id) REFERENCES variable_questions(id),
    FOREIGN KEY (answer_id) REFERENCES variable_answers(id)
);

CREATE TABLE IF NOT EXISTS open_questions (
    id BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
    test_id BIGINT,
    text VARCHAR(5000),
    image_id BIGINT, -- from tg
    audio_id BIGINT, -- from tg
    file_id BIGINT,  -- from tg
    correct_answer VARCHAR(5000),
    PRIMARY KEY (id),
    FOREIGN KEY (test_id) REFERENCES tests(id)
);

CREATE TABLE IF NOT EXISTS open_answers (
    question_id BIGINT,
    user_id BIGINT,
    text VARCHAR(5000),
    image_id BIGINT, -- from tg
    audio_id BIGINT, -- from tg
    file_id BIGINT,  -- from tg
    PRIMARY KEY (question_id, user_id),
    FOREIGN KEY (question_id) REFERENCES open_questions(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS users_tests ( -- сохраненные тесты,
    user_id BIGINT,                      -- созданные другими
    test_id BIGINT,                      -- пользователями
    PRIMARY KEY (user_id, test_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (test_id) REFERENCES tests(id)
);

CREATE TABLE IF NOT EXISTS solving (
    id BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
    user_id BIGINT UNIQUE,           -- эту таблицу потом можно будет вынести в отдельную БД
    test_id BIGINT,                  -- и создать отдельный микросервис для работы со статистикой
    open_question_ids VARCHAR(1000), -- (который хранит информацию из других источников, например!)
    open_answer_ids VARCHAR(1000),
    variable_question_ids VARCHAR(1000),
    variable_answer_ids VARCHAR(1000),
    variable_answer_results VARCHAR(1000),
    started_at timestamp,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (test_id) REFERENCES tests(id)
);

CREATE INDEX IF NOT EXISTS solving_user_id
    ON solving(user_id);

-- CREATE OR REPLACE FUNCTION regexp_match(v_text in varchar, v_regexp in varchar) returns boolean
--     language plpgsql strict immutable
-- as
-- $$
-- begin
--     return v_text ~* v_regexp;
-- end
-- $$;

-- DROP FUNCTION regexp_match(varchar, varchar);

CREATE OR REPLACE FUNCTION regexp_match(varchar, varchar) RETURNS boolean
    AS 'select $1 ~* $2;'
    LANGUAGE SQL
    IMMUTABLE;
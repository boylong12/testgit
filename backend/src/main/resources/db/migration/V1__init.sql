CREATE TABLE admin_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE surveys (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    public_token VARCHAR(64) UNIQUE,
    allow_multiple BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    survey_id BIGINT NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL,
    title TEXT NOT NULL,
    required_field BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    label VARCHAR(512) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE TABLE responses (
    id BIGSERIAL PRIMARY KEY,
    survey_id BIGINT NOT NULL REFERENCES surveys(id) ON DELETE CASCADE,
    visitor_key VARCHAR(128) NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE answers (
    id BIGSERIAL PRIMARY KEY,
    response_id BIGINT NOT NULL REFERENCES responses(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id),
    value_json TEXT NOT NULL
);

CREATE INDEX idx_questions_survey ON questions(survey_id);
CREATE INDEX idx_responses_survey_visitor ON responses(survey_id, visitor_key);
CREATE INDEX idx_answers_response ON answers(response_id);

-- default admin: admin / admin123 (BCrypt)
INSERT INTO admin_users (username, password_hash) VALUES
('admin', '$2a$10$0UrpgJDataat1xR9GiuTweICE9cFbu3402jdKEoPeLQwor/wit.Jm');
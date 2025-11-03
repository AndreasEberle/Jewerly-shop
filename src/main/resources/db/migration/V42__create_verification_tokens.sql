-- Create verification tokens table for password reset and email confirmation
CREATE TABLE IF NOT EXISTS verification_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token       VARCHAR(255) UNIQUE NOT NULL,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_type  VARCHAR(50) NOT NULL, -- 'PASSWORD_RESET', 'EMAIL_CONFIRMATION'
    expires_at  TIMESTAMPTZ NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_verification_tokens_token ON verification_tokens(token);
CREATE INDEX idx_verification_tokens_user_id ON verification_tokens(user_id);
CREATE INDEX idx_verification_tokens_token_type ON verification_tokens(token_type);
CREATE INDEX idx_verification_tokens_expires_at ON verification_tokens(expires_at);


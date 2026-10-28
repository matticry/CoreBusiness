CREATE TABLE IF NOT EXISTS tbl_user_token (
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id                  BIGINT NOT NULL,
    access_token             TEXT NOT NULL,
    access_token_expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    refresh_token            TEXT NOT NULL,
    refresh_token_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at               TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_tbl_user_token_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tbl_user_token_user_id ON tbl_user_token(user_id);
CREATE INDEX IF NOT EXISTS idx_tbl_user_token_access_token ON tbl_user_token(access_token);
CREATE INDEX IF NOT EXISTS idx_tbl_user_token_refresh_token ON tbl_user_token(refresh_token);

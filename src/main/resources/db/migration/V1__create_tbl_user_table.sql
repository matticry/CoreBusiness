CREATE TABLE IF NOT EXISTS tbl_user (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    lastname            VARCHAR(100) NOT NULL,
    dni                 VARCHAR(20) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    password            VARCHAR(255) NOT NULL,
    status              BOOLEAN DEFAULT TRUE NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    password_changed_at TIMESTAMP WITH TIME ZONE,
    email_verified      BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tbl_user_id ON tbl_user(id);
CREATE INDEX IF NOT EXISTS idx_tbl_user_dni ON tbl_user(dni);
CREATE UNIQUE INDEX IF NOT EXISTS idx_tbl_user_email ON tbl_user(email);

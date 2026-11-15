CREATE TABLE job (
  id         UUID         PRIMARY KEY,
  title      VARCHAR(200) NOT NULL,
  status     VARCHAR(30)  NOT NULL DEFAULT 'NEW',
  created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
  -- optimistic lock / record version, also the basis for future offline sync
  version    BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_job_status ON job (status);

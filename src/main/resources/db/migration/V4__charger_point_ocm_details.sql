CREATE TABLE operator
(
    id                 BIGINT PRIMARY KEY,
    name               VARCHAR(255),
    website_url        VARCHAR(255),
    phone              VARCHAR(255),
    email              VARCHAR(255),
    fault_report_email VARCHAR(255)
);

CREATE TABLE data_provider
(
    id      BIGINT PRIMARY KEY,
    license TEXT
);

ALTER TABLE charger_point
    ADD COLUMN operator_id         BIGINT REFERENCES operator (id),
    ADD COLUMN data_provider_id    BIGINT REFERENCES data_provider (id),
    ADD COLUMN is_operational      BOOLEAN,
    ADD COLUMN usage_cost          TEXT,
    ADD COLUMN number_of_points    INTEGER,
    ADD COLUMN date_last_verified  TIMESTAMPTZ,
    ADD COLUMN date_last_confirmed TIMESTAMPTZ;

CREATE INDEX idx_charger_point_operator ON charger_point (operator_id);
CREATE INDEX idx_charger_point_data_provider ON charger_point (data_provider_id);

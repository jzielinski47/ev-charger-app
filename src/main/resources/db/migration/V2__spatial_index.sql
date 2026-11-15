CREATE INDEX IF NOT EXISTS idx_address_info_location
    ON address_info USING GIST (location);

ANALYZE address_info;

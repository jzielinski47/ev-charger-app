CREATE TABLE vehicle_inlets
(
    vehicle_id BIGINT       NOT NULL REFERENCES vehicle (id),
    inlet      VARCHAR(255) NOT NULL,
    PRIMARY KEY (vehicle_id, inlet)
);

INSERT INTO vehicle_inlets (vehicle_id, inlet)
SELECT DISTINCT vehicle_id,
       CASE connector_type
           WHEN 'TYPE_2_SOCKET_ONLY' THEN 'TYPE_2'
           WHEN 'TYPE_2_TETHERED' THEN 'TYPE_2'
           WHEN 'TESLA' THEN 'NACS'
           ELSE connector_type
       END
FROM vehicle_connector_types
WHERE connector_type IN ('TYPE_1', 'TYPE_2', 'TYPE_2_SOCKET_ONLY', 'TYPE_2_TETHERED',
                         'CCS2', 'CHADEMO', 'NACS', 'TESLA');

DROP TABLE vehicle_connector_types;

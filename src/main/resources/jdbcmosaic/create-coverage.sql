INSERT INTO jdbc_mosaic_master_table (coverage_name, tile_table_name, spatial_table_name)
VALUES ('tiled_coverage_1', 'jdbc_mosaic_tile_table_0', 'jdbc_mosaic_spatial_table_0');

CREATE TABLE jdbc_mosaic_spatial_table_0 (
  tile_id CHAR(64) NOT NULL,
  min_x    DOUBLE   NOT NULL,
  min_y    DOUBLE   NOT NULL,
  max_x    DOUBLE   NOT NULL,
  max_y    DOUBLE   NOT NULL,
  CONSTRAINT jdbc_mosaic_spatial_table_constraint PRIMARY KEY (tile_id)
);

CREATE TABLE jdbc_mosaic_tile_table_0 (
  tile_id  CHAR(64) NOT NULL,
  tile_data BLOB,
  CONSTRAINT jdbc_mosaic_tile_table_constraint PRIMARY KEY (tile_id)
);

CREATE INDEX jdbc_mosaic_spatial_index_0 ON jdbc_mosaic_spatial_table_0 (min_x, min_y);
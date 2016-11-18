CREATE TABLE jdbc_mosaic_master_table (
  coverage_name      CHARACTER(64) NOT NULL,
  spatial_table_name VARCHAR(128)  NOT NULL,
  tile_table_name    VARCHAR(128)  NOT NULL,
  res_x              DOUBLE,
  res_y              DOUBLE,
  min_x              DOUBLE,
  min_y              DOUBLE,
  max_x              DOUBLE,
  max_y              DOUBLE,
  CONSTRAINT jdbc_mosaic_master_table_pk PRIMARY KEY (coverage_name, spatial_table_name, tile_table_name)
);

INSERT INTO jdbc_mosaic_master_table(coverage_name,tile_table_name,spatial_table_name) VALUES ('tiled_coverage_1','tileTNPrefix_0','spatialTNPrefix_0');

 CREATE TABLE spatialTNPrefix_0 ( tile_id CHAR(64) NOT NULL,min_x DOUBLE NOT NULL,min_y DOUBLE NOT NULL,max_x DOUBLE NOT NULL,max_y DOUBLE NOT NULL,CONSTRAINT spatialTNPrefix_0_PK PRIMARY KEY(tile_id));
CREATE TABLE tileTNPrefix_0(tile_id CHAR(64) NOT NULL ,tile_data BLOB,CONSTRAINT tileTNPrefix_0_PK PRIMARY KEY(tile_id));


CREATE  INDEX IX_spatialTNPrefix_0 ON spatialTNPrefix_0(min_x,min_y);

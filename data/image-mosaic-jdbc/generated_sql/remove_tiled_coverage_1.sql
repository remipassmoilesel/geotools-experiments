
drop index IX_spatialTNPrefix_0;


drop table spatialTNPrefix_0;

drop table tileTNPrefix_0;

DELETE FROM jdbc_mosaic_master_table WHERE coverage_name = 'tiled_coverage_1' AND tile_table_name = 'tileTNPrefix_0' AND spatial_table_name = 'spatialTNPrefix_0'  ;

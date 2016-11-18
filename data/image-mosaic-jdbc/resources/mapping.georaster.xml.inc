	<!-- possible values: universal,postgis,db2,mysql,oracle,georaster -->
	<spatialExtension name="georaster"/>		
	<mapping>		
		<masterTable name="RASTER" >
		    <geoRasterAttribute name="IMAGE"/>
			<coverageNameAttribute name="NAME"/>
		</masterTable>
	</mapping>

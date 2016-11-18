	<connect>
		<!-- value DBCP or JNDI -->		
		<dstype value="DBCP"/>				
<!-- 		<jndiReferenceName value=""/>		 -->

		<username value="geotools" />
		<password value="geotools" />		
		
		<jdbcUrl value="jdbc:oracle:thin:@ux-mc01.ux-home.local:1521:geotools102" />
  		<driverClassName value="oracle.jdbc.OracleDriver"/> 
		<maxActive value="10"/>
		<maxIdle value="0"/>		
	</connect>

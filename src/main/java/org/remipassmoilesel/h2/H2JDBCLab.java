package org.remipassmoilesel.h2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.commons.io.FileUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.h2.tools.Server;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple trial on H2 databases
 */
public class H2JDBCLab {

    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    private static Path dbPath;
    private static Path rootDirectory;

    private static final String TYPE_NAME = "GeneratedPoint";

    public static void main(String[] args) throws IOException, SQLException {

        rootDirectory = Paths.get("data", "h2");
        dbPath = rootDirectory.resolve("database.h2");

        writeDatabase();
        readDatabase();
    }

    public static void readDatabase() throws IOException, SQLException {

        DataStore dataStore = DataStoreFinder.getDataStore(getEmbeddedParams(dbPath));

        // get store
        FeatureStore fsource = (FeatureStore) dataStore.getFeatureSource(TYPE_NAME);

        // show features
        FeatureIterator iterator = fsource.getFeatures().features();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

        Connection conn = DriverManager.getConnection("jdbc:h2:file:" + dbPath.toAbsolutePath());
        PreparedStatement stat = conn.prepareStatement("SHUTDOWN");
        stat.execute();

//        dataStore.dispose();
    }

    public static void writeDatabase() throws IOException, SQLException {

        // create rootDirectory directory

        FileUtils.deleteDirectory(rootDirectory.toFile());
        Files.createDirectories(rootDirectory);

        DataStore dataStore = DataStoreFinder.getDataStore(getEmbeddedParams(dbPath));

        System.out.println(dataStore);

        // create a feture type
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName(TYPE_NAME);
        b.add("name", String.class);
        b.add("number", Integer.class);
        b.setCRS(DefaultEngineeringCRS.CARTESIAN_2D);
        b.add("location", Geometry.class);

        SimpleFeatureType type = b.buildFeatureType();

        // create feature stoage
        dataStore.createSchema(type);

        // get store
        FeatureStore fsource = (FeatureStore) dataStore.getFeatureSource(type.getTypeName());

        // create features and add it
        DefaultFeatureCollection featColl = new DefaultFeatureCollection();
        SimpleFeatureBuilder featBuilder = new SimpleFeatureBuilder(type);
        for (int i = 0; i < 100; i++) {
            featBuilder.add("string_value_" + i);
            featBuilder.add(i);
            featBuilder.add(geometryFactory.createPoint(new Coordinate(i, i)));

            featColl.add(featBuilder.buildFeature(null));
        }
        fsource.addFeatures(featColl);

        Connection conn = DriverManager.getConnection("jdbc:h2:file:" + dbPath.toAbsolutePath());
        PreparedStatement stat = conn.prepareStatement("SHUTDOWN");
        stat.execute();

        dataStore.dispose();

        System.out.println("Finished !");
    }

    public static Map<String, Object> getServerParams(Path databaseFile) {

        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "h2");
        params.put("host", "localhost");
        params.put("port", 1000);
        params.put("database", "file:" + databaseFile.toAbsolutePath());

        return params;
    }

    public static Map<String, Object> getEmbeddedParams(Path databaseFile) {

        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "h2");
        params.put("database", "file:" + dbPath.toAbsolutePath());

        return params;
    }

}

package org.remipassmoilesel.h2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.apache.commons.io.FileUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.remipassmoilesel.utils.SimpleFeatureUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple trial on H2 databases
 */
public class H2JDBCLab {

    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    public static void main(String[] args) throws IOException {

        // create root directory
        Path root = Paths.get("data", "h2");
        FileUtils.deleteDirectory(root.toFile());
        Files.createDirectories(root);

        Path dbPath = root.resolve("./database.h2");

        // create datastore
        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "h2");
        params.put("database", "./" + dbPath.toString());

        DataStore dataStore = DataStoreFinder.getDataStore(params);

        System.out.println(dataStore);

        // create a feture type
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("GeneratedPoint");
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

        // show features
        FeatureIterator iterator = fsource.getFeatures().features();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }

        System.exit(0);
    }

}

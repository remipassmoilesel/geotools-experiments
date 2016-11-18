package org.remipassmoilesel.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.*;
import org.opengis.filter.identity.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.remipassmoilesel.geopackage.ShapeIdIssue.geomBuilder;

/**
 * Misc trials around filters
 */
public class FilterLab {

    public static void main(String[] args) throws IOException, CQLException {

        Path directory = Paths.get("data", "filterLab");
        Files.createDirectories(directory);

        Path db = directory.resolve("geopkg.db");
        String featureId = "feature1";

        // just after adding features to a geopackage, ids are invalid (featurename.null)
        //createFeatures(db, featureId);

        // but if we create a new feature iterator, ids will be valid
        displayFeatures(db, featureId);

        Map<String, String> params = new HashMap();
        params.put("dbtype", "geopkg");
        params.put("database", db.toString());

        JDBCDataStore datastore = (JDBCDataStore) DataStoreFinder.getDataStore(params);
        FeatureStore featurestore = (FeatureStore) datastore.getFeatureSource(featureId);

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

        // manual use of filter
        //System.out.println(filter.evaluate(feature));
        //System.out.println(filter.evaluate(bean));

        // simple match example
        displayResults(featurestore, ff.like(ff.property("style_id"), "style_value.130"));

        // "or" example
        PropertyIsLike filter1 = ff.like(ff.property("style_id"), "style_value.132");
        PropertyIsLike filter2 = ff.like(ff.property("style_id"), "style_value.142");
        Or or = ff.or(filter1, filter2);
        displayResults(featurestore, or);

        // "and" example
        PropertyIsLike filter3 = ff.like(ff.property("style_id"), "style_value.12*");
        PropertyIsLike filter4 = ff.like(ff.property("persistent_id"), "fid-3041*");
        And and = ff.and(filter3, filter4);
        displayResults(featurestore, and);

        // custom filter
        HashSet<Identifier> set = new HashSet<>();
        set.add(new FeatureIdImpl("fid--7dfce468_15861cd4acc_-7fc0"));
        Filter f = new PersistentIdFilter(set);

        displayResults(featurestore, f);


        datastore.dispose();
    }

    public static void displayResults(FeatureStore featurestore, Filter f) throws IOException {

        FeatureIterator it = featurestore.getFeatures(f).features();

        System.out.println();
        System.out.println("## Filter: " + f);
        while (it.hasNext()) {
            Feature feature = it.next();
            System.out.println(feature);
        }

        it.close();
    }


    public static void createFeatures(Path db, String featureId) throws IOException {

        // create a geopackage
        Files.deleteIfExists(db);
        Files.createFile(db);

        GeoPackage geopkg = new GeoPackage(db.toFile());

        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName(featureId);
        tbuilder.setCRS(DefaultGeographicCRS.WGS84);
        tbuilder.add("geometry", Geometry.class);
        tbuilder.add("persistent_id", String.class);
        tbuilder.add("style_id", String.class);


        SimpleFeatureType type = tbuilder.buildFeatureType();
        SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(type);

        FeatureEntry fe = new FeatureEntry();
        fe.setBounds(new ReferencedEnvelope());

        // get feature store from geopackage
        geopkg.create(fe, type);

        Map<String, String> params = new HashMap();
        params.put("dbtype", "geopkg");
        params.put("database", db.toString());

        JDBCDataStore datastore = (JDBCDataStore) DataStoreFinder.getDataStore(params);
        FeatureStore featurestore = (FeatureStore) datastore.getFeatureSource(featureId);

        // add points to datastore
        DefaultFeatureCollection features = new DefaultFeatureCollection();

        for (int i = 100; i < 150; i++) {

            fbuilder.add(geomBuilder.createPoint(new Coordinate(i, i)));
            fbuilder.add(SimpleFeatureBuilder.createDefaultFeatureId());
            fbuilder.add("style_value." + i);

            SimpleFeature feature = fbuilder.buildFeature(null);
            features.add(feature);
        }

        /**
         *
         * Just after adding features, normal identifiers are not availables.
         * But if we rebuild a feature iterator, it will be available
         *
         */

        System.out.println("Before adding: ");
        SimpleFeatureIterator it = features.features();
        while (it.hasNext()) {
            System.out.println(it.next().getID());
        }

        featurestore.addFeatures(features);

        System.out.println();
        System.out.println("After adding: ");
        it = features.features();
        while (it.hasNext()) {
            SimpleFeature feature = it.next();

            System.out.println();
            System.out.println(feature.getIdentifier());
            System.out.println(feature.getID());
            System.out.println(feature.getAttribute("style_id"));
            System.out.println(feature.getAttribute("second_id"));
        }

        it.close();

        datastore.dispose();
    }

    public static void displayFeatures(Path db, String featureId) throws IOException {

        Map<String, String> params = new HashMap();
        params.put("dbtype", "geopkg");
        params.put("database", db.toString());

        JDBCDataStore datastore = (JDBCDataStore) DataStoreFinder.getDataStore(params);
        FeatureStore featurestore = (FeatureStore) datastore.getFeatureSource(featureId);

        System.out.println();
        System.out.println("After adding: ");
        FeatureIterator it = featurestore.getFeatures().features();
        while (it.hasNext()) {
            SimpleFeature feature = (SimpleFeature) it.next();

            System.out.println();
            System.out.println(feature.getIdentifier());
            System.out.println(feature.getID());
            System.out.println(feature.getAttribute("style_id"));
            System.out.println(feature.getAttribute("second_id"));
        }

        it.close();

        datastore.dispose();
    }

}

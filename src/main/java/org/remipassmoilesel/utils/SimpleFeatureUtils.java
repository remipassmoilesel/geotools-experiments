package org.remipassmoilesel.utils;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.concurrent.locks.ReentrantLock;


/**
 * Simple utilities to create some default features with just a geometry
 */
public class SimpleFeatureUtils {

    private static org.geotools.feature.simple.SimpleFeatureBuilder fbuilder;

    static {
        // create a feature type
        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("geometries");
        tbuilder.setCRS(null);
        tbuilder.add("the_geom", Geometry.class);

        SimpleFeatureType type = tbuilder.buildFeatureType();

        // create a feature builder
        fbuilder = new org.geotools.feature.simple.SimpleFeatureBuilder(type);
    }

    public static String getFeatureId(String prefix) {
        return prefix + "_" + System.nanoTime();
    }

    public static SimpleFeature getLineFeature(Geometry geom) {
        return buildFeature(geom, "line");
    }

    public static SimpleFeature getPointFeature(Geometry geom) {
        return buildFeature(geom, "point");
    }

    private static synchronized SimpleFeature buildFeature(Geometry geom, String idPrefix) {

        fbuilder.add(geom);
        SimpleFeature feature = fbuilder.buildFeature(getFeatureId(idPrefix));

        return feature;
    }

}

package org.remipassmoilesel.draw;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by remipassmoilesel on 02/11/16.
 */
public class DefaultFeatureBuilder {

    private static ReentrantLock lock = new ReentrantLock();
    private static SimpleFeatureBuilder fbuilder;

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

    public static String getFeatureId(String prefix){
        return prefix + "_" + System.nanoTime();
    }

    public static SimpleFeature getLineFeature(Geometry geom) {

        lock.lock();

        fbuilder.add(geom);
        SimpleFeature feature = fbuilder.buildFeature(getFeatureId("line"));

        lock.unlock();

        return feature;
    }

}

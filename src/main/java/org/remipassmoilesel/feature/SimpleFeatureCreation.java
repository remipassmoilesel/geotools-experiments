package org.remipassmoilesel.feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.DataUtilities;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Simple example of feature creation with geometry
 */
public class SimpleFeatureCreation {

    public static void main(String[] args) {

        // create a feature type
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("SnappedPoints");
        builder.add("name", String.class);
        builder.add("number", Integer.class);

        // always set CRS before add geom
        builder.setCRS(null);
        builder.add("location", Point.class);

        //Multiple geometries with multiple CRS can be added
        //builder.setCRS(null);
        //builder.add("location", Point.class);

        SimpleFeatureType type = builder.buildFeatureType();

        // create a feature builder
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);

        // create a geometry builder
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();


        featureBuilder.add("Name of feature");
        featureBuilder.add(1);
        featureBuilder.add(geometryFactory.createPoint(new Coordinate(20,30)));

        SimpleFeature feature = featureBuilder.buildFeature("string_id");

    }
}

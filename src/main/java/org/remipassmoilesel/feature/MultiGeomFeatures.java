package org.remipassmoilesel.feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.remipassmoilesel.utils.GuiUtils;

import java.awt.*;
import java.util.ArrayList;

/**
 * Test of feature sources, trying to add several geometrical shapes
 */
public class MultiGeomFeatures {

    public static void main(String[] args) throws FactoryException {

        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");

        DefaultFeatureCollection geomCollection = new DefaultFeatureCollection();

        // create a feature type
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("geometries");
        builder.setCRS(crs);
        builder.add("the_geom", Geometry.class);

        SimpleFeatureType type = builder.buildFeatureType();

        // create a feature builder
        SimpleFeatureBuilder featb = new SimpleFeatureBuilder(type);

        // create a geometry builder
        GeometryFactory geomf = JTSFactoryFinder.getGeometryFactory();

        featb.add(geomf.createPolygon(new Coordinate[]{
                new Coordinate(47.97d, 5.56),
                new Coordinate(47.11d, 5.62),
                new Coordinate(44.53d, 1.49),
                new Coordinate(47.97d, 5.56)
        }));

        featb.add(geomf.createPoint(new Coordinate(45.91d, 4.28)));
        featb.add(geomf.createPoint(new Coordinate(44.63d, 3.08)));

        geomCollection.add(featb.buildFeature("first"));


        // create a map
        MapContent map = new MapContent();

        Style polygonStyle = SLD.createPolygonStyle(Color.blue, Color.yellow, 1.0f);
        FeatureLayer pointLayer = new FeatureLayer(geomCollection, polygonStyle);
        map.addLayer(pointLayer);


        GuiUtils.showInWindow(map);


    }

}

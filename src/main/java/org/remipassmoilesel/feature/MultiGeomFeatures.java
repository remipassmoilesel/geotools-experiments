package org.remipassmoilesel.feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.styling.*;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.remipassmoilesel.utils.GuiUtils;

import java.awt.*;
import java.io.IOException;

/**
 * Trying to add several geometrical shapes to a single feature source
 * <p>
 * As expected, that is not possible to write generic geometries in a shapefile.
 */
public class MultiGeomFeatures {

    private static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

    public static void main(String[] args) throws FactoryException, IOException {

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

        // create a polygon
        featb.add(geomf.createPolygon(new Coordinate[]{
                new Coordinate(47.97d, 5.56),
                new Coordinate(47.11d, 5.62),
                new Coordinate(44.53d, 1.49),
                new Coordinate(47.97d, 5.56)
        }));
        geomCollection.add(featb.buildFeature("first"));

        // create polygon styles
        org.geotools.styling.Stroke polystroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLUE),
                filterFactory.literal(1),
                filterFactory.literal(1));

        Fill polyfill = styleFactory.createFill(
                filterFactory.literal(Color.CYAN),
                filterFactory.literal(0.5));

        org.geotools.styling.PolygonSymbolizer polysym = styleFactory.createPolygonSymbolizer(polystroke, polyfill, null);

        // create some points
        featb.add(geomf.createPoint(new Coordinate(45.91d, 4.28)));
        geomCollection.add(featb.buildFeature("second"));

        featb.add(geomf.createPoint(new Coordinate(44.63d, 3.08)));
        geomCollection.add(featb.buildFeature("third"));

        // create point styles
        Graphic pointGraphic = styleFactory.createDefaultGraphic();

        Mark mark = styleFactory.getCircleMark();

        mark.setStroke(styleFactory.createStroke(
                filterFactory.literal(Color.BLUE), filterFactory.literal(1)));

        mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));

        pointGraphic.graphicalSymbols().clear();
        pointGraphic.graphicalSymbols().add(mark);
        pointGraphic.setSize(filterFactory.literal(5));

        PointSymbolizer pointsym = styleFactory.createPointSymbolizer(pointGraphic, null);

        // create a map and show all
        MapContent map = new MapContent();

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(polysym);
        rule.symbolizers().add(pointsym);

        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        FeatureLayer geomLayer = new FeatureLayer(geomCollection, style);
        map.addLayer(geomLayer);

        GuiUtils.showInWindow(map);

        // write features, doesn't work with generic geometry class
        // Path dest = Paths.get("data/multigeom/multigeom.shp");
        // ShapeFileUtils.write(type, geomCollection, dest);

    }

}

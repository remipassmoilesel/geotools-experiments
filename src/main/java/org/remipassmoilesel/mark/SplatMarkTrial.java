package org.remipassmoilesel.mark;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.styling.*;
import org.opengis.filter.FilterFactory2;
import org.remipassmoilesel.utils.MiscUtils;
import org.remipassmoilesel.utils.SimpleFeatureUtils;
import org.remipassmoilesel.utils.GuiUtils;

import java.util.PrimitiveIterator;
import java.util.Random;

/**
 * Created by remipassmoilesel on 03/11/16.
 */
public class SplatMarkTrial {

    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    public static void main(String[] args) {

        // create a point style
        StyleBuilder builder = new StyleBuilder();

        Graphic splat = builder.createGraphic(null, builder.createMark("splat"), null);
        PointSymbolizer symbolizer = builder.createPointSymbolizer(splat);

        // builder will fill in all the other classes with defaults
        Style style = builder.createStyle(symbolizer);

        // create a map content and a layer
        MapContent mapContent = new MapContent();

        DefaultFeatureCollection shapes = new DefaultFeatureCollection();
        FeatureLayer layer = new FeatureLayer(shapes, style);

        PrimitiveIterator.OfDouble rand = new Random().doubles(1,100).iterator();
        for (int i = 0; i < 100; i++) {
            double x = rand.nextDouble();
            double y = rand.nextDouble();

            shapes.add(SimpleFeatureUtils.getPointFeature(geometryFactory.createPoint(new Coordinate(x,y))));
        }

        mapContent.addLayer(layer);

        // show all in window
        GuiUtils.showInWindow(mapContent);

    }

}

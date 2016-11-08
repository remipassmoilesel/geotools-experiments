package org.remipassmoilesel.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.remipassmoilesel.utils.SimpleFeatureUtils;

import java.util.PrimitiveIterator;
import java.util.Random;

import static org.remipassmoilesel.mark.CustomMarksTrial.createCustomImageSymbolizer;

/**
 * Created by remipassmoilesel on 04/11/16.
 */
public class StyleSerialization {

    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    private static final StyleBuilder builder = new StyleBuilder();

    public static void main(String[] args) {

        MapContent content = createContent();

        Layer layer = content.layers().get(0);
        System.out.println(layer.getStyle().toString());

        Style style = layer.getStyle();

//        builder.

        SLDTransformer transformer = new SLDTransformer();
//        String xml = transformer.transform( style.);

    }

    public static MapContent createContent(){
        // create style for point
        // Style style = builder.createStyle(createShapeSymbolizer());
        // Style style = builder.createStyle(createDefaultImageSymbolizer());
        Style style = builder.createStyle(createCustomImageSymbolizer());

        // create a map content and a layer
        MapContent mapContent = new MapContent();

        DefaultFeatureCollection shapes = new DefaultFeatureCollection();
        FeatureLayer layer = new FeatureLayer(shapes, style);

        PrimitiveIterator.OfDouble rand = new Random().doubles(1, 100).iterator();
        for (int i = 0; i < 100; i++) {
            double x = rand.nextDouble();
            double y = rand.nextDouble();

            shapes.add(SimpleFeatureUtils.getPointFeature(geometryFactory.createPoint(new Coordinate(x, y))));
        }

        mapContent.addLayer(layer);

        return mapContent;
    }

}

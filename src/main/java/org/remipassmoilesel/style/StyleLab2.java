package org.remipassmoilesel.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.remipassmoilesel.utils.GuiUtils;

import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.Random;

import static org.hsqldb.lib.tar.TarHeaderField.size;

/**
 * Created by remipassmoilesel on 14/11/16.
 */
public class StyleLab2 {

    private final static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private final static GeometryFactory geom = JTSFactoryFinder.getGeometryFactory();
    private final static FilterFactory ff = CommonFactoryFinder.getFilterFactory();
    private static final String STYLE_ID = "style_id";
    private static final byte POINT_SIZE = 10;
    private static final String GEOMETRY = "geometry";
    private static SimpleFeatureBuilder builder;

    public static void main(String[] args) throws IOException, TransformerException {

        // build a style
        Style style = sf.createStyle();
        style.setName("Layer style");
        style.getDescription().setTitle("Layer style title");
        style.getDescription().setAbstract("Whaaoo beautiful style :)");

        // build a layer
        DefaultFeatureCollection coll = new DefaultFeatureCollection();
        FeatureLayer layer = new FeatureLayer(coll, style);

        // generate 3 styles with 3 ids
        ArrayList<String> styles = new ArrayList<>();
        styles.add(generateStyleId());
        styles.add(generateStyleId());
        styles.add(generateStyleId());

        FeatureTypeStyle featureTypeStyle = sf.createFeatureTypeStyle();
        featureTypeStyle.rules().add(buildStyle(Color.blue, styles.get(0)));
        featureTypeStyle.rules().add(buildStyle(Color.red, styles.get(1)));
        featureTypeStyle.rules().add(buildStyle(Color.green, styles.get(2)));

        style.featureTypeStyles().add(featureTypeStyle);

        // add random points
        PrimitiveIterator.OfDouble randPosition = new Random().doubles(0, 500).iterator();
        PrimitiveIterator.OfInt randColor = new Random().ints(0, 3).iterator();
        for (int i = 0; i < 100; i++) {
            double x = randPosition.nextDouble();
            double y = randPosition.nextDouble();

            String sid = styles.get(randColor.next());

            SimpleFeature feat = buildPoint(new Coordinate(x, y), sid);
            coll.add(feat);
        }

        Iterator<FeatureTypeStyle> it = style.featureTypeStyles().iterator();
        while (it.hasNext()) {
            FeatureTypeStyle s = it.next();
            System.out.println(s.getClass() + " " + s);
            for (Rule rule : s.rules()) {
                System.out.println(rule.getClass() + " " + rule);
            }
        }

        serializeStyle(style, Paths.get("data/serializedStyle.xml"));

        Style style2 = unserializeStyle(Paths.get("data/serializedStyle.xml"));

        GuiUtils.showInWindow(layer);

    }

    private static void serializeStyle(Style style, Path destination) throws IOException, TransformerException {

        BufferedWriter writer = Files.newBufferedWriter(destination);

        try {
            SLDTransformer styleTransform = new SLDTransformer();
            styleTransform.setEncoding(Charset.forName("UTF-8"));
            styleTransform.setIndentation(2);

            String xml = styleTransform.transform(style);
            writer.write(xml);
        } finally {
            writer.close();
        }
    }

    private static Style unserializeStyle(Path destination) throws IOException, TransformerException {

        BufferedReader reader = Files.newBufferedReader(destination);

        SLDParser parser = new SLDParser(sf, reader);

        Style[] styles = parser.readXML();

        for (Style style : styles) {
            System.out.println(style);
        }

        return styles[0];


    }

    public static Rule buildStyle(Color color, String styleid) {

        Stroke stroke = sf.stroke(ff.literal(color), null, null, null, null, null, null);
        Fill fill = sf.fill(null, ff.literal(color), ff.literal(1.0));

        Mark mark = sf.getCircleMark();
        mark.setFill(fill);
        mark.setStroke(stroke);

        Graphic graphic = sf.createDefaultGraphic();
        graphic.graphicalSymbols().clear();
        graphic.graphicalSymbols().add(mark);
        graphic.setSize(ff.literal(POINT_SIZE));

        // here we have to specify the name of the geometry field. Set to null allow to not specify it
        PointSymbolizer symbolizer = sf.createPointSymbolizer(graphic, null);

        Rule r = sf.createRule();
        r.symbolizers().add(symbolizer);
        Filter filter = ff.equal(ff.property(STYLE_ID), ff.literal(styleid), true);
        r.setFilter(filter);

        return r;
    }

    public static void applyStyle(SimpleFeature feat, String styleId) {
        feat.setAttribute(STYLE_ID, styleId);
    }

    public static String generateStyleId() {
        return "style_" + System.nanoTime();
    }

    public static final SimpleFeature buildPoint(Coordinate coord, String styleid) {

        if (builder == null) {
            SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
            tbuilder.setName("DefaultFeature");
            tbuilder.setCRS(null);
            tbuilder.add(GEOMETRY, Geometry.class);
            tbuilder.add(STYLE_ID, String.class);
            builder = new SimpleFeatureBuilder(tbuilder.buildFeatureType());
        }

        builder.add(geom.createPoint(coord));
        builder.add(styleid);
        return builder.buildFeature(null);
    }

}

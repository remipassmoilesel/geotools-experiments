package org.remipassmoilesel.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.remipassmoilesel.utils.GuiUtils;

import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.Random;

/**
 * Created by remipassmoilesel on 03/01/17.
 */
public class StyleLab3 {

    private static final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private static final GeometryFactory geom = JTSFactoryFinder.getGeometryFactory();

    public static void main(String[] args) throws TransformerException {

        // serialize style to xml
        // styleToSld();

        // main style
        // Style style = createDefaultStyle();
        // Style style = createSimpleStyleFromSLD();
        Style style = createStyleFromSLD2();
        // Style style = specialRoundGraphicStrokes();

        // create a feature layer
        DefaultFeatureCollection coll = new DefaultFeatureCollection();
        FeatureLayer layer = new FeatureLayer(coll, style);

        // create feature type
        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("feature1");
        tbuilder.setCRS(DefaultGeographicCRS.WGS84);
        tbuilder.add("geometry", Geometry.class);
        tbuilder.add("name", String.class);

        SimpleFeatureType type = tbuilder.buildFeatureType();
        SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(type);

        // populate with random features
        int featureNumber = 10;
        ReferencedEnvelope bounds = new ReferencedEnvelope(-20, 20, -30, 30, DefaultGeographicCRS.WGS84);
        PrimitiveIterator.OfDouble rand = new Random().doubles(bounds.getMinX(), bounds.getMaxX()).iterator();
        for (int i = 0; i < featureNumber; i++) {

            ArrayList<Coordinate> points = new ArrayList();
            for (int j = 0; j < 5; j++) {
                points.add(new Coordinate(rand.next(), rand.next()));
            }

            fbuilder.add(geom.createLineString(points.toArray(new Coordinate[points.size()])));
            fbuilder.add("Feature " + i);
            coll.add(fbuilder.buildFeature(null));
        }

        GuiUtils.showInWindow(layer);

    }

    private static Style specialRoundGraphicStrokes() {

        // See SLDParser.class for details about special styles

        // create graphic mark
        Stroke blueStroke = sf.stroke(ff.literal(Color.blue), null, ff.literal(2), null, null, null, null);
        Fill redFill = sf.fill(null, ff.literal(Color.red), ff.literal(1.0));

        Mark mark = sf.getCircleMark();
        mark.setFill(redFill);
        mark.setStroke(blueStroke);

        Graphic graphic = sf.createDefaultGraphic();
        graphic.graphicalSymbols().clear();
        graphic.graphicalSymbols().add(mark);
        graphic.setSize(ff.literal(15));
        graphic.setOpacity(ff.literal(0.5));

        Stroke roundStroke = sf.getDefaultStroke();
        roundStroke.setGraphicStroke(graphic);

        // this instruction set spaces of rounds
        roundStroke.setDashArray(Arrays.asList(ff.literal(15), ff.literal(10)));

        // and line symbolizer to draw line
        LineSymbolizer lineSym = sf.createLineSymbolizer(blueStroke, null);

        // create a line symbolizer to draw rounds
        LineSymbolizer roundedLineSym = sf.createLineSymbolizer(roundStroke, null);

        // create rule
        Rule r = sf.createRule();
        r.symbolizers().add(roundedLineSym);
        r.symbolizers().add(lineSym);

        // add it to style
        Style style = sf.createStyle();
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(r);
        style.featureTypeStyles().add(fts);

        return style;
    }

    /**
     * Unserialize SLD stylesheet
     *
     * @return
     */
    private static Style createStyleFromSLD2() {

        //String path = "/styleSld/style.sld";
        //String path = "/styleSld/style_hatching.sld";
        //String path = "/styleSld/style_label.sld";
        String path = "/styleSld/style_vertices.sld";
        InputStream stream = StyleLab3.class.getResourceAsStream(path);

        SLDParser stylereader = new SLDParser(sf, stream);
        StyledLayerDescriptor sld = stylereader.parseSLD();

        NamedLayer layer = (NamedLayer) sld.layers().get(0);

        return layer.styles().get(0);
    }

    private static Style createSimpleStyleFromSLD() {
        Style style = SLD.createLineStyle(Color.darkGray, 3);
        return style;
    }

    /**
     * Serialize style to SLD stylesheet
     *
     * @throws TransformerException
     */
    private static void styleToSld() throws TransformerException {
        Style style = createSimpleStyleFromSLD();
        StyledLayerDescriptor sld = sf.createStyledLayerDescriptor();
        UserLayer ulayer = sf.createUserLayer();
        ulayer.setLayerFeatureConstraints(new FeatureTypeConstraint[]{null});
        sld.addStyledLayer(ulayer);
        ulayer.addUserStyle(style);

        SLDTransformer styleTransform = new SLDTransformer();
        String xml = styleTransform.transform(sld);

        System.out.println(xml);
    }


    /**
     * This kind of feature type style is not a good idea, because lines are not drawn properly
     *
     * @return
     */
    private static Style createDefaultStyle2() {

        Color foreground = Color.darkGray;
        int thick = 3;

        // create stroke
        org.geotools.styling.Stroke stroke = sf.stroke(ff.literal(foreground), null, ff.literal(thick), null, null, null, null);

        // create line symbolizer
        LineSymbolizer lineSym = sf.createLineSymbolizer(stroke, null);

        // create rule
        Rule r = sf.createRule();
        r.symbolizers().add(lineSym);

        // add it to style
        Style style = sf.createStyle();
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(r);
        style.featureTypeStyles().add(fts);

        return style;
    }

    /**
     * This kind of feature type style is not a good idea, because lines are not drawn properly
     *
     * @return
     */
    private static Style createDefaultStyle() {

        Color background = null;
        Color foreground = Color.black;
        int thick = 5;

        // create stroke and optional fill
        org.geotools.styling.Stroke stroke = sf.stroke(ff.literal(foreground), null, null, null, null, null, null);
        Fill fill = null;
        if (background != null) {
            fill = sf.fill(null, ff.literal(background), ff.literal(1.0));
        }

        // create point symbolizer
        Mark mark = sf.getCircleMark();
        mark.setStroke(stroke);
        mark.setFill(fill);

        Graphic graphic = sf.createDefaultGraphic();
        graphic.graphicalSymbols().clear();
        graphic.graphicalSymbols().add(mark);
        graphic.setSize(ff.literal(thick));

        // here we can specify name of geometry field. Set to null allow to not specify it
        PointSymbolizer pointSym = sf.createPointSymbolizer(graphic, null);

        // create line symbolizer
        LineSymbolizer lineSym = sf.createLineSymbolizer(stroke, null);

        // create polygon symbolizer
        PolygonSymbolizer polygonSym = sf.createPolygonSymbolizer(stroke, fill, null);

        // create rule
        Rule r = sf.createRule();
        r.symbolizers().add(pointSym);
        r.symbolizers().add(lineSym);
        r.symbolizers().add(polygonSym);

        // add it to style
        Style style = sf.createStyle();
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(r);
        style.featureTypeStyles().add(fts);

        return style;
    }

}

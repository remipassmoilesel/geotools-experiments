package org.remipassmoilesel.draw;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.opengis.filter.FilterFactory;

import java.awt.*;

/**
 * Helper to build simple styles
 */
public class SimpleStyleBuilder {

    private final static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    private final static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

    public static Style createLineStyle(Color color, float width) {

        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(color), filterFactory.literal(width));

        LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    public static Style createPointStyle(Color color, int width) {

        Graphic gr = styleFactory.createDefaultGraphic();

        Mark mark = styleFactory.getCircleMark();

        mark.setStroke(styleFactory.createStroke(
                filterFactory.literal(color), filterFactory.literal(1)));

        mark.setFill(styleFactory.createFill(filterFactory.literal(color)));

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add(mark);
        gr.setSize(filterFactory.literal(width));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }
}

package org.remipassmoilesel.style;

import org.geotools.styling.*;

import java.awt.*;

import static org.remipassmoilesel.style.StyleLab.filterFactory;
import static org.remipassmoilesel.style.StyleLab.styleFactory;

/**
 * Created by remipassmoilesel on 01/11/16.
 */
public class StyleMemoLab {

    public static void main(String[] args) {

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

        // create polygon styles
        org.geotools.styling.Stroke polystroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLUE),
                filterFactory.literal(1),
                filterFactory.literal(1));

        Fill polyfill = styleFactory.createFill(
                filterFactory.literal(Color.CYAN),
                filterFactory.literal(0.5));

        org.geotools.styling.PolygonSymbolizer polysym = styleFactory.createPolygonSymbolizer(polystroke, polyfill, null);

        // wrap symbolizers into rule and rule into style
        // many rules can be used to change display according to scale, ...
        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(polysym);
        rule.symbolizers().add(pointsym);

        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);


        // wrap several symbolizers in one SLD sheet
        SLD.wrapSymbolizers();

    }

}

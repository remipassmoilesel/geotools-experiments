package org.remipassmoilesel.mark;

import org.geotools.renderer.style.MarkFactory;


import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;

/**
 * Create a shape icon
 */
public class ShapeMarkFactory implements MarkFactory {

    private static GeneralPath SHAPE;

    static {
        SHAPE = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        SHAPE.moveTo(-0.2f, 0.9f);
        SHAPE.lineTo(0.266f, -0.5f);
        SHAPE.lineTo(-0.366f, -0.7f);
        SHAPE.lineTo(0.4f, 1.12f);
        SHAPE.lineTo(0.3f, 1.10f);
    }

    public Shape getShape(Graphics2D graphics, Expression symbolUrl, Feature feature) throws Exception {
        if (symbolUrl == null) {
            // cannot handle a null url
            return null;
        }
        // Evaluate the expression as a String
        String wellKnownName = symbolUrl.evaluate(feature, String.class);

        if (wellKnownName != null && wellKnownName.equalsIgnoreCase("splat")) {
            return SHAPE;
        }

        return null; // we do not know this one
    }
}



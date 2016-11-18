package org.remipassmoilesel.mark;

import org.geotools.renderer.style.ExternalGraphicFactory;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom image factory implementation.
 * <p>
 * Should be usefull to provide icons without use absolute paths, in order to share projects without
 * providing icons.
 * <p>
 * But also break interoperability.
 * <p>
 * Here we are using a custom mime type to detect custom icons, and a custom URL prefix because
 * if we don't provide a URL at icon creation, errors prevent creation.
 * <p>
 * Referenced in resources/meta-inf/services
 */
public class CustomIconFactory implements ExternalGraphicFactory {

    private static final Logger LOGGER = Logging.getLogger(CustomIconFactory.class);

    private static final String PROTOCOL_ID = "file://customicon/";
    private static final String CUSTOM_TYPE = "image/customicon";

    @Override
    public Icon getIcon(Feature feature, Expression symbolUrl, String format, int size) throws Exception {

        if (symbolUrl == null || format.trim().indexOf(CUSTOM_TYPE) != 0) {
            // cannot handle a null url
            return null;
        }

        // Evaluate the expression as a String
        String wellKnownName = symbolUrl.evaluate(feature, String.class);

        if (wellKnownName != null) {
            wellKnownName = wellKnownName.trim();
            int protInd = wellKnownName.indexOf(PROTOCOL_ID);

            // expression is a custom icon
            if (protInd == 0) {
                String path = Paths.get(".", wellKnownName.substring(PROTOCOL_ID.length(), wellKnownName.length())).toAbsolutePath().toString();
                System.out.println("CustomIconFactory: Returning custom icon: " + path);
                return new ImageIcon(path);
            } else {
                LOGGER.log(Level.WARNING, "CustomIconFactory: Malformed icon name: " + wellKnownName);
                return null;
            }
        }

        return null; // we do not know this one
    }
}



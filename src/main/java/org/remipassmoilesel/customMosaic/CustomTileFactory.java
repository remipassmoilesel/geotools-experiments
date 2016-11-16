package org.remipassmoilesel.customMosaic;

import org.geotools.renderer.style.ExternalGraphicFactory;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomTileFactory implements ExternalGraphicFactory {

    private static final Logger LOGGER = Logging.getLogger(CustomTileFactory.class);

    private static final String PROTOCOL_ID = "file://tile/";
    private static final String CUSTOM_TYPE = "image/tile";

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
                String tileId = wellKnownName.substring(PROTOCOL_ID.length(), wellKnownName.length());
                Path path = CustomMosaic.ROOT_FOLDER.resolve(tileId);

                System.out.println("CustomTileFactory: Returning custom icon: " + path);

                return new ImageIcon(path.toString());
            } else {
                LOGGER.log(Level.WARNING, "CustomIconFactory: Malformed icon name: " + wellKnownName);
                return null;
            }
        }

        return null; // we do not know this one
    }
}



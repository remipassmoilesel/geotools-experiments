package org.remipassmoilesel.swing;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Parameter;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.*;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JParameterListWizard;
import org.geotools.swing.wizard.JWizard;
import org.geotools.util.KVP;
import org.remipassmoilesel.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by remipassmoilesel on 26/10/16.
 */
public class SwingLab {

    public static void main(String[] args) throws IOException {

        File shpFile = new File("data/france-communes/communes-20160119.shp");

        // Connect to the shapefile
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shpFile);
        SimpleFeatureSource shapefileSource = dataStore
                .getFeatureSource();

        // Create a basic style with yellow lines and no fill
        Style shpStyle = SLD.createPolygonStyle(Color.YELLOW, null, 0.0f);

        // Set up a MapContent with the two layers
        final MapContent map = new MapContent();
        map.setTitle("French communes");

        Layer shpLayer = new FeatureLayer(shapefileSource, shpStyle);
        map.addLayer(shpLayer);

//        GuiUtils.showInWindow(map);
        GuiUtils.showInCustomWindow(map);
    }

}

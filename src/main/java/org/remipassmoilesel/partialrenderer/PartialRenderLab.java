package org.remipassmoilesel.partialrenderer;

import com.j256.ormlite.field.DataPersisterManager;
import org.apache.commons.io.FileUtils;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wms.WMSUtils;
import org.geotools.data.wms.WebMapServer;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.CRS;
import org.geotools.styling.SLD;
import org.opengis.referencing.FactoryException;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Timer;

/**
 * Created by remipassmoilesel on 04/12/16.
 */
public class PartialRenderLab {

    public static final Path CACHE_DATABASE_DIR = Paths.get("data/renderedPartialsStore/");
    private static boolean setupWms = false;
    private static boolean setupShape = true;
    private static boolean showStats = true;

    public static void main(String[] args) throws IOException, ServiceException, FactoryException {

        FileUtils.deleteDirectory(CACHE_DATABASE_DIR.toFile());

        DataPersisterManager.registerDataPersisters(BufferedImagePersister.getSingleton());

        String shapePath = "data/france-communes/communes-20160119.shp";
        String wmsUrl = "http://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities";
        int wmsLayerIndex = 0;

        MapContent mapContent = new MapContent();

        if (setupWms == true) {

            // retrieve and add wms layer
            WebMapServer wms = new WebMapServer(new URL(wmsUrl));

            WMSCapabilities capabilities = wms.getCapabilities();
            Layer[] namedLayers = WMSUtils.getNamedLayers(capabilities);

            WMSLayer wmsLayer = new WMSLayer(wms, namedLayers[wmsLayerIndex]);

            mapContent.addLayer(wmsLayer);

        }

        if (setupShape == true) {

            // retrieve a shape file and add it to a mapcontent
            Path shape = Paths.get(shapePath);

            FileDataStore dataStore = FileDataStoreFinder.getDataStore(shape.toFile());
            SimpleFeatureSource shapeFileSource = dataStore
                    .getFeatureSource();

            FeatureLayer shapeLayer = new FeatureLayer(shapeFileSource, SLD.createLineStyle(Color.blue, 0.2f));

            mapContent.addLayer(shapeLayer);
        }

        //ReferencedEnvelope start = new ReferencedEnvelope(-2.38d, 0.44d, 48.45d, 49.98d, DefaultGeographicCRS.WGS84);
        //ReferencedEnvelope start = new ReferencedEnvelope(2.38d, 5.44d, 48.45d, 49.98d, DefaultGeographicCRS.WGS84);
        Point2D.Double start = new Point2D.Double(-2.38d, 48.45d);

        //RenderedPartialFactory partMan = new RenderedPartialFactory(mapContent);
        //partMan.intersect(start);

        SwingUtilities.invokeLater(() -> {

            CacheMapPane pane = new CacheMapPane(mapContent);

            //pane.setWorldBounds(start);
            pane.setWorldPosition(start);

            JFrame frame = new JFrame();
            frame.setContentPane(pane);
            frame.setSize(new Dimension(800, 600));
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            frame.setVisible(true);

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // listen map move
            CacheMapPaneMouseController mcontrol = new CacheMapPaneMouseController(pane);
            pane.addMouseMotionListener(mcontrol);
            pane.addMouseListener(mcontrol);
            pane.addMouseWheelListener(mcontrol);

        });

        if (showStats) {

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Rendered / in memory used / in database / in db used: "
                            + RenderedPartialFactory.getRenderedPartials()
                            + " / " + RenderedPartialStore.getInMemoryUsedPartials()
                            + " / " + RenderedPartialStore.getAddedInDatabase()
                            + " / " + RenderedPartialStore.getInDatabaseUsedPartials()
                    );
                }
            }, 1000, 1000);

        }

    }

}

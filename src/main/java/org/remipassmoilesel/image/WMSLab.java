package org.remipassmoilesel.image;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.StyleImpl;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WMSUtils;
import org.geotools.data.wms.WebMapServer;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.wms.WMSChooser;
import org.geotools.swing.wms.WMSLayerChooser;

/**
 * This is a Web Map Server "quickstart" doing the minimum required to display
 * something on screen.
 * <p>
 * Free WMS service:
 * http://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities
 */
public class WMSLab extends JFrame {

    public static void main(String[] args) throws Exception {
        showInformationsAbout("http://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities");
    }

    private static void showInformationsAbout(String url) throws IOException, ServiceException {

        WebMapServer wms = new WebMapServer(new URL(url));

        WMSCapabilities capabilities = wms.getCapabilities();
        System.out.println("capabilities");
        System.out.println(capabilities);

        // gets the top most layer, which will contain all the others
        Layer rootLayer = capabilities.getLayer();
        System.out.println("rootLayer");
        System.out.println(rootLayer);

        // gets all the layers in a flat list, in the order they appear in
        // the capabilities document (so the rootLayer is at index 0)
        List<Layer> layers = capabilities.getLayerList();
        System.out.println("List<Layer> layers = capabilities.getLayerList();");
        for (Layer layer : layers) {
            System.out.println(layer);
        }

        // All wms layers are not usable. This utils allow to retrieve only usable layers
        Layer[] namedLayers = WMSUtils.getNamedLayers(capabilities);
        System.out.println("Layer[] namedLayers = WMSUtils.getNamedLayers(capabilities)");
        int j = 0;
        for (Layer layer : namedLayers) {
            System.out.println(j + ": " + layer);
            j++;
        }

        int i = 0;
        for (Layer layer : namedLayers) {

            // Print layer info
            System.out.println("Layer: (" + i + ")" + layer.getName());
            System.out.println("       " + layer.getTitle());
            System.out.println("       " + layer.getChildren().length);
            System.out.println("       " + layer.getBoundingBoxes());
            CRSEnvelope env = layer.getLatLonBoundingBox();
            System.out.println("       " + env.getLowerCorner() + " x " + env.getUpperCorner());

            // Get layer styles
            List styles = layer.getStyles();
            for (Iterator it = styles.iterator(); it.hasNext(); ) {
                StyleImpl elem = (StyleImpl) it.next();

                // Print style info
                System.out.println("Style:");
                System.out.println("  Name:" + elem.getName());
                System.out.println("  Title:" + elem.getTitle());
            }

            i++;
        }

    }

    /**
     * Prompts the user for a wms service, connects, and asks for a layer and then
     * and displays its contents on the screen in a map frame.
     */
    private static void promptAndShow() throws IOException, ServiceException {

        // display a data store file chooser dialog for shapefiles
        URL capabilitiesURL = WMSChooser.showChooseWMS();
        if (capabilitiesURL == null) {
            System.exit(0); // canceled
        }
        WebMapServer wms = new WebMapServer(capabilitiesURL);

        List<Layer> wmsLayers = WMSLayerChooser.showSelectLayer(wms);
        if (wmsLayers == null) {
            JOptionPane.showMessageDialog(null, "Could not connect - check url");
            System.exit(0);
        }
        MapContent mapcontent = new MapContent();
        mapcontent.setTitle(wms.getCapabilities().getService().getTitle());

        // get available layers
        // see more http://docs.geotools.org/latest/userguide/tutorial/raster/image.html#raster-data
        WMSCapabilities capabilities = wms.getCapabilities();
        List<Layer> layers = capabilities.getLayerList();
        for (Layer layer : layers) {
            System.out.println(layer);
        }

        for (Layer wmsLayer : wmsLayers) {
            WMSLayer displayLayer = new WMSLayer(wms, wmsLayer);
            mapcontent.addLayer(displayLayer);
        }
        // Now display the map
        JMapFrame.showMap(mapcontent);
    }
}
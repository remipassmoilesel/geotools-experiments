package org.remipassmoilesel.image;
import java.net.URL;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.wms.WMSChooser;
import org.geotools.swing.wms.WMSLayerChooser;

/**
 * This is a Web Map Server "quickstart" doing the minimum required to display
 * something on screen.
 *
 * Free WMS service:
 * http://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities
 *
 */
public class WMSLab extends JFrame {
    /**
     * Prompts the user for a wms service, connects, and asks for a layer and then
     * and displays its contents on the screen in a map frame.
     */
    public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
        URL capabilitiesURL = WMSChooser.showChooseWMS();
        if( capabilitiesURL == null ){
            System.exit(0); // canceled
        }
        WebMapServer wms = new WebMapServer( capabilitiesURL );

        List<Layer> wmsLayers = WMSLayerChooser.showSelectLayer( wms );
        if( wmsLayers == null ){
            JOptionPane.showMessageDialog(null, "Could not connect - check url");
            System.exit(0);
        }
        MapContent mapcontent = new MapContent();
        mapcontent.setTitle( wms.getCapabilities().getService().getTitle() );

        // get available layers
        // see more http://docs.geotools.org/latest/userguide/tutorial/raster/image.html#raster-data
        WMSCapabilities capabilities = wms.getCapabilities();
        List<Layer> layers = capabilities.getLayerList();
        for (Layer layer : layers) {
            System.out.println(layer);
        }

        for( Layer wmsLayer : wmsLayers ){
            WMSLayer displayLayer = new WMSLayer(wms, wmsLayer );
            mapcontent.addLayer(displayLayer);
        }
        // Now display the map
        JMapFrame.showMap(mapcontent);
    }
}
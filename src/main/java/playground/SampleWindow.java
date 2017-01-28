package playground;

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
import org.geotools.styling.SLD;
import org.remipassmoilesel.utils.GuiUtils;

import java.awt.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by remipassmoilesel on 14/01/17.
 */
public class SampleWindow {

    public static void main(String[] args) throws Exception {

        MapContent mapContent = new MapContent();

        String shapePath = "data/france-communes/communes-20160119.shp";
        String wmsUrl = "http://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities";
        int wmsLayerIndex = 0;

        // retrieve and add wms layer
        WebMapServer wms = new WebMapServer(new URL(wmsUrl));

        WMSCapabilities capabilities = wms.getCapabilities();
        Layer[] namedLayers = WMSUtils.getNamedLayers(capabilities);

        WMSLayer wmsLayer = new WMSLayer(wms, namedLayers[wmsLayerIndex]);
        mapContent.addLayer(wmsLayer);

        // retrieve a shape file and add it to a mapcontent
        Path shape = Paths.get(shapePath);

        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shape.toFile());
        SimpleFeatureSource shapeFileSource = dataStore
                .getFeatureSource();

        FeatureLayer shapeLayer = new FeatureLayer(shapeFileSource, SLD.createLineStyle(Color.darkGray, 0.2f));
        mapContent.addLayer(shapeLayer);

        //shapeLayer.setVisible(false);

        GuiUtils.showInWindow(mapContent);
    }

}

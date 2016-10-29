package org.remipassmoilesel.layers;

import org.geotools.data.ows.Layer;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.styling.StyleFactory;

import java.io.IOException;

/**
 * Feature layer example
 */
public class Layers {


    public static void main(String[] args) throws IOException {

        StyleFactory sf = CommonFactoryFinder.getStyleFactory();

        FeatureLayer layer = new FeatureLayer(new DefaultFeatureCollection(), sf.getDefaultStyle());

        // Misc vars on layer
        System.out.println(layer.getFeatureSource().getBounds());
        System.out.println(layer.getBounds());
        System.out.println(layer.isSelected());
        System.out.println(layer.isVisible());
        System.out.println(layer.getTitle());
    }

}

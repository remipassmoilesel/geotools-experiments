package org.remipassmoilesel.draw;

import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opengis.feature.simple.SimpleFeature;

import java.awt.*;

/**
 * Created by remipassmoilesel on 02/11/16.
 */
public class RendererBuilder {

    public static StreamingRenderer getRenderer(){

        StreamingRenderer renderer = new StreamingRenderer();

        RenderingHints javaHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderer.setJava2DHints(javaHints);

        renderer.addRenderListener(new RenderListener() {

            @Override
            public void featureRenderer(SimpleFeature feature) {
//                System.out.println(feature);
            }

            @Override
            public void errorOccurred(Exception e) {
                System.out.println(e);
            }
        });

        return renderer;
    }

}

package org.remipassmoilesel.draw.optimized;

import org.geotools.map.Layer;

import java.awt.image.BufferedImage;

/**
 * Created by remipassmoilesel on 02/11/16.
 */
public class LayerContainer {

    private static int layerNumber = -1;

    private final Layer layer;
    private final String layerId;
    private BufferedImage renderedImage;

    public LayerContainer(Layer layer) {

        this.renderedImage = null;

        // setup layer
        this.layerId = "layer_" + ++layerNumber;
        this.layer = layer;

    }

    public void setRenderedImage(BufferedImage renderedImage) {
        this.renderedImage = renderedImage;
    }

    public String getLayerId() {
        return layerId;
    }

    public BufferedImage getRenderedImage() {
        return renderedImage;
    }

    public void setVisible(boolean visible) {
        layer.setVisible(visible);
    }
}

package org.remipassmoilesel.draw.optimized;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Manager for map layers. Allow to render one layer at a time, and to store rendered images.
 */
public class MapLayersManager {

    private final ArrayList<LayerContainer> layers;
    private final MapContent globalMapContent;
    private ReferencedEnvelope mapBoundsToRender;
    private Dimension renderedDimensions;

    public MapLayersManager() {
        this.layers = new ArrayList<>();
        this.globalMapContent = new MapContent();
    }

    public void addLayer(Layer layer) {
        layers.add(new LayerContainer(layer));
        globalMapContent.addLayer(layer);
    }

    public void renderLayer(int id) {
        LayerContainer ctr = layers.get(id);
        ctr.setRenderedDimensions(renderedDimensions);
        ctr.setMapBoundsToRender(mapBoundsToRender);
        ctr.renderLayer();
    }

    public void renderLayerLater(int id, Runnable whenFinished) {
        new Thread(() -> {

            renderLayer(id);

            if (whenFinished != null) {
                whenFinished.run();
            }

        }).start();
    }

    public BufferedImage[] getRenderedImages() {

        BufferedImage[] images = new BufferedImage[layers.size()];
        for (int i = 0; i < layers.size(); i++) {
            images[i] = layers.get(i).getRenderedImage();
        }
        return images;
    }

    public MapContent getMapContent() {
        return globalMapContent;
    }

    public void setMapBoundsToRender(ReferencedEnvelope mapBoundsToRender) {
        this.mapBoundsToRender = mapBoundsToRender;
    }

    public void setRenderedDimensions(Dimension renderedDimensions) {
        this.renderedDimensions = renderedDimensions;
    }

    public void renderAllLayersLater(Runnable whenFinished) {
        new Thread(() -> {
            for (int i = 0; i < layers.size(); i++) {
                renderLayer(i);
            }

            if (whenFinished != null) {
                whenFinished.run();
            }
        }).start();

    }
}

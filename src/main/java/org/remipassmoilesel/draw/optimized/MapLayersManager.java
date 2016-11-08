package org.remipassmoilesel.draw.optimized;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.remipassmoilesel.draw.RendererBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manager for map layers. Allow to renderer one layer at a time, and to store rendered images.
 */
public class MapLayersManager {

    private final ArrayList<LayerContainer> layers;
    private final MapContent mapContent;
    private final ReentrantLock renderLock;
    private final StreamingRenderer renderer;

    private ReferencedEnvelope mapBoundsToRender;
    private Dimension renderedDimensions;

    public MapLayersManager() {
        this.layers = new ArrayList<>();
        this.mapContent = new MapContent();

        this.renderLock = new ReentrantLock();

        this.renderer = RendererBuilder.getRenderer();
        renderer.setMapContent(mapContent);
    }

    public void addLayer(Layer layer) {
        layers.add(new LayerContainer(layer));
        mapContent.addLayer(layer);
    }

    public void renderLayer(int id) {

        // lock rendering
        if (renderLock.tryLock() == false) {
            return;
        }

        LayerContainer layer = layers.get(id);
        String layerId = layer.getLayerId();

        // monitor time of rendering
        System.out.println(layerId + ": Start rendering ");
        long startRender = System.currentTimeMillis();

        // hide other layers
        for (int i = 0; i < layers.size(); i++) {
            LayerContainer lay = layers.get(i);
            if (i == id) {
                lay.setVisible(true);
            } else {
                lay.setVisible(false);
            }
        }

        BufferedImage renderedImage = new BufferedImage(renderedDimensions.width, renderedDimensions.height,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = renderedImage.createGraphics();
        renderer.paint(g2d, new Rectangle(renderedDimensions), mapBoundsToRender);

        layer.setRenderedImage(renderedImage);

        // display time of rendering
        long renderTime = System.currentTimeMillis() - startRender;
        System.out.println(layerId + ": Stop rendering, " + renderTime + " ms");

        renderLock.unlock();

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
        return mapContent;
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

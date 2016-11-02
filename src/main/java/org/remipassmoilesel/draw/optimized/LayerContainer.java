package org.remipassmoilesel.draw.optimized;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.remipassmoilesel.draw.RendererBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by remipassmoilesel on 02/11/16.
 */
public class LayerContainer {

    private static int layerNumber = -1;

    protected final ReentrantLock renderLock;
    private final Layer layer;
    private final MapContent mapContent;
    private final StreamingRenderer renderer;
    private BufferedImage renderedImage;
    private final String layerId;
    private Dimension renderedDimensions;
    private ReferencedEnvelope mapBoundsToRender;

    public LayerContainer(Layer layer) {

        this.mapContent = new MapContent();
        this.renderedImage = null;
        this.renderedDimensions = new Dimension();

        // setup layer
        this.layerId = "layer_" + ++layerNumber;
        this.layer = layer;
        mapContent.addLayer(layer);

        // set up map renderer
        this.renderLock = new ReentrantLock();
        this.renderer = RendererBuilder.getRenderer();
        renderer.setMapContent(mapContent);
    }

    public void renderLayer(){

        if (renderLock.tryLock() == false) {
            return;
        }

        System.out.println(layerId + ": Start rendering ");

        long startRender = System.currentTimeMillis();

        renderedImage = new BufferedImage(renderedDimensions.width, renderedDimensions.height,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = renderedImage.createGraphics();
        renderer.paint(g2d, new Rectangle(renderedDimensions), mapBoundsToRender);

        long renderTime = System.currentTimeMillis() - startRender;

        System.out.println(layerId + ": Stop rendering, " + renderTime + " ms");

        renderLock.unlock();

    }

    public void setRenderedDimensions(Dimension renderedDimensions) {
        this.renderedDimensions = renderedDimensions;
    }

    public BufferedImage getRenderedImage() {
        return renderedImage;
    }

    public void setMapBoundsToRender(ReferencedEnvelope mapBoundsToRender) {
        this.mapBoundsToRender = mapBoundsToRender;
    }
}

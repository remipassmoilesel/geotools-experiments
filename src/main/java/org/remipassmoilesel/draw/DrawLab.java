package org.remipassmoilesel.draw;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.styling.StyleFactory;
import org.remipassmoilesel.render.RenderLab;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 * Small trial around user drawing. See draw.optimized for an optimized version.
 */
public class DrawLab extends RenderLab {

    private static final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private final FeatureLayer drawLayer;
    private final LineBuilder lineBuilder;
    private final DefaultFeatureCollection shapesCollection;

    public static void main(String[] args) {
        launchWindow(new DrawLab(true, true));
    }

    public DrawLab(boolean setupWms, boolean setupShape) throws HeadlessException {
        super(setupWms, setupShape);

        // add a layer to draw on
        shapesCollection = new DefaultFeatureCollection();
        drawLayer = new FeatureLayer(shapesCollection, sf.getDefaultStyle());

        mapContent.addLayer(drawLayer);

        // add a line builder
        lineBuilder = new LineBuilder();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);

        if (e.isControlDown()) {
            return;
        }

//        showCoordinates(e);

        Point2D worldPosition;

        int width = this.getWidth();
        int height = this.getHeight();

        // transform mouse position
        try {
            AffineTransform worldToScreen = RendererUtilities.worldToScreenTransform(mapBoundsToRender, new Rectangle(width, height));
            AffineTransform screenToWorld = worldToScreen.createInverse();

            worldPosition = screenToWorld.transform(e.getPoint(), null);

        } catch (NoninvertibleTransformException e1) {
            throw new RuntimeException("Error while converting positions: " + e1.getMessage());
        }


        if (e.getClickCount() < 2) {

            // 1: create line if not already drawing
            if (lineBuilder.isDrawing() == false) {
                lineBuilder.start(drawLayer, shapesCollection, worldPosition);
            }

            // 2: add points if already drawing
            else {
                lineBuilder.addPoint(worldPosition);
            }
        }

        // 3: terminate line if double click
        else if (e.getClickCount() > 1 && lineBuilder.isDrawing()) {
            lineBuilder.finish(worldPosition);
        }

        renderImage();

        repaint();

    }

    private void showCoordinates(MouseEvent e) {

        int width = this.getWidth();
        int height = this.getHeight();

        System.out.println("e.getPoint()");
        System.out.println(e.getPoint());

        try {
            AffineTransform worldToScreen = RendererUtilities.worldToScreenTransform(mapBoundsToRender, new Rectangle(width, height));
            AffineTransform screenToWorld = worldToScreen.createInverse();

            System.out.println("screenToWorld.transform(e.getPoint(), null)");
            System.out.println(screenToWorld.transform(e.getPoint(), null));

        } catch (NoninvertibleTransformException e1) {
            e1.printStackTrace();
        }

    }
}

package org.remipassmoilesel.partialrenderer;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Display a map by using partial cache system
 * <p>
 * Cache is managed by a RenderedPartialFactory. This partial factory produce portions of map and store it.
 */
public class CacheMapPane extends JPanel {

    private final CacheRenderer renderer;
    /**
     * World bounds to render
     */
    private ReferencedEnvelope worldBounds;

    /**
     * Or ULC point to render from
     */
    private Point2D worldPosition;

    /**
     * Map to render
     */
    private MapContent map;

    public CacheMapPane(MapContent map) {
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        this.map = map;
        this.renderer = new CacheRenderer(map);
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        //System.out.println();
        //System.out.println("protected void paintComponent(Graphics g) {");

        Rectangle screenBounds = g2d.getClipBounds();

        if (screenBounds.width < 1 || screenBounds.height < 1) {
            System.out.println("Screen bounds too small");
            return;
        }

        // search which partials are necessary to display
        RenderedPartialQueryResult rs;

        if (worldPosition != null) {
            renderer.setWorldPosition(worldPosition);
        } else {
            renderer.setWorldBounds(worldBounds);
        }

        renderer.render(g, g.getClipBounds().getSize());

    }

    public void setWorldBounds(ReferencedEnvelope bounds) {
        this.worldBounds = bounds;
    }

    public void setWorldPosition(Point2D worldPoint) {
        this.worldPosition = worldPoint;
    }

    public Point2D getWorldPosition() {
        return new Point2D.Double(worldPosition.getX(), worldPosition.getY());
    }

    public double getPartialSideDg() {
        return renderer.getPartialSideDg();
    }

    public int getPartialSidePx() {
        return renderer.getPartialSidePx();
    }

    public void setPartialSideDg(double partialSideDg) {
        renderer.setPartialSideDg(partialSideDg);
    }
}

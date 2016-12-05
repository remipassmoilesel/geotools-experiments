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
public class CachedMapPane extends JPanel {

    /**
     * If set to true, the partial grid is displayed
     */
    private boolean showGrid = true;

    /**
     * Retrieve and produce partials of a map
     */
    private final RenderedPartialFactory partialFactory;

    /**
     * World bounds to render
     */
    private ReferencedEnvelope worldBounds;

    /**
     * Map to render
     */
    private MapContent map;

    public CachedMapPane(MapContent map) {
        this.worldBounds = new ReferencedEnvelope();
        this.partialFactory = new RenderedPartialFactory(map);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
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
        RenderedPartialQueryResult rs = partialFactory.intersect(worldBounds);

        // get affine transform to set position of partials
        AffineTransform worldToScreen = rs.getWorldToScreenTransform();

        if(showGrid){
            g2d.setColor(Color.darkGray);
        }

        for (RenderedPartial part : rs.getPartials()) {

            // compute position of tile on map
            ReferencedEnvelope ev = part.getEnvelope();
            Point2D.Double worldPos = new Point2D.Double(ev.getMinX(), ev.getMaxY());
            Point2D screenPos = worldToScreen.transform(worldPos, null);

            int x = (int) screenPos.getX();
            int y = (int) screenPos.getY();
            int w = part.getRenderedWidth();
            int h = part.getRenderedHeight();

            // draw partial
            g2d.drawImage(part.getImage(), x, y, w, h, null);

            if (showGrid) {
                g2d.drawRect(x, y, w, h);
            }

        }

        // draw maximums bounds aked if necessary
        if (showGrid) {

            Point2D.Double ulcWld = new Point2D.Double(worldBounds.getMinX(), worldBounds.getMaxY());
            Point2D.Double brcWld = new Point2D.Double(worldBounds.getMaxX(), worldBounds.getMinY());

            Point2D ulcPx = worldToScreen.transform(ulcWld, null);
            Point2D brcPx = worldToScreen.transform(brcWld, null);

            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.red);
            g2d.drawRect((int) ulcPx.getX(), (int) ulcPx.getY(), 3, 3);
            g2d.drawRect((int) brcPx.getX(), (int) brcPx.getY(), 3, 3);

        }

    }

    public void setWorldBounds(ReferencedEnvelope bounds) {
        this.worldBounds = bounds;
    }

}

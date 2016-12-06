package org.remipassmoilesel.partialrenderer;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Created by remipassmoilesel on 06/12/16.
 */
public class CacheRenderer {

    /**
     * If set to true, the partial grid is displayed
     */
    private boolean showGrid = true;

    /**
     * Retrieve and produce partials of a map
     */
    private final RenderedPartialFactory partialFactory;

    /**
     * Or ULC point to render from
     */
    private Point2D worldPosition;

    /**
     * Map to render
     */
    private MapContent map;

    public CacheRenderer(MapContent map) {
        this.partialFactory = new RenderedPartialFactory(map);
        this.map = map;
    }

    protected void render(Graphics g, Dimension pixelDimensions) {
        render((Graphics2D) g, pixelDimensions);
    }

    protected void render(Graphics2D g2d, Dimension pixelDimensions) {

        // search which partials are necessary to display
        RenderedPartialQueryResult result = partialFactory.intersect(worldPosition, pixelDimensions, map.getCoordinateReferenceSystem());

        // get affine transform to set position of partials
        AffineTransform worldToScreen = result.getWorldToScreenTransform();

        if (showGrid) {
            g2d.setColor(Color.darkGray);
        }

        for (RenderedPartialImage part : result.getPartials()) {

            // compute position of tile on map
            ReferencedEnvelope ev = part.getEnvelope();
            Point2D.Double worldPos = new Point2D.Double(ev.getMinX(), ev.getMaxY());
            Point2D screenPos = worldToScreen.transform(worldPos, null);

            int x = (int) Math.round(screenPos.getX());
            int y = (int) Math.round(screenPos.getY());
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

            if (worldPosition != null) {

                Point2D wp = worldToScreen.transform(worldPosition, null);

                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(Color.red);
                g2d.drawRect((int) wp.getX(), (int) wp.getY(), 3, 3);

            }

        }


    }

    /**
     * Set world position from which render
     *
     * @param worldPoint
     */
    public void setWorldPosition(Point2D worldPoint) {
        this.worldPosition = worldPoint;
    }

    /**
     * Get world position
     *
     * @return
     */
    public Point2D getWorldPosition() {
        return new Point2D.Double(worldPosition.getX(), worldPosition.getY());
    }

    /**
     * Set to true to show partial grid and marks
     *
     * @param showGrid
     */
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public double getPartialSideDg() {
        return partialFactory.getPartialSideDg();
    }

    public int getPartialSidePx() {
        return partialFactory.getPartialSidePx();
    }

    public void setPartialSideDg(double partialSideDg) {
        partialFactory.setPartialSideDg(partialSideDg);
    }
}

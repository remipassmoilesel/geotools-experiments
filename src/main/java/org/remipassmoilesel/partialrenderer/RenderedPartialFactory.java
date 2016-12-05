package org.remipassmoilesel.partialrenderer;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.remipassmoilesel.draw.RendererBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Store and create partials
 */
public class RenderedPartialFactory {

    /**
     * List of available in memory partials.
     */
    private final ArrayList<RenderedPartial> partials;

    /**
     * Geotools renderer used to create partials
     */
    private final StreamingRenderer renderer;

    /**
     * Associated map content
     */
    private final MapContent mapContent;

    /**
     * Zoom level of current rendering
     */
    private double zoomLevel;

    private Dimension tileDim = new Dimension(100, 100);

    public RenderedPartialFactory(MapContent content) {
        partials = new ArrayList<>();
        renderer = RendererBuilder.getRenderer();
        mapContent = content;

        zoomLevel = 0.4f;

        renderer.setMapContent(mapContent);
    }

    public RenderedPartialQueryResult intersect(ReferencedEnvelope worldBounds) {

        //System.out.println();
        //System.out.println("public RenderedPartialQueryResult intersect(ReferencedEnvelope worldBounds) {");

        ArrayList<RenderedPartial> rsparts = new ArrayList<>();

        // Side value in decimal degree of each partial
        double incr = zoomLevel;

        // count partials
        int tileNumberW = 0;
        int tileNumberH = 0;

        // first position to go from
        // position is rounded in order to have partials that can be reused in future display
        double x = getStartFrom(worldBounds.getMinX());
        double y = getStartFrom(worldBounds.getMinY());

        // iterate area to render from bottom left corner to upper right corner
        while (y < worldBounds.getMaxY()) {

            // count horizontal partials only on the first line
            if (tileNumberH == 0) {
                tileNumberW++;
            }

            // area of current partial
            ReferencedEnvelope area = new ReferencedEnvelope(x, x + incr, y, y + incr, DefaultGeographicCRS.WGS84);

            // find existing partial
            RenderedPartial searched = new RenderedPartial(null, area);
            int index = partials.indexOf(searched);
            if (index != -1) {
                rsparts.add(partials.get(index));
            }

            // or create a new one
            else {
                renderPartial(searched);
                partials.add(searched);
                rsparts.add(searched);
            }

            x += incr;

            // change line when finished
            if (x > worldBounds.getMaxX()) {
                y += incr;
                tileNumberH++;

                // reset x except the last loop
                if (y < worldBounds.getMaxY()) {
                    x = getStartFrom(worldBounds.getMinX());
                }
            }

        }

        // if not enough tiles, return null to avoid errors on transformations
        if (rsparts.size() < 1) {
            return null;
        }

        double w = worldBounds.getWidth();
        double h = worldBounds.getHeight();

        // compute real screen bounds of asked world area
        // given that we used fixed size partials, area can be larger than asked one
        Rectangle screenBounds = new Rectangle(0, 0,
                (int) Math.round(w * tileDim.width / incr),
                (int) Math.round(h * tileDim.height / incr));

        return new RenderedPartialQueryResult(rsparts, worldBounds, screenBounds, tileNumberW, tileNumberH);
    }

    /**
     * Get the closest start point of specified coordinate.
     * <p>
     * Coordinates are normalized in order to have reusable partials
     *
     * @param coord
     * @return
     */
    public double getStartFrom(double coord) {
        return Math.floor(coord * 10) / 10;
    }

    /**
     * Generate a partial image
     *
     * @param part
     */
    public void renderPartial(RenderedPartial part) {

        ReferencedEnvelope bounds = part.getEnvelope();

        // create an image, and render map
        BufferedImage img = new BufferedImage(tileDim.width, tileDim.height, BufferedImage.TYPE_INT_ARGB);

        renderer.paint((Graphics2D) img.getGraphics(), new Rectangle(tileDim), bounds);

        // keep image
        part.setImage(img);

        System.out.println("Rendered: " + bounds);
    }

    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel = zoomLevel;
    }
}

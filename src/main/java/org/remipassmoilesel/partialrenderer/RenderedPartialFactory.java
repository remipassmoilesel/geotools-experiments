package org.remipassmoilesel.partialrenderer;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.remipassmoilesel.draw.RendererBuilder;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Store and create partials
 * <p>
 * Partials are fixed size squares. Same degree value is used for partial height and width but Geotools
 * renderer is supposed to compensate (no deformation should appear)
 * <p>
 * Need tests at low latitude
 */
public class RenderedPartialFactory {

    /**
     * Count how many partials are rendered
     */
    private static int renderedPartials = 0;

    /**
     * Count how many partials are reused
     */
    private static int reusedPartials = 0;

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
    private double partialSideDg = 2d;

    private int partialSidePx = 500;

    public RenderedPartialFactory(MapContent content) {

        partials = new ArrayList<>();
        renderer = RendererBuilder.getRenderer();
        mapContent = content;

        renderer.setMapContent(mapContent);
    }

    /**
     * Get partials from Upper Left Corner (world) position with specified dimension
     *
     * @param ulc
     * @param pixelDimension
     * @return
     */
    public RenderedPartialQueryResult intersect(Point2D ulc, Dimension pixelDimension, CoordinateReferenceSystem crs) {

        double partialSideDg = this.partialSideDg;

        // get width and height in decimal dg
        double wdg = partialSideDg * pixelDimension.width / partialSidePx;
        double hdg = partialSideDg * pixelDimension.height / partialSidePx;

        // create a new enveloppe
        double x1 = ulc.getX();
        double y1 = ulc.getY() - hdg; // to BLC
        double x2 = ulc.getX() + wdg;
        double y2 = ulc.getY();

        // create a new envelope
        return intersect(new ReferencedEnvelope(x1, x2, y1, y2, crs));

    }

    /**
     * Get partials around a world envelope
     *
     * @param worldBounds
     * @return
     */
    public RenderedPartialQueryResult intersect(ReferencedEnvelope worldBounds) {

        //System.out.println();
        //System.out.println("public RenderedPartialQueryResult intersect(ReferencedEnvelope worldBounds) {");

        ArrayList<RenderedPartial> rsparts = new ArrayList<>();

        // Side value in decimal degree of each partial
        double partialSideDg = this.partialSideDg;

        // count partials
        int tileNumberW = 0;
        int tileNumberH = 0;

        // first position to go from
        // position is rounded in order to have partials that can be reused in future display
        double x = getStartPointFrom(worldBounds.getMinX());
        double y = getStartPointFrom(worldBounds.getMinY());

        // iterate area to render from bottom left corner to upper right corner
        while (y < worldBounds.getMaxY()) {

            // count horizontal partials only on the first line
            if (tileNumberH == 0) {
                tileNumberW++;
            }

            // area of current partial
            ReferencedEnvelope area = new ReferencedEnvelope(x, x + partialSideDg, y, y + partialSideDg, DefaultGeographicCRS.WGS84);

            // find existing partial
            RenderedPartial searched = new RenderedPartial(null, area);
            int index = partials.indexOf(searched);
            if (index != -1) {
                rsparts.add(partials.get(index));

                reusedPartials++;
            }

            // or create a new one
            else {
                renderPartial(searched);
                partials.add(searched);
                rsparts.add(searched);

                renderedPartials++;
            }

            x += partialSideDg;

            // change line when finished
            if (x > worldBounds.getMaxX()) {
                y += partialSideDg;
                tileNumberH++;

                // reset x except the last loop
                if (y < worldBounds.getMaxY()) {
                    x = getStartPointFrom(worldBounds.getMinX());
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
                (int) Math.round(w * partialSidePx / partialSideDg),
                (int) Math.round(h * partialSidePx / partialSideDg));

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
    public double getStartPointFrom(double coord) {

        double mod = coord % partialSideDg;
        if (mod < 0) {
            mod += partialSideDg;
        }

        double rslt = coord - mod;

        return rslt;
    }

    /**
     * Generate a partial image
     *
     * @param part
     */
    public void renderPartial(RenderedPartial part) {

        ReferencedEnvelope bounds = part.getEnvelope();

        // create an image, and render map
        BufferedImage img = new BufferedImage(partialSidePx, partialSidePx, BufferedImage.TYPE_INT_ARGB);

        renderer.paint((Graphics2D) img.getGraphics(), new Rectangle(partialSidePx, partialSidePx), bounds);

        // keep image
        part.setImage(img);

        //System.out.println("Rendered: " + bounds);
    }

    public void setPartialSideDg(double partialSideDg) {
        this.partialSideDg = partialSideDg;
    }

    public static int getRenderedPartials() {
        return renderedPartials;
    }

    public static int getReusedPartials() {
        return reusedPartials;
    }
}

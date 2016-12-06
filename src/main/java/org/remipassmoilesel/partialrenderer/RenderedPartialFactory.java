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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Store and create partials
 * <p>
 * Partials are fixed size squares. Same degree value is used for partial height and width but Geotools
 * renderer is supposed to compensate (no deformation should appear)
 * <p>
 * Need tests at low latitude
 *
 * <p>//TOD: multithread rendering</p>
 */
public class RenderedPartialFactory {

    /**
     * Count how many partials are rendered
     */
    private static long renderedPartials = 0;

    /**
     * Geotools renderer used to create partials
     */
    private final StreamingRenderer renderer;

    /**
     * Associated map content
     */
    private final MapContent mapContent;

    /**
     * Where are stored partials
     */
    private final RenderedPartialStore store;

    /**
     * Zoom level of current rendering
     */
    private double partialSideDg = 2d;

    private int partialSidePx = 500;

    public RenderedPartialFactory(MapContent content) {

        renderer = RendererBuilder.getRenderer();
        mapContent = content;

        renderer.setMapContent(mapContent);

        try {
            store = new RenderedPartialStore(PartialRenderLab.CACHE_DATABASE_DIR.resolve("partials.db"));
        } catch (SQLException e) {
            throw new RuntimeException("Unable to initialize database: " + e.getMessage(), e);
        }
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

        ArrayList<RenderedPartialImage> rsparts = new ArrayList<>();

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
            ReferencedEnvelope area = new ReferencedEnvelope(x, round(x + partialSideDg), y, round(y + partialSideDg), DefaultGeographicCRS.WGS84);

            // try to find partial in store
            RenderedPartialImage part = null;
            try {
                part = store.getPartial(area);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // partial have been found, add to results
            if (part != null) {
                rsparts.add(part);
            }

            // partial not found, create a new one
            else {
                RenderedPartialImage newPart = new RenderedPartialImage(null, area);
                renderPartial(newPart);
                try {
                    store.addPartial(newPart);
                } catch (SQLException e) {
                    e.printStackTrace();
                    continue;
                }
                newPart.setupImageSoftReference();
                rsparts.add(newPart);
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

        return round(rslt);
    }

    /**
     * Add a value and round it to 6 decimal, in order to normalize coordinates and have reusable partials
     *
     * @param coord
     * @return
     */
    public double round(double coord) {
        return Math.round(coord * 1000000.0) / 1000000.0;
    }

    /**
     * Generate a partial image
     *
     * @param part
     */
    public void renderPartial(RenderedPartialImage part) {

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

    /**
     * Get number of rendered partial. For debug purpose.
     *
     * @return
     */
    public static long getRenderedPartials() {
        return renderedPartials;
    }

    public int getPartialSidePx() {
        return partialSidePx;
    }

    public double getPartialSideDg() {
        return partialSideDg;
    }

}

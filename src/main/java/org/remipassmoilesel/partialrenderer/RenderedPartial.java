package org.remipassmoilesel.partialrenderer;

import com.j256.ormlite.table.DatabaseTable;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Portion of map. Contains:
 * <p>
 * - Bounds in real world coordinates
 * <p>
 * - A rendered image
 * <p>
 * Final goal is to store partials in a database and to retain render images with soft links,
 * to avoid OutOfMemoryException
 */
@DatabaseTable(tableName = "partials")
public class RenderedPartial {

    /**
     * Rendered image
     */
    private BufferedImage image;

    /**
     * World coordinate BLC
     */
    private double x1;

    /**
     * World coordinate
     */
    private double x2;

    /**
     * World coordinate
     */
    private double y1;

    /**
     * World coordinate
     */
    private double y2;

    private CoordinateReferenceSystem crs;

    /**
     * Size in pixel
     */
    private int renderedWidth;

    /**
     * Size in pixel
     */
    private int renderedHeight;

    public RenderedPartial() {
    }

    public RenderedPartial(BufferedImage img, ReferencedEnvelope ev) {
        this(img, ev.getMinX(), ev.getMaxX(), ev.getMinY(), ev.getMaxY(), ev.getCoordinateReferenceSystem());
    }

    public RenderedPartial(BufferedImage img, double x1, double x2, double y1, double y2, CoordinateReferenceSystem crs) {
        this.image = img;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.crs = crs;

        // not used for now
        this.layerId = "";
        this.zoomLevel = 0.1f;
        this.generated = System.currentTimeMillis();
    }

    public BufferedImage getImage() {
        return image;
    }

    public ReferencedEnvelope getEnvelope() {
        return new ReferencedEnvelope(x1, x2, y1, y2, crs);
    }

    /**
     * Only enveloppe is used
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RenderedPartial that = (RenderedPartial) o;
        return Double.compare(that.x1, x1) == 0 &&
                Double.compare(that.x2, x2) == 0 &&
                Double.compare(that.y1, y1) == 0 &&
                Double.compare(that.y2, y2) == 0 &&
                Objects.equals(crs, that.crs);
    }

    @Override
    public String toString() {
        return "RenderedPartial{" +
                "image=" + image +
                ", x1=" + x1 +
                ", x2=" + x2 +
                ", y1=" + y1 +
                ", y2=" + y2 +
                ", crs=" + crs.getName() +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, x2, y1, y2, crs);
    }

    public void setImage(BufferedImage img) {
        this.image = img;
        this.renderedWidth = img.getWidth();
        this.renderedHeight = img.getHeight();
    }

    /**
     * Identifier used to invalidate cache when needed
     */
    private long generated;

    /**
     * Several zoom level will be available
     */
    private float zoomLevel;

    /**
     * Tiles will be stored by layer
     */
    private String layerId;

    public int getRenderedHeight() {
        return renderedHeight;
    }

    public int getRenderedWidth() {
        return renderedWidth;
    }
}

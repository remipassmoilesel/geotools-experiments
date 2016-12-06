package org.remipassmoilesel.cacherenderer;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
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
@DatabaseTable(tableName = RenderedPartialImage.TABLE_NAME)
public class RenderedPartialImage {

    public static final String TABLE_NAME = "PARTIALS";
    public static final String PARTIAL_ID_FIELD_NAME = "ID";
    public static final String PARTIAL_IMAGE_FIELD_NAME = "IMAGE";
    public static final String PARTIAL_X1_FIELD_NAME = "X1";
    public static final String PARTIAL_X2_FIELD_NAME = "X2";
    public static final String PARTIAL_Y1_FIELD_NAME = "Y1";
    public static final String PARTIAL_Y2_FIELD_NAME = "Y2";
    public static final String PARTIAL_CRS_FIELD_NAME = "CRS";


    @DatabaseField(generatedId = true, columnName = PARTIAL_ID_FIELD_NAME)
    private long id;

    /**
     * Rendered image
     */
    @DatabaseField(columnName = PARTIAL_IMAGE_FIELD_NAME, persisterClass = BufferedImagePersister.class)
    private BufferedImage image;

    private SoftReference<BufferedImage> imageSoftRef;

    /**
     * World coordinate BLC
     */
    @DatabaseField(columnName = PARTIAL_X1_FIELD_NAME)
    private double x1;

    /**
     * World coordinate
     */
    @DatabaseField(columnName = PARTIAL_X2_FIELD_NAME)
    private double x2;

    /**
     * World coordinate
     */
    @DatabaseField(columnName = PARTIAL_Y1_FIELD_NAME)
    private double y1;

    /**
     * World coordinate
     */
    @DatabaseField(columnName = PARTIAL_Y2_FIELD_NAME)
    private double y2;

    @DatabaseField(columnName = PARTIAL_CRS_FIELD_NAME)
    private String crsId;

    private CoordinateReferenceSystem crs;

    /**
     * Size in pixel
     */
    private int renderedWidth;

    /**
     * Size in pixel
     */
    private int renderedHeight;

    public RenderedPartialImage() {
    }

    public RenderedPartialImage(BufferedImage img, ReferencedEnvelope ev) {
        this(img, ev.getMinX(), ev.getMaxX(), ev.getMinY(), ev.getMaxY(), ev.getCoordinateReferenceSystem());
    }

    public RenderedPartialImage(BufferedImage img, double x1, double x2, double y1, double y2, CoordinateReferenceSystem crs) {
        this.image = img;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        setCRS(crs);

        // not used for now
        this.layerId = "";
        this.zoomLevel = 0.1f;
        this.generated = System.currentTimeMillis();
    }

    /**
     * Return reference to image if image is yet in memory.
     * <p>
     * If not return null
     *
     * @return
     */
    public BufferedImage getImage() {

        if (imageSoftRef == null) {
            return image;
        }

        return imageSoftRef.get();
    }

    public ReferencedEnvelope getEnvelope() {
        return new ReferencedEnvelope(x1, x2, y1, y2, crs);
    }

    /**
     * Only envelope is used to test equality
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RenderedPartialImage that = (RenderedPartialImage) o;
        return Double.compare(that.x1, x1) == 0 &&
                Double.compare(that.x2, x2) == 0 &&
                Double.compare(that.y1, y1) == 0 &&
                Double.compare(that.y2, y2) == 0 &&
                Objects.equals(crs, that.crs);
    }

    @Override
    public String toString() {
        return "RenderedPartialImage{" +
                "image=" + image +
                ", x1=" + x1 +
                ", x2=" + x2 +
                ", y1=" + y1 +
                ", y2=" + y2 +
                ", crs=" + (crs != null ? crs.getName() : crs) +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, x2, y1, y2, crs);
    }

    public void setImage(BufferedImage img) {
        this.image = img;
        updateImageDimensions();
    }

    public void updateImageDimensions() {

        if (getImage() == null) {
            return;
        }

        this.renderedWidth = getImage().getWidth();
        this.renderedHeight = getImage().getHeight();
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

    /**
     * Create a soft reference to the image and break internal hard reference
     */
    public void setupImageSoftReference() {

        if (image == null) {
            throw new NullPointerException("Cannot setup soft reference: image is null");
        }

        // create soft reference
        this.imageSoftRef = new SoftReference<>(this.image);

        // break old ones
        this.image = null;
    }

    public void setUpCRS() throws FactoryException {

        if (this.crsId == null) {
            throw new NullPointerException("Cannot setup CRS, CRS id is null");
        }

        this.crs = CRS.decode(crsId);
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
        this.crsId = crsToId(crs);
    }

    public static String crsToId(CoordinateReferenceSystem crs) {
        String authority = crs.getName().getAuthority() != null ? crs.getName().getAuthority() + ":" : "";
        return authority + crs.getName().getCode();
    }

    public String getCrsId() {
        return crsId;
    }
}

package org.remipassmoilesel.geometry;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

/**
 * Misc trials on geometry objects
 */
public class GeometryLab {

    public static void main(String[] args) {

        // France metrop
        ReferencedEnvelope mapBoundsToRender = new ReferencedEnvelope(-5.40d, 9.91d, 41d, 51.08d, DefaultGeographicCRS.WGS84);

        // get number of dimensions
        System.out.println(mapBoundsToRender.getDimension());

        // get values
        System.out.println(mapBoundsToRender.getMinimum(0));
        System.out.println(mapBoundsToRender.getMaximum(0));
        System.out.println(mapBoundsToRender.getMinimum(1));
        System.out.println(mapBoundsToRender.getMaximum(1));

    }

}

package org.remipassmoilesel.jts;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.geometry.jts.JTSFactoryFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple trials with JTS.
 * <p>
 * /!\ Only references to coordinates are used
 */
public class JTSLab {

    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    public static void main(String[] args) {

        // prefer use of lists instead of arrays to add points
        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.addAll(Arrays.asList(new Coordinate[]{
                new Coordinate(1, 1),
                new Coordinate(10, 10),
                new Coordinate(20, 20),
        }));

        // create a line string
        LineString line = geometryFactory.createLineString(coords.toArray(new Coordinate[coords.size()]));

        System.out.println(line);

        // get all vertices from line
        CoordinateSequence cseq = line.getCoordinateSequence();

        // change values: 0: first coordinate object, 0: x, 5: value
        cseq.setOrdinate(0, 0, 5);
        System.out.println(line);

        cseq.setOrdinate(0, 1, 6);
        System.out.println(line);

        cseq.setOrdinate(1, 1, 7);
        System.out.println(line);

        try {
            // add point: ArrayIndexOutOfBoundsException
            cseq.setOrdinate(3, 0, 30);
            cseq.setOrdinate(3, 1, 30);
            System.out.println(line);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        coords.add(new Coordinate(30, 30));
        line = geometryFactory.createLineString(coords.toArray(new Coordinate[coords.size()]));

        System.out.println(line);
    }

}

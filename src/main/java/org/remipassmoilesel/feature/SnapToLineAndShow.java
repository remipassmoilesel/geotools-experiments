package org.remipassmoilesel.feature;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.files.StreamLogging;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.*;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Random;

import static org.hsqldb.lib.tar.TarHeaderField.name;

/**
 * Example modified to show layers in a window
 * <p>
 * <p>
 * This example illustrates a common spatial operation: moving or snapping a point to lie on a nearby line. For instance, you might use this approach to align point locations from a mobile device with a map of roads. The JTS library is used for this sort of task day in and day out.
 * <p>
 * What you will learn:
 * <p>
 * Use of a spatial index to cache features in memory and search efficiently.
 * Going beyond the familiar JTS Geometry class methods and making direct use of other classes.
 * <p>
 * Related material:
 * <p>
 * http://2007.foss4g.org/presentations/view.php?abstract_id=115
 */

public class SnapToLineAndShow {

    public static void main(String[] args) throws Exception {

        /*
         * Open a shapefile. You should choose one with line features
         * (LineString or MultiLineString geometry)
         *
         */
        File file = new File("data/simpleline/simple_line.shp");

        if (file == null) {
            file = JFileDataStoreChooser.showOpenFile("shp", null);
            if (file == null) {
                return;
            }
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        FeatureSource source = store.getFeatureSource();

        // Check that we have line features
        Class<?> geomBinding = source.getSchema().getGeometryDescriptor().getType().getBinding();
        boolean isLine = geomBinding != null
                && (LineString.class.isAssignableFrom(geomBinding) ||
                MultiLineString.class.isAssignableFrom(geomBinding));

        if (!isLine) {
            System.out.println("This example needs a shapefile with line features");
            return;
        }

        // Create a basic style with yellow lines and no fill
        Style lineStyle = SLD.createLineStyle(Color.RED, 3f);

        // Set up a MapContent with the two layers
        final MapContent map = new MapContent();
        map.setTitle("Snap to line");

        Layer shpLayer = new FeatureLayer(source, lineStyle);
        map.addLayer(shpLayer);

        final SpatialIndex index = new STRtree();
        FeatureCollection features = source.getFeatures();
        System.out.println("Slurping in features ...");
        features.accepts(new FeatureVisitor() {

            @Override
            public void visit(Feature feature) {
                SimpleFeature simpleFeature = (SimpleFeature) feature;
                Geometry geom = (MultiLineString) simpleFeature.getDefaultGeometry();
                // Just in case: check for  null or empty geometry
                if (geom != null) {
                    Envelope env = geom.getEnvelopeInternal();
                    if (!env.isNull()) {
                        index.insert(env, new LocationIndexedLine(geom));
                    }
                }
            }
        }, new NullProgressListener());

        // Create a collection and layer for points
        DefaultFeatureCollection pointCollection = new DefaultFeatureCollection();
        Style pointStyle = SLD.createPointStyle(
                "Circle",
                Color.black,
                Color.blue,
                0.5f,
                10f,
                null,
                null);

        FeatureLayer pointLayer = new FeatureLayer(pointCollection, pointStyle);
        map.addLayer(pointLayer);

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("GeneratedPoint");
        b.add("name", String.class);
        b.add("number", Integer.class);

        // always set crs before add geom
        b.setCRS(null);
        b.add("location", Point.class);

        SimpleFeatureBuilder pointFeatBuilder = new SimpleFeatureBuilder(b.buildFeatureType());

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

        /*
         * For test data, we generate a large number of points placed randomly
         * within the bounding rectangle of the features.
         */
        final int NUM_POINTS = 1000;
        ReferencedEnvelope bounds = features.getBounds();
        Coordinate[] points = new Coordinate[NUM_POINTS];
        Random rand = new Random(file.hashCode());
        for (int i = 0; i < NUM_POINTS; i++) {
            points[i] = new Coordinate(
                    bounds.getMinX() + rand.nextDouble() * bounds.getWidth(),
                    bounds.getMinY() + rand.nextDouble() * bounds.getHeight());

            // creating point
            Point point = geometryFactory.createPoint(points[i]);

            // 'add' calls have to respect order
            pointFeatBuilder.add("Point " + i);
            pointFeatBuilder.add(i);
            pointFeatBuilder.add(point);
            pointCollection.add(pointFeatBuilder.buildFeature(null));
        }

        // Create a collection and layer for points
        DefaultFeatureCollection snapCollection = new DefaultFeatureCollection();
        Style snapStyle = SLD.createPointStyle(
                "Circle",
                Color.black,
                Color.yellow,
                1f,
                10f,
                null,
                null);

        // add special layer for snapped points
        FeatureLayer snapLayer = new FeatureLayer(snapCollection, snapStyle);
        map.addLayer(snapLayer);

        SimpleFeatureTypeBuilder b2 = new SimpleFeatureTypeBuilder();
        b2.setName("SnappedPoints");
        b2.add("name", String.class);
        b2.add("number", Integer.class);

        // always set CRS before add geom
        b2.setCRS(null);
        b2.add("location", Point.class);

        SimpleFeatureBuilder snapFeatBuilder = new SimpleFeatureBuilder(b2.buildFeatureType());

        /*
         * We defined the maximum distance that a line can be from a point
         * to be a candidate for snapping (1% of the width of the feature
         * bounds for this example).
         */
        final double MAX_SEARCH_DISTANCE = bounds.getSpan(0) / 100.0;

        // Maximum time to spend running the snapping process (milliseconds)
        final long DURATION = 5000;

        int pointsProcessed = 0;
        int pointsSnapped = 0;
        long elapsedTime = 0;
        long startTime = System.currentTimeMillis();
        while (pointsProcessed < NUM_POINTS &&
                (elapsedTime = System.currentTimeMillis() - startTime) < DURATION) {

            // Get point and create search envelope
            Coordinate pt = points[pointsProcessed++];
            Envelope search = new Envelope(pt);
            search.expandBy(MAX_SEARCH_DISTANCE);

            /*
             * Query the spatial index for objects within the search envelope.
             * Note that this just compares the point envelope to the line envelopes
             * so it is possible that the point is actually more distant than
             * MAX_SEARCH_DISTANCE from a line.
             */
            List<LocationIndexedLine> lines = index.query(search);

            // Initialize the minimum distance found to our maximum acceptable
            // distance plus a little bit
            double minDist = MAX_SEARCH_DISTANCE + 1.0e-6;
            Coordinate minDistPoint = null;

            for (LocationIndexedLine line : lines) {
                LinearLocation here = line.project(pt);
                Coordinate point = line.extractPoint(here);
                double dist = point.distance(pt);
                if (dist < minDist) {
                    minDist = dist;
                    minDistPoint = point;
                }
            }


            if (minDistPoint == null) {
                // No line close enough to snap the point to
//                System.out.println(pt + "- X");

            } else {

                // creating point
                Point point = geometryFactory.createPoint(pt);

                // 'add' calls have to respect order
                snapFeatBuilder.add("Point " + pointsProcessed);
                snapFeatBuilder.add(pointsProcessed);
                snapFeatBuilder.add(point);

                snapCollection.add(snapFeatBuilder.buildFeature(null));

                System.out.printf("%s - snapped by moving %.4f\n",
                        pt.toString(), minDist);

                pointsSnapped++;
            }
        }

        System.out.printf("Processed %d points (%.2f points per second). \n"
                        + "Snapped %d points.\n\n",
                pointsProcessed,
                1000.0 * pointsProcessed / elapsedTime,
                pointsSnapped);

        // Create a JMapFrame with a menu to choose the display style for the
        JMapFrame frame = new JMapFrame(map);
        frame.setSize(800, 600);
        frame.enableStatusBar(true);
        frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN, JMapFrame.Tool.RESET);
        frame.enableToolBar(true);
        frame.enableLayerTable(true);
        frame.setVisible(true);
    }
}



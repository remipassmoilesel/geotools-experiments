package org.remipassmoilesel.worldfile;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.util.ProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * CRS Lab
 * <p>
 * This tutorial gives a visual demonstration of coordinate reference systems by displaying a
 * shapefile and shows how changing the map projection morphs the geometry of the features.
 * <p>
 * http://docs.geotools.org/latest/userguide/tutorial/geometry/geometrycrs.html
 */
public class CRSLab {

    private File sourceFile;
    private SimpleFeatureSource featureSource;
    private MapContent map;

    public static void main(String[] args) throws Exception {

//        CRSLab lab = new CRSLab();
//        lab.displayShapefile("data/ne_50m_admin/ne_50m_admin_0_countries.shp");

        //displayCRS(null);

        String wkt = "PROJCS[\"NAD83 / BC Albers\"," +
                "GEOGCS[\"NAD83\", " +
                "  DATUM[\"North_American_Datum_1983\", " +
                "    SPHEROID[\"GRS 1980\", 6378137.0, 298.257222101, AUTHORITY[\"EPSG\",\"7019\"]], " +
                "    TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0], " +
                "    AUTHORITY[\"EPSG\",\"6269\"]], " +
                "  PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], " +
                "  UNIT[\"degree\", 0.017453292519943295], " +
                "  AXIS[\"Lon\", EAST], " +
                "  AXIS[\"Lat\", NORTH], " +
                "  AUTHORITY[\"EPSG\",\"4269\"]], " +
                "PROJECTION[\"Albers_Conic_Equal_Area\"], " +
                "PARAMETER[\"central_meridian\", -126.0], " +
                "PARAMETER[\"latitude_of_origin\", 45.0], " +
                "PARAMETER[\"standard_parallel_1\", 50.0], " +
                "PARAMETER[\"false_easting\", 1000000.0], " +
                "PARAMETER[\"false_northing\", 0.0], " +
                "PARAMETER[\"standard_parallel_2\", 58.5], " +
                "UNIT[\"m\", 1.0], " +
                "AXIS[\"x\", EAST], " +
                "AXIS[\"y\", NORTH]]";
        CoordinateReferenceSystem example = CRS.parseWKT(wkt);

        System.out.println(CRS.lookupIdentifier(example, true));
        System.out.println(CRS.lookupIdentifier(CRS.decode("EPSG:4230"), true));
    }

    private static void displayCRS(String code) throws FactoryException {

        if (code == null) {
            code = "EPSG:4326";
        }

        CoordinateReferenceSystem crs = CRS.decode(code);
        String wkt = crs.toWKT();
        System.out.println("wkt for: " + code);
        System.out.println(wkt);

    }

    private void displayShapefile(String path) throws Exception {

        if (path == null || path.length() < 1) {
            sourceFile = JFileDataStoreChooser.showOpenFile("shp", null);
            if (sourceFile == null) {
                return;
            }
        } else {
            sourceFile = new File(path);
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(sourceFile);
        featureSource = store.getFeatureSource();

        // Create a map context and add our shapefile to it
        map = new MapContent();
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.layers().add(layer);

        // Create a JMapFrame with custom toolbar buttons
        JMapFrame mapFrame = new JMapFrame(map);
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);

        JToolBar toolbar = mapFrame.getToolBar();
        toolbar.addSeparator();
        toolbar.add(new JButton(new ValidateGeometryAction()));
        toolbar.add(new JButton(new ExportShapefileAction()));

        // Display the map frame. When it is closed the application will exit
        mapFrame.setSize(800, 600);
        mapFrame.setVisible(true);
    }

    /**
     * Button to check that feature geometries are valid (e.g. polygon boundaries are closed)
     */
    class ValidateGeometryAction extends SafeAction {

        ValidateGeometryAction() {
            super("Validate geometry");
            putValue(Action.SHORT_DESCRIPTION, "Check each geometry");


        }

        public void action(ActionEvent e) throws Throwable {

            int numInvalid = validateFeatureGeometry(null);
            String msg;
            if (numInvalid == 0) {
                msg = "All feature geometries are valid";
            } else {
                msg = "Invalid geometries: " + numInvalid;
            }
            JOptionPane.showMessageDialog(null, msg, "Geometry results",
                    JOptionPane.INFORMATION_MESSAGE);
        }

    }

    private int validateFeatureGeometry(ProgressListener progress) throws Exception {

        final SimpleFeatureCollection featureCollection = featureSource.getFeatures();

        // Rather than use an iterator, create a FeatureVisitor to check each fature
        class ValidationVisitor implements FeatureVisitor {
            public int numInvalidGeometries = 0;

            public void visit(Feature f) {
                SimpleFeature feature = (SimpleFeature) f;
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                if (geom != null && !geom.isValid()) {
                    numInvalidGeometries++;
                    System.out.println("Invalid Geoemtry: " + feature.getID());
                }
            }
        }

        ValidationVisitor visitor = new ValidationVisitor();

        // Pass visitor and the progress bar to feature collection
        featureCollection.accepts(visitor, progress);
        return visitor.numInvalidGeometries;
    }

    /**
     * Export a shapefile in a different CRS
     * The next action will form a little utility that can read in a shapefile and write out a shapefile in a different coordinate reference system.
     * <p>
     * One important thing to pick up from this lab is how easy it is to create a MathTransform between two CoordinateReferenceSystems. You can use
     * the MathTransform to transform points one at a time; or use the JTS utility class to create a copy of a Geometry with the points modified.
     * <p>
     * We use similar steps to export a shapefile as used by the Csv2Shape example. In this case we are reading the contents from an existing shapefile using
     * a FeatureIterator; and writing out the contents one at a time using a FeatureWriter. Please close these objects after use.
     */
    class ExportShapefileAction extends SafeAction {
        ExportShapefileAction() {
            super("Export...");
            putValue(Action.SHORT_DESCRIPTION, "Export using current crs");
        }

        public void action(ActionEvent e) throws Throwable {
            exportToShapefile();
        }


    }

    private void exportToShapefile() throws Exception {

        SimpleFeatureType schema = featureSource.getSchema();
        JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
        chooser.setDialogTitle("Save reprojected shapefile");
        chooser.setSaveFile(sourceFile);
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file.equals(sourceFile)) {
            JOptionPane.showMessageDialog(null, "Cannot replace " + file);
            return;
        }

        // Set up a math transform used to process the data:
        CoordinateReferenceSystem dataCRS = schema.getCoordinateReferenceSystem();
        CoordinateReferenceSystem worldCRS = map.getCoordinateReferenceSystem();
        boolean lenient = true; // allow for some error due to different datums
        MathTransform transform = CRS.findMathTransform(dataCRS, worldCRS, lenient);

        // Grab all features:
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();

        //To create a new shapefile we will need to produce a FeatureType that is similar to our original.
        // The only difference will be the CoordinateReferenceSystem of the geometry descriptor.
        DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
        Map<String, Serializable> create = new HashMap<>();
        create.put("url", file.toURI().toURL());
        create.put("create spatial index", Boolean.TRUE);
        DataStore dataStore = factory.createNewDataStore(create);
        SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(schema, worldCRS);
        dataStore.createSchema(featureType);

        //Get the name of the new Shapefile, which will be used to open the FeatureWriter
        String createdName = dataStore.getTypeNames()[0];

        Transaction transaction = new DefaultTransaction("Reproject");
        try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                     dataStore.getFeatureWriterAppend(createdName, transaction);
             SimpleFeatureIterator iterator = featureCollection.features()) {
            while (iterator.hasNext()) {
                // copy the contents of each feature and transform the geometry
                SimpleFeature feature = iterator.next();
                SimpleFeature copy = writer.next();
                copy.setAttributes(feature.getAttributes());

                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                Geometry geometry2 = JTS.transform(geometry, transform);

                copy.setDefaultGeometry(geometry2);
                writer.write();
            }
            transaction.commit();
            JOptionPane.showMessageDialog(null, "Export to shapefile complete");
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
            JOptionPane.showMessageDialog(null, "Export to shapefile failed");
        } finally {
            transaction.close();
        }
    }


}

package org.remipassmoilesel.image;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.operation.Mosaic;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.WorldFileWriter;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.gce.imagemosaic.ImageMosaicFormatFactory;
import org.geotools.gce.imagemosaic.ImageMosaicReader;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.Layer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.remipassmoilesel.utils.GuiBuilder;
import org.remipassmoilesel.utils.GuiUtils;
import sun.security.provider.Sun;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.sun.xml.internal.ws.policy.sourcemodel.wspolicy.XmlToken.Name;
import static java.lang.System.exit;
import static org.remipassmoilesel.worldfile.WorldFileLab.printPrjForCode;

/**
 * Operations with geotools mosaic plugin
 * <p>
 * Read a directory of pictures and create a gridcoverage.
 * <p>
 * Index and properties files must have the name of their directory. These files are not mandatory, they will be generated if missing.
 */
public class MosaicPluginLab {

    public static void main(String[] args) throws IOException, FactoryException {

//        writeMosaic("data/geoserver-mosaic-sample", "data/generated.jpg");

        Coordinate[] coords = new Coordinate[]{
                new Coordinate(10, 20),
                new Coordinate(20, 30),
                new Coordinate(30, 40)
        };
        buildMosaic("data/arbitrary-images", coords);

        showMosaic("data/arbitrary-images");
    }

    /**
     * Read a mosaic and write it to an image
     *
     * @param pathSrc
     * @param pathDest
     * @throws IOException
     */
    public static void writeMosaic(String pathSrc, String pathDest) throws IOException {

        ImageMosaicFormatFactory factory = new ImageMosaicFormatFactory();
        ImageMosaicFormat format = (ImageMosaicFormat) factory.createFormat();
        ImageMosaicReader reader = format.getReader(new File(pathSrc));

        GridCoverage2D coverage = reader.read(null);

        RenderedImage image = coverage.getRenderedImage();
        ImageIO.write(image, "jpg", new File(pathDest));

    }

    /**
     * Read a mosaic and display it on a window
     *
     * @param pathSrc
     * @throws IOException
     */
    public static void showMosaic(String pathSrc) throws IOException {

        ImageMosaicFormatFactory factory = new ImageMosaicFormatFactory();
        ImageMosaicFormat format = (ImageMosaicFormat) factory.createFormat();
        ImageMosaicReader reader = format.getReader(new File(pathSrc));

        GridCoverage2D coverage = reader.read(null);

        Layer rasterLayer = new GridCoverageLayer(coverage, GuiUtils.getDefaultRGBRasterStyle(reader));

        GuiBuilder.newMap("Mosaic: " + pathSrc).addLayer(rasterLayer).show();

    }

    /**
     * Construct a mosaic plugin index for a directory.
     *
     * @param directory
     * @throws IOException
     * @throws FactoryException
     */
    public static void buildMosaic(String directory, Coordinate[] positions) throws IOException, FactoryException {

        Path root = Paths.get(directory);

        // default CRS
//        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
        CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        // write a prj file - a WKT as a single line
        Path newPrj = Paths.get(root.toString(), root.getFileName() + ".prj");
        try (BufferedWriter writer = Files.newBufferedWriter(newPrj, Charset.forName("utf-8"))) {
            writer.write(crs.toString().replaceAll("\\s+", ""));
            writer.flush();
        }

        // build a feature type
        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("Tile");
        tbuilder.setCRS(crs);
        tbuilder.add("the_geom", Polygon.class);
        tbuilder.add("location", String.class);

        SimpleFeatureType type = tbuilder.buildFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        GeometryFactory geom = JTSFactoryFinder.getGeometryFactory();

        // build a feature collection
        ArrayList<SimpleFeature> features = new ArrayList<SimpleFeature>();

        // iterate tiles
        Iterator<Path> diterator = Files.newDirectoryStream(root).iterator();
        int i = 0;
        while (diterator.hasNext()) {
            Path f = diterator.next();

            // ignore non jpg files
            if (f.toString().toLowerCase().endsWith(".jpg") == false) {
                System.out.println("Ignore file: " + f.toString());
                continue;
            }

            System.out.println("Processing: " + f.toString());

            // get tile position
            double x = positions[i].x;
            double y = positions[i].y;

            // get tile size
            BufferedImage bimg = ImageIO.read(f.toFile());
            int w = bimg.getWidth();
            int h = bimg.getHeight();

            // generate geometry
            LinearRing square = geom.createLinearRing(new Coordinate[]{
                    new Coordinate(x, y),
                    new Coordinate(x + w, y),
                    new Coordinate(x + w, y + h),
                    new Coordinate(x, y + h),
                    new Coordinate(x, y),
            });

            // add tile to index
            builder.add(square);
            builder.add(f.getFileName().toString());

            features.add(builder.buildFeature(new String("Tile_" + i)));

            // generate world file
            createWorldFileForTile(f, x, y);

            i++;
        }

        // create the properties file
        Path prop = Paths.get(root.toString(), root.getFileName() + ".properties");
        try (BufferedWriter writer = Files.newBufferedWriter(prop, Charset.forName("utf-8"))) {

            // More on configuration: see MosaicConfigurationBean
            writer.write("# Mosaic plugin properties file");
            writer.newLine();
            writer.write("Levels=1.0,1.0");
            writer.newLine();
            writer.write("Heterogeneous=false");
            writer.newLine();
            writer.write("AbsolutePath=false");
            writer.newLine();
            writer.write("Name=" + root.getFileName());
            writer.newLine();
            writer.write("TypeName=" + root.getFileName());
            writer.newLine();
            writer.write("Caching=false");
            writer.newLine();
            writer.write("ExpandToRGB=false");
            writer.newLine();
            writer.write("LocationAttribute=location");
            writer.newLine();
            writer.write("SuggestedSPI=com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi");
            writer.newLine();
            writer.write("CheckAuxiliaryMetadata=false");
            writer.newLine();
            writer.write("LevelsNum=1");
            writer.newLine();

            writer.flush();
        }

        // create the shapefile index
        Path newIndex = Paths.get(root.toString(), root.getFileName() + ".shp");
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", newIndex.toUri().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(type);

        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        SimpleFeatureType shapeType = featureSource.getSchema();

        /*
         * The Shapefile format has a couple limitations:
         * - "the_geom" is always first, and used for the geometry attribute name
         * - "the_geom" must be of type Point, MultiPoint, MuiltiLineString, MultiPolygon
         * - Attribute names are limited in length
         * - Not all data types are supported (example Timestamp represented as Date)
         *
         * Each data store has different limitations so check the resulting SimpleFeatureType.
         */
        System.out.println("SHAPE: " + shapeType);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            /*
             * SimpleFeatureStore has a method to add features from a
             * SimpleFeatureCollection object, so we use the ListFeatureCollection
             * class to wrap our list of features.
             */
            SimpleFeatureCollection collection = new ListFeatureCollection(type, features);
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }

            System.out.println("Index created: " + newIndex.toString());

        } else {
            System.out.println(typeName + " does not support read/write access");
        }

    }

    private static void createWorldFileForTile(Path p, double x, double y) throws IOException {

        String strPath = p.toString();
        String dest = strPath.substring(0, strPath.length() - 1) + "w";

        AffineTransform translate = new AffineTransform(1, 0, 0, 1, x, y);
        new WorldFileWriter(new File(dest), translate);

        System.out.println("World file created: " + p.toString());

    }


}

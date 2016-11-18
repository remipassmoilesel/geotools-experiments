package org.remipassmoilesel.mosaic;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.WorldFileWriter;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.gce.imagemosaic.ImageMosaicFormatFactory;
import org.geotools.gce.imagemosaic.ImageMosaicReader;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.GridCoverageLayer;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.wkt.Formattable;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.remipassmoilesel.utils.GuiUtils;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Trial on large mosaic. ~~ 22 MP, 500 MB heap space, fluid except when render all layer in once
 */
public class HugeMosaicLabShp {

    // default CRS
    public static final CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;
    public static final GeometryFactory geom = JTSFactoryFinder.getGeometryFactory();


    public static void main(String[] args) throws IOException {

        Path root = Paths.get("data/arbitrary-pictures-2");

        createMosaic(root);

        showMosaic(root);

    }

    /**
     * Read a mosaic and display it on a window
     *
     * @param src
     * @throws IOException
     */
    public static void showMosaic(Path src) throws IOException {

        ImageMosaicFormatFactory factory = new ImageMosaicFormatFactory();
        ImageMosaicFormat format = (ImageMosaicFormat) factory.createFormat();
        ImageMosaicReader reader = format.getReader(src.toString());
        GridCoverage2D coverage = reader.read(null);
        GridCoverageLayer layer = new GridCoverageLayer(coverage, GuiUtils.getDefaultRGBRasterStyle(reader));

        GuiUtils.showInWindow(layer);

    }

    private static void createMosaic(Path root) throws IOException {

        // write a prj file - a WKT as a single line
        Path newPrj = Paths.get(root.toString(), root.getFileName() + ".prj");
        try (BufferedWriter writer = Files.newBufferedWriter(newPrj, Charset.forName("utf-8"))) {
            writer.write(((Formattable) crs).toWKT(0));
            writer.flush();
            writer.close();
        }

        // build a feature type corresponding to shapefile index
        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("tile");
        tbuilder.setCRS(crs);
        tbuilder.add("the_geom", Polygon.class);
        tbuilder.add("location", String.class);

        SimpleFeatureType type = tbuilder.buildFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);


        // build a feature collection
        DefaultFeatureCollection features = new DefaultFeatureCollection();

        int x = 0;
        int y = 0;
        int mX = 400;
        int mY = 600;
        int maxX = 6;
        int i = 0;

        // iterate tiles
        Iterator<Path> diterator = Files.newDirectoryStream(root).iterator();
        while (diterator.hasNext()) {
            Path f = diterator.next();

            // ignore non jpg files
            if (f.toString().toLowerCase().endsWith(".jpg") == false &&
                    f.toString().toLowerCase().endsWith(".jpeg") == false) {
                System.out.println("Ignore file: " + f.toString());
                continue;
            }

            System.out.println("Processing: " + f.toString());

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

            features.add(builder.buildFeature(null));

            // generate a world file
            createWorldFileForTile(f, x, y);

            System.out.println(x);
            System.out.println(y);
            System.out.println(i);

            i++;

            if(i < maxX){
                x += mX;
            }

            else {
                x = 0;
                i = 0;
                y += mY;
            }

        }

        // create the properties file
        Path propertyFile = root.resolve(root.getFileName() + ".properties");
        try (BufferedWriter writer = Files.newBufferedWriter(propertyFile, Charset.forName("utf-8"))) {

            // More on configuration: see MosaicConfigurationBean
            writer.write("# Mosaic plugin properties file");
            writer.newLine();
            writer.write("Levels=1.0,1.0");
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
            writer.write("CheckAuxiliaryMetadata=false");
            writer.newLine();
            writer.write("LevelsNum=1");
            writer.newLine();

            writer.flush();
            writer.close();
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

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            /*
             * SimpleFeatureStore has a method to add features from a
             * SimpleFeatureCollection object, so we use the ListFeatureCollection
             * class to wrap our list of features.
             */
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(features);
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
        Path dest = Paths.get(strPath.substring(0, strPath.length() - 1) + "w");

        AffineTransform translate = new AffineTransform(1, 0, 0, 1, x, y);
        new WorldFileWriter(dest.toFile(), translate);

        System.out.println("World file created: " + dest);
    }

}

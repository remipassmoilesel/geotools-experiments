package org.remipassmoilesel.geopackage;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.apache.commons.io.FileUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.image.WorldImageFormat;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.*;
import org.geotools.geopkg.mosaic.GeoPackageReader;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.remipassmoilesel.utils.GuiUtils;
import org.remipassmoilesel.utils.MiscUtils;
import org.remipassmoilesel.utils.SqliteUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static com.google.common.io.Resources.getResource;

/**
 * Created by remipassmoilesel on 07/11/16.
 */
public class GeopackageLab {

    //    public static final CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;
    private static final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
    private static final GeometryFactory geom = JTSFactoryFinder.getGeometryFactory();

    public static void main(String[] args) throws IOException, FactoryException, SQLException {

        Path dbpath = Paths.get("data/geopk.db");

        // create a geopackage
        //cleanExistingGeopackage(dbpath);
        //createGeopkg(dbpath);

        // misc trials on jdbc
        //jdbcLab(dbpath);

        // write access
        modifyGeopkg(dbpath);

        // display informations about geopackage
        readGeopkg(dbpath);

        // add project metadatas
        // addProjectMetadatas(dbpath);

    }

    public static void addProjectMetadatas(Path dbpath) throws IOException, SQLException {

        ProjectPropertiesDAO dao = new ProjectPropertiesDAO(dbpath);

        System.out.println("dao.getValues()");
        System.out.println(dao.getValues());

        System.out.println("dao.addValue(title, Ho ho !)");
        System.out.println(dao.addValue("title", "Ho ho !"));

        System.out.println("dao.getValues()");
        System.out.println(dao.getValues());

        System.out.println("dao.updateValue(title, Hey hey !)");
        System.out.println(dao.updateValue("title", "Hey hey !"));

        System.out.println("dao.getValues()");
        System.out.println(dao.getValues());

    }

    public static void cleanExistingGeopackage(Path dbpath) throws IOException {
        // clean previous database
        if (Files.exists(dbpath)) {
            Files.delete(dbpath);
        }
    }

    public static void createGeopkg(Path dbpath) throws IOException {

        Files.createFile(dbpath);

        GeoPackage geopkg = new GeoPackage(dbpath.toFile());
        geopkg.init();

        // first feature set
        FeatureEntry fe = new FeatureEntry();

        // will be overrided by feature name
        fe.setTableName("featureset1");

        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("point");

        // geometry is mandatory to avoid nullpointerexceptions
        tbuilder.setCRS(crs);
        tbuilder.add("geom", com.vividsolutions.jts.geom.Point.class);
        tbuilder.add("layer-id", String.class);

        SimpleFeatureType ftype = tbuilder.buildFeatureType();
        SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(ftype);

        DefaultFeatureCollection coll = new DefaultFeatureCollection();

        for (int i = 0; i < 10; i++) {

            // geometry can be null here
            fbuilder.add(geom.createPoint(new Coordinate(i, i)));

            fbuilder.add("id" + i);

            coll.add(fbuilder.buildFeature("fid" + i));

        }

        geopkg.add(fe, coll);

        // second feature set, generic
        FeatureEntry fe2 = new FeatureEntry();
        fe.setDataType(Entry.DataType.Feature);

        tbuilder.setName("points2");

        // geometry is mandatory to avoid nullpointerexceptions
        tbuilder.setCRS(crs);
        tbuilder.add("geom", Geometry.class);
        tbuilder.add("layer-id", String.class);

        SimpleFeatureType ftype2 = tbuilder.buildFeatureType();
        SimpleFeatureBuilder fbuilder2 = new SimpleFeatureBuilder(ftype2);

        DefaultFeatureCollection coll2 = new DefaultFeatureCollection();

        for (int i = 0; i < 10; i++) {

            // geometry can be null here
            fbuilder2.add(geom.createPoint(new Coordinate(i, i)));

            fbuilder2.add("id" + i);

            coll2.add(fbuilder2.buildFeature("fid" + i));

        }

        geopkg.add(fe2, coll2);

        // store tiles
        TileEntry e = new TileEntry();
        e.setTableName("layers");
        e.setBounds(new ReferencedEnvelope(-180, 180, -90, 90, crs));
        e.getTileMatricies().add(new TileMatrix(0, 1, 1, 256, 256, 1d, 1d));

        geopkg.create(e);

        List<Tile> tiles = new ArrayList();
        for (int i = 1; i < 4; i++) {

            Path path = Paths.get("data", "arbitrary-images", i + ".jpg");
            System.out.println(path);
            System.out.println(Files.exists(path));

            BufferedImage img = ImageIO.read(path.toFile());
            tiles.add(new Tile(0, 0, 0, MiscUtils.imageToByte(img)));
        }

        for (Tile t : tiles) {
            geopkg.add(e, t);
        }


        // add a raster, not functional for now
        RasterEntry re = new RasterEntry();
        re.setTableName("raster");
        re.setSrid(null);

        WorldImageFormat format = new WorldImageFormat();
        WorldImageReader reader = format.getReader(setUpPNG());
        GridCoverage2D cov = reader.read(null);

        RasterEntry entry = new RasterEntry();
        entry.setTableName("Pk50095");

        geopkg.add(entry, cov, format);

        GridCoverageReader r = geopkg.reader(entry, format);
        GridCoverage2D c = (GridCoverage2D) r.read(null);


        geopkg.close();


    }

    private static URL setUpPNG() throws IOException {
        File d = File.createTempFile("Pk50095", "png", new File("target"));
        d.delete();
        d.mkdirs();

//        FileUtils.copyURLToFile(TestData.url(this, "Pk50095.png"), new File(d, "Pk50095.png"));
//        FileUtils.copyURLToFile(TestData.url(this, "Pk50095.pgw"), new File(d, "Pk50095.pgw"));
        return DataUtilities.fileToURL(new File(d, "Pk50095.png"));
    }

    public static void modifyGeopkg(Path dbpath) throws IOException, FactoryException {

        Map<String, String> params = new HashMap();
        params.put("dbtype", "geopkg");
        params.put("database", dbpath.toString());

        JDBCDataStore datastore = (JDBCDataStore) DataStoreFinder.getDataStore(params);

        // create features to add

        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("point");

        // geometry is mandatory to avoid nullpointerexceptions
        tbuilder.setCRS(crs);
        tbuilder.add("geom", com.vividsolutions.jts.geom.Point.class);
        tbuilder.add("layer-id", String.class);

        SimpleFeatureType ftype = tbuilder.buildFeatureType();
        SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(ftype);

        DefaultFeatureCollection coll = new DefaultFeatureCollection();

        for (int i = 15; i < 30; i++) {

            // geometry can be null here
            fbuilder.add(geom.createPoint(new Coordinate(i, i)));

            fbuilder.add("id" + i);

            coll.add(fbuilder.buildFeature("fid" + i));

        }

        String typeName = datastore.getTypeNames()[0];
        SimpleFeatureSource source = datastore.getFeatureSource(typeName);

        System.out.println();
        System.out.println("source.getClass()");
        System.out.println(source.getClass());
        System.out.println();

        if (source instanceof SimpleFeatureStore) {
            SimpleFeatureStore store = (SimpleFeatureStore) source; // write access!
            store.addFeatures(coll);
//            store.removeFeatures(filter); // filter is like SQL WHERE
//            store.modifyFeature(attribute, value, filter);
        }

    }

    public static void readGeopkg(Path dbpath) throws IOException, FactoryException {

        GeoPackageReader reader = new GeoPackageReader(dbpath.toFile(), null);

        System.out.println("Arrays.asList(reader.getGridCoverageNames())");
        System.out.println(Arrays.asList(reader.getGridCoverageNames()));

        GridCoverage2D gc = reader.read(reader.getGridCoverageNames()[0], null);

        Map<String, String> params = new HashMap();
        params.put("dbtype", "geopkg");
        params.put("database", dbpath.toString());

        JDBCDataStore datastore = (JDBCDataStore) DataStoreFinder.getDataStore(params);

        HashMap<String, Object> informations = new HashMap<>();
        informations.put("datastore.getDatabaseSchema()", datastore.getDatabaseSchema());
        informations.put("datastore.getClassToSqlTypeMappings()", datastore.getClassToSqlTypeMappings());
        informations.put("datastore.getDataSource()", datastore.getDataSource());
        informations.put("datastore.getFilterCapabilities()", datastore.getFilterCapabilities());
        informations.put("datastore.getSQLDialect()", datastore.getSQLDialect());
        informations.put("datastore.getVirtualTables()", datastore.getVirtualTables());
        informations.put("datastore.getClassToSqlTypeMappings()", datastore.getClassToSqlTypeMappings());
        informations.put("datastore.getNames()", datastore.getNames());
        informations.put("datastore.getTypeNames()", datastore.getTypeNames());
        informations.put("datastore.class", datastore.getClass());

        Iterator<String> iter = informations.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Object val = informations.get(key);
            System.out.println();
            System.out.println(key);
            System.out.println(val);
        }

        List<Name> names = datastore.getNames();
        System.out.println("### " + names.get(0));
        SimpleFeatureSource source = datastore.getFeatureSource(names.get(0));
        SimpleFeatureIterator it = source.getFeatures().features();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
        it.close();

        // Get the bounds of features. Do not correspond to the value stored in geopackage with FeatureEntry.setBounds method
        // Stored bounds have to be manually updated
        System.out.println(source.getBounds());
        System.out.println(source.getFeatures().getBounds());

        System.out.println("### " + names.get(1));
        source = datastore.getFeatureSource(names.get(1));
        it = source.getFeatures().features();
        while (it.hasNext()) {
            System.out.println(it.next());
        }

        it.close();

        // Beware: always dispose datastores
        datastore.dispose();

        reader.dispose();

        MapContent content = new MapContent();
        content.addLayer(new GridCoverageLayer(gc, GuiUtils.getDefaultRGBRasterStyle(gc)));

        GuiUtils.showInWindow(content);
    }

    public static void jdbcLab(Path dbpath) throws IOException {

        Map params = new HashMap();
        params.put("dbtype", "geopkg");
        params.put("database", dbpath.toString());

        JDBCDataStore datastore = (JDBCDataStore) DataStoreFinder.getDataStore(params);

        Connection connection = datastore.getConnection(Transaction.AUTO_COMMIT);

        SqliteUtils.showSqliteTables(connection);

        // Beware: always dispose datastores
        datastore.dispose();
    }


}

package org.remipassmoilesel.mosaic;

import org.apache.commons.io.FileUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geopkg.GeoPackage;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.map.GridCoverageLayer;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;

import org.geotools.gce.imagemosaic.jdbc.*;
import org.remipassmoilesel.utils.GuiUtils;
import org.remipassmoilesel.utils.SqliteUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Functionnal sample of JDBC mosaic plugin with SQLite
 * <p>
 * Why in JDBC mosaic in Geopackage instead of built in tile module ?
 * <p>
 * Because I will need to set exact position of tiles, with possible overlap
 */
public class ImageMosaicJDBCLab {

    private static final Path ROOT = Paths.get("data", "image-mosaic-jdbc");
    private static final Path GENERATED_SQL_ROOT = ROOT.resolve("generated_sql");
    private static final Path DATABASE = ROOT.resolve("geopkg.sqlite");
    private static final Path ARBITRARY_TILES_ROOT = Paths.get("data/arbitrary-pictures-2");
    private static final Path CONFIG_FILE = Paths.get("data/image-mosaic-jdbc/jdbc-mosaic-config.xml");
    private static Config config;

    public static void main(String[] args) throws Exception {

        loadConfiguration();

        //createSqlTemplates();

        //createEmptyDatabase();

        //importArbitraryTiles();

        read();

    }

    /**
     * Load a JDBC mosaic configuration object from an XML file
     *
     * @throws Exception
     */
    private static void loadConfiguration() throws Exception {
        config = Config.readFrom(CONFIG_FILE.toUri().toURL());
    }

    /**
     * Read a JDBC mosaic and display it in a window
     *
     * @throws FactoryException
     * @throws IOException
     * @throws SQLException
     */
    public static void read() throws FactoryException, IOException, SQLException {

//        Connection connection = DriverManager.getConnection(config.getJdbcUrl());
//        System.out.println(connection);
//        System.out.println(SqliteUtils.getTableList(connection));

        System.out.println("Used configuration: ");
        System.out.println(config.getDataSourceParams());
        System.out.println(config);

        // First, get a reader
        // the configUrl references the config xml and is object of one of the following types
        // 1) java.net.URL
        // 2) java.io.File
        // 3) java.lang.String (A filename string or an url string)

        // if you use the original version of plugin
        //AbstractGridFormat format = GridFormatFinder.findFormat(CONFIG_FILE.toString());
        //ImageMosaicJDBCReader reader = (ImageMosaicJDBCReader) format.getReader(CONFIG_FILE.toString(), null);

        AbstractGridFormat format = GridFormatFinder.findFormat(config);

        ImageMosaicJDBCReader reader = (ImageMosaicJDBCReader) format.getReader(config, null);

        // get a parameter object for a grid geometry
        ParameterValue<GridGeometry2D> gg = AbstractGridFormat.READ_GRIDGEOMETRY2D.createValue();

        // create an envelope, 2 Points, lower left and upper right, x,y order
        GeneralEnvelope envelope = new GeneralEnvelope(new double[]{0, 0}, new double[]{3000, 3000});

        // set a CRS for the envelope
        envelope.setCoordinateReferenceSystem(DefaultEngineeringCRS.CARTESIAN_2D);

        // Set the envelope into the parameter object
        int width = 3000;
        int heigth = 3000;

        // to check: GridEnvelope2D was GeneralGridRange (unavailable)
        gg.setValue(new GridGeometry2D(new GridEnvelope2D(new Rectangle(0, 0, width, heigth)), envelope));

        // create a parameter Object for the background color (NODATA), this param is optional
        final ParameterValue outTransp = ImageMosaicJDBCFormat.BACKGROUND_COLOR.createValue();
        outTransp.setValue(Color.WHITE);

        // if you like a transparent background, use this
        // final ParameterValue outTransp=ImageMosaicJDBCFormat.OUTPUT_TRANSPARENT_COLOR.createValue();
        // outTransp.setValue(Color.WHITE);

        // params of readers cannot be null
        GridCoverage2D coverage = reader.read(new GeneralParameterValue[]{gg, outTransp});
        GridCoverageLayer layer = new GridCoverageLayer(coverage, GuiUtils.getDefaultRGBRasterStyle(reader, new GeneralParameterValue[]{gg, outTransp}));
        GuiUtils.showInWindow(layer);

    }

    /**
     * Import a bunch of arbitrary images to test jdbc mosaic plugin
     *
     * @throws Exception
     */
    public static void importArbitraryTiles() throws Exception {

        Connection connection = getDatabaseConnection();

        int x = 0;
        int y = 0;
        int mX = 400;
        int mY = 600;
        int maxX = 6;
        int i = 0;

        // iterate tiles
        Iterator<Path> diterator = Files.newDirectoryStream(ARBITRARY_TILES_ROOT).iterator();
        while (diterator.hasNext()) {
            Path f = diterator.next();

            // ignore non jpg files
            if (f.toString().toLowerCase().endsWith(".jpg") == false &&
                    f.toString().toLowerCase().endsWith(".jpeg") == false) {
                System.out.println("Ignore file: " + f.toString());
                continue;
            }

            System.out.println("Processing: " + f.toString());

            // insert tile
            insertTile(connection, f, x, y);

            i++;

            if (i < maxX) {
                x += mX;
            } else {
                x = 0;
                i = 0;
                y += mY;
            }

        }
    }

    /**
     * Insert a tile in specified database
     *
     * @param conn
     * @param img
     * @param x
     * @param y
     * @throws Exception
     */
    private static void insertTile(Connection conn, Path img, int x, int y) throws Exception {

        // check if configuration is present
        if (config == null) {
            throw new Exception("Please load configuration before");
        }

        // read image and dimensions
        BufferedImage bimg = ImageIO.read(img.toFile());
        int w = bimg.getWidth();
        int h = bimg.getHeight();

        // insert image in spatial table
        PreparedStatement spatialStat = conn.prepareStatement("INSERT INTO jdbc_mosaic_spatial_table_0 ("
                + config.getKeyAttributeNameInSpatialTable() + ","
                + config.getTileMinXAttribute() + ","
                + config.getTileMinYAttribute() + ","
                + config.getTileMaxXAttribute() + ","
                + config.getTileMaxYAttribute()
                + ") VALUES  (?,?,?,?,?)");

        String tileId = "tile_" + System.nanoTime();
        spatialStat.setString(1, tileId);
        spatialStat.setDouble(2, x);
        spatialStat.setDouble(3, y);
        spatialStat.setDouble(4, w);
        spatialStat.setDouble(5, h);

        spatialStat.execute();

        // insert image
        PreparedStatement imageStat = conn.prepareStatement("INSERT INTO jdbc_mosaic_tile_table_0 ( "
                + config.getBlobAttributeNameInTileTable() + ","
                + config.getKeyAttributeNameInTileTable()
                + " ) values (?,?)");

        imageStat.setBytes(1, imageToByteArray(img));
        imageStat.setString(2, tileId);
        imageStat.execute();
    }

    /**
     * Read an image and return a byte array
     *
     * @param image
     * @return
     * @throws IOException
     */
    public static byte[] imageToByteArray(Path image) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = Files.newInputStream(image);
        int len;
        byte[] buff = new byte[4096];

        while ((len = in.read(buff)) > 0)
            out.write(buff, 0, len);

        out.close();
        in.close();

        return out.toByteArray();
    }

    /**
     * Create an empty SQLite database
     *
     * @throws IOException
     * @throws SQLException
     */
    public static void createEmptyDatabase() throws IOException, SQLException {

        // delete previous database if needed
        Files.deleteIfExists(DATABASE);

        // create geopackage (sqlite database)
        GeoPackage geopk = new GeoPackage(DATABASE.toFile());
        geopk.init();

        Connection connection = getDatabaseConnection();

        // create master table
        SqliteUtils.runScript("/jdbcmosaic/create-master-table.sql", connection);

        // create first coverage
        SqliteUtils.runScript("/jdbcmosaic/create-coverage.sql", connection);

    }

    public static Connection getDatabaseConnection() throws IOException {

        Map params = new HashMap();
        params.put("dbtype", "geopkg");
        params.put("database", DATABASE.toFile());

        // get connection
        JDBCDataStore datastore = (JDBCDataStore) DataStoreFinder.getDataStore(params);
        Connection connection = datastore.getConnection(Transaction.AUTO_COMMIT);

        return connection;
    }

    /**
     * Create SQL templates for JDBC mosaic with provided "Import" tool
     * <p>
     * To use the DDLTool class you need to set it public (in source) and reinstall plugin with "mvn install"
     */
    public static void createSqlTemplates() throws IOException {

        // delete previous directory if needed
        FileUtils.deleteDirectory(GENERATED_SQL_ROOT.toFile());
        Files.createDirectories(GENERATED_SQL_ROOT);

        // from: http://docs.geotools.org/stable/userguide/library/coverage/jdbc/ddl.html
        // ddl  URLOrFileName -spatialTNPrefix spatialTNPrefix [-tileTNPrefix tileTNPrefix]
        // [-pyramids pyramids] -statementDelim statementDelim [-srs srs ] -targetDir
        String[] args = new String[]{
                "-config",
                CONFIG_FILE.toString(),
                "-spatialTNPrefix",
                "spatialTNPrefix",
                "-tileTNPrefix",
                "tileTNPrefix",
                "-statementDelim",
                ";",
                "-targetDir",
                GENERATED_SQL_ROOT.toString(),
        };
        Object result = null;
        //Object result = DDLGenerator.generateWithoutExit(args);

        if (result instanceof Exception) {
            System.err.println("Error while generating scripts: ");
            ((Exception) result).printStackTrace();
        } else if (result instanceof String) {
            System.err.println("Error while generating scripts: ");
            System.err.println(result);
        } else {
            System.out.println("Files generated");
        }

    }

}

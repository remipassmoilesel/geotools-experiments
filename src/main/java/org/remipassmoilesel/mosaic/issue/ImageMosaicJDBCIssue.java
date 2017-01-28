package org.remipassmoilesel.mosaic.issue;

import org.apache.commons.io.FileUtils;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.WorldFileReader;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wms.WMSUtils;
import org.geotools.data.wms.WebMapServer;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.gce.imagemosaic.jdbc.Config;
import org.geotools.gce.imagemosaic.jdbc.ImageMosaicJDBCFormat;
import org.geotools.gce.imagemosaic.jdbc.ImageMosaicJDBCReader;
import org.geotools.gce.imagemosaic.jdbc.SpatialExtension;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.*;
import org.geotools.referencing.CRS;
import org.geotools.styling.*;
import org.h2.jdbcx.JdbcConnectionPool;
import org.opengis.filter.FilterFactory2;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.style.ContrastMethod;
import org.remipassmoilesel.utils.GuiUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

/**
 * Created by remipassmoilesel on 26/01/17.
 */
public class ImageMosaicJDBCIssue {

    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private static final StyleFactory sf = CommonFactoryFinder.getStyleFactory();

    static {
        // force axis order to prevent display issues
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    public static void main(String[] args) throws Exception {

        String wmsUrl = "http://ows.mundialis.de/services/service";
        Path databaseFolder = Paths.get("data/mosaic-issues");
        Path imagePath = Paths.get("data/jdbc-mosaic-sample/map.tif");
        String shapeFilePath = "data/ne_50m_admin/ne_50m_admin_0_countries0_repro.shp";

        WMSLayer wmsLayer = buildWmsLayer(wmsUrl);
        FeatureLayer shapefileLayer = buildShapeFileLayer(shapeFilePath);
        org.geotools.map.Layer rasterLayer = buildRasterLayer(imagePath);
        GridCoverageLayer mosaicLayer = buildMosaicLayer(databaseFolder, imagePath);

        // create a map content and display it
        MapContent mapContent = new MapContent();

        mapContent.addLayer(wmsLayer);
        mapContent.addLayer(shapefileLayer);
        mapContent.addLayer(rasterLayer);
        mapContent.addLayer(mosaicLayer);

        /*
        System.out.println();
        System.out.println("rasterLayer.getBounds()");
        System.out.println(rasterLayer.getBounds());
        System.out.println("mosaicLayer.getBounds()");
        System.out.println(mosaicLayer.getBounds());
        */

        GuiUtils.showInWindowAndWait(mapContent);

    }

    /**
     * Create a JDBC mosaic database and layer
     *
     * @param databaseFolder
     * @param imagePath
     * @return
     * @throws Exception
     */
    public static GridCoverageLayer buildMosaicLayer(Path databaseFolder, Path imagePath) throws Exception {

        // delete previous folder if exist
        if (Files.isDirectory(databaseFolder) == true) {
            FileUtils.deleteDirectory(databaseFolder.toFile());
        }
        Files.createDirectories(databaseFolder);

        // build a database and a connection
        Path databasePath = databaseFolder.resolve("mosaic.h2");
        String databaseUrl = getJdbcUrlForH2(databasePath.toAbsolutePath());
        JdbcConnectionPool pool = JdbcConnectionPool.create(databaseUrl, "", "");
        Connection conn = pool.getConnection();

        // create main database scheme
        PreparedStatement createMaster = TileStorageQueries.createMasterTableIfNotExist(conn);
        createMaster.execute();
        createMaster.close();

        // names of tables
        String coverageName = "firstcoverage";
        String dataTableName = coverageName + "_data";
        String spatialTableName = coverageName + "_spatial";

        // create master table entry for coverage
        PreparedStatement masterEntryStat = TileStorageQueries.insertIntoMasterTable(conn);
        masterEntryStat.setString(1, coverageName);
        masterEntryStat.setString(2, dataTableName);
        masterEntryStat.setString(3, spatialTableName);

        masterEntryStat.execute();
        masterEntryStat.close();

        // create spatial table
        PreparedStatement spatStat = TileStorageQueries.createSpatialTable(conn, spatialTableName);
        spatStat.execute();
        spatStat.close();

        // create data table
        PreparedStatement dataStat = TileStorageQueries.createDataTable(conn, dataTableName);
        dataStat.execute();
        dataStat.close();

        // create indexes
        PreparedStatement indexStat = TileStorageQueries.createIndexes(conn, spatialTableName);
        indexStat.execute();
        indexStat.close();

        // read image sample
        BufferedImage diskImage = ImageIO.read(imagePath.toFile());
        String tileId = "tileid1";

        // show image to check it
        //GuiUtils.showImage(bimg);

        WorldFileReader worldFile = new WorldFileReader(Paths.get("data/jdbc-mosaic-sample/map.tfw").toFile());

        /*
        // these values display image in a good way, but not at a good place
        String crsCode = "EPSG:404000";
        double minx = 0;
        double miny = 0;
        double maxx = diskImage.getWidth();
        double maxy = diskImage.getHeight();
        */

        String crsCode = "EPSG:4326";
        double minx = worldFile.getXULC();
        double maxx = worldFile.getXULC() + worldFile.getXPixelSize() * diskImage.getWidth();
        double miny = worldFile.getYULC() + worldFile.getYPixelSize() * diskImage.getHeight();
        double maxy = worldFile.getYULC();

        ReferencedEnvelope bounds = new ReferencedEnvelope(minx, maxx, miny, maxy, CRS.decode(crsCode));

        /*
        System.out.println("mosaic bounds");
        System.out.println(bounds);
        */

        // insert image in database
        PreparedStatement imgStat1 = TileStorageQueries.insertIntoDataTable(conn, dataTableName);

        imgStat1.setString(1, tileId);
        imgStat1.setBytes(2, imageToByte(diskImage));
        imgStat1.execute();
        imgStat1.close();

        PreparedStatement imgStat2 = TileStorageQueries.insertIntoSpatialTable(conn, spatialTableName);

        imgStat2.setString(1, tileId);
        imgStat2.setDouble(2, minx);
        imgStat2.setDouble(3, miny);
        imgStat2.setDouble(4, maxx);
        imgStat2.setDouble(5, maxy);
        imgStat2.execute();
        imgStat2.close();

        // check how image is in database
        PreparedStatement imgSelect = TileStorageQueries.selectLastTiles(conn, dataTableName, spatialTableName);
        imgSelect.setInt(1, 0);
        imgSelect.setInt(2, 1);
        ResultSet result = imgSelect.executeQuery();
        result.next();

        BufferedImage dbImage = bytesToImage(result.getBytes(2));
        //GuiUtils.showImage(dbImage);
        if (dbImage.getWidth() != diskImage.getWidth() ||
                dbImage.getHeight() != diskImage.getHeight()) {
            throw new IllegalStateException("Images are different: \n" + diskImage + "\n " + dbImage);
        }

        result.close();
        imgSelect.close();

        // I use a custom jdbc mosaic extension modifed in order to allow instantiation of Config object
        // Unit testing confirm that there is no influence from that, but you can check that by using this configuration file
        File configFile = new File("src/main/java/org/remipassmoilesel/mosaic/issue/config.xml");

        // create a configuration for layer
        // Config config = getConfiguration(databasePath, coverageName, crsCode);

        // create layer
        AbstractGridFormat format = GridFormatFinder.findFormat(configFile);
        Hints hints = null; //new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, CRS.decode("EPSG:4326"));
        ImageMosaicJDBCReader reader = (ImageMosaicJDBCReader) format.getReader(configFile, hints);

        ParameterValue<GridGeometry2D> gg = AbstractGridFormat.READ_GRIDGEOMETRY2D.createValue();

        double width = bounds.getWidth();
        double height = bounds.getHeight();
        GeneralEnvelope envelope = reader.getOriginalEnvelope();
        gg.setValue(new GridGeometry2D(new GridEnvelope2D(new Rectangle(0, 0, (int) width, (int) height)), envelope));

        final ParameterValue<Color> bgColor = ImageMosaicJDBCFormat.BACKGROUND_COLOR.createValue();
        bgColor.setValue(Color.red);

        GeneralParameterValue[] params = new GeneralParameterValue[]{gg, bgColor};
        GridCoverage2D coverage = reader.read(params);

        if (coverage == null) {
            throw new NullPointerException("Unable to read coverage with specified params: " + coverageName + " / " + params);
        }

        GridCoverageLayer mosaicLayer = new GridCoverageLayer(coverage, createDefaultRGBStyle(coverage));

        return mosaicLayer;
    }

    /**
     * Build a simple shape file layer
     *
     * @return
     * @throws IOException
     */
    public static FeatureLayer buildShapeFileLayer(String shapeFilePath) throws IOException {
        File shapeFile = new File(shapeFilePath);
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shapeFile);
        SimpleFeatureSource shapeFileSource = dataStore.getFeatureSource();

        FeatureLayer shapeLayer = new FeatureLayer(shapeFileSource, SLD.createLineStyle(Color.darkGray, 0.2f));
        return shapeLayer;
    }

    /**
     * Build a simple raster layer
     *
     * @param imagePath
     * @return
     * @throws IOException
     */
    public static org.geotools.map.Layer buildRasterLayer(Path imagePath) throws Exception {

        // If we do not force CRS here, layer will not be displayed over WMS layer
        String crsCode = "EPSG:4326";
        Hints hints = new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, CRS.decode(crsCode));

        File rasterFile = imagePath.toFile();
        AbstractGridFormat format = GridFormatFinder.findFormat(rasterFile);
        AbstractGridCoverage2DReader reader = format.getReader(rasterFile, hints);
        GridCoverage2D coverage = reader.read(null);

        if (coverage == null) {
            throw new IOException("Unable to read coverage: " + imagePath);
        }

        //Style style = createGreyscaleStyle(1);
        Style style = createDefaultRGBStyle(coverage);
        GridReaderLayer rasterLayer = new GridReaderLayer(reader, style);

        /*
        for (GridSampleDimension dim : coverage.getSampleDimensions()) {
            System.out.println("GridSampleDimension dim");
            System.out.println(dim);
        }
        */

        return rasterLayer;
    }

    /**
     * Create a simple WMS layer
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static WMSLayer buildWmsLayer(String url) throws Exception {

        WebMapServer webMapServer = new WebMapServer(new URL(url));
        WMSCapabilities capabilities = webMapServer.getCapabilities();
        java.util.List<Layer> namedLayers = Arrays.asList(WMSUtils.getNamedLayers(capabilities));

        Layer wmsLayer = namedLayers.get(0);

        return new WMSLayer(webMapServer, wmsLayer);
    }

    /**
     * Utility used to convert images to byte arrays
     *
     * @param img
     * @return
     */
    public static byte[] imageToByte(BufferedImage img) {

        try {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                ImageIO.write(img, "png", out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Return a buffered image generated from byte array or null if an error occur
     *
     * @param bytes
     * @return
     */
    public static BufferedImage bytesToImage(byte[] bytes) {

        try {
            try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
                return ImageIO.read(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * Generate a JDBC mosaic configuration
     *
     * @return
     */
    private static Config getConfiguration(Path databasePath, String coverageName, String crsCode) {

        /*

        // Uncomment if you can use custom JDBC mosaic plugin available at: https://github.com/remipassmoilesel/geotools

        // instantiate configuration
        Config config = new Config("org." + coverageName + "_" + System.nanoTime());

        // General config
        config.setCoverageName(coverageName);
        config.setCoordsys(crsCode);
        // interpolation 1 = nearest neighbour, 2 = bipolar, 3 = bicubic
        config.setInterpolation(1);
        config.setIgnoreAxisOrder(false);
        config.setVerifyCardinality(true);

        // Database config
        config.setDstype("DBCP");
        config.setUsername("");
        config.setPassword("");
        config.setJdbcUrl(getJdbcUrlForH2(databasePath));
        config.setDriverClassName("org.h2.Driver");
        config.setMaxActive(5);
        config.setMaxIdle(10);
        config.setSpatialExtension(SpatialExtension.fromString("universal"));

        // master table
        config.setMasterTable(TileStorageQueries.MASTER_TABLE_NAME);
        config.setCoverageNameAttribute(TileStorageQueries.COVERAGE_NAME_FIELD_NAME);
        config.setTileMinXAttribute(TileStorageQueries.MIN_X_FIELD_NAME);
        config.setTileMinYAttribute(TileStorageQueries.MIN_Y_FIELD_NAME);
        config.setTileMaxXAttribute(TileStorageQueries.MAX_X_FIELD_NAME);
        config.setTileMaxYAttribute(TileStorageQueries.MAX_Y_FIELD_NAME);
        config.setTileTableNameAtribute(TileStorageQueries.TILE_TABLE_NAME_FIELD_NAME);
        config.setSpatialTableNameAtribute(TileStorageQueries.SPATIAL_TABLE_NAME_FIELD_NAME);
        config.setResXAttribute(TileStorageQueries.RES_X_FIELD_NAME);
        config.setResYAttribute(TileStorageQueries.RES_Y_FIELD_NAME);

        // tile table
        config.setBlobAttributeNameInTileTable(TileStorageQueries.TILE_DATA_FIELD_NAME);
        config.setKeyAttributeNameInTileTable(TileStorageQueries.TILE_ID_FIELD_NAME);

        // spatial table
        config.setKeyAttributeNameInSpatialTable(TileStorageQueries.TILE_ID_FIELD_NAME);
        config.setMinXAttribute(TileStorageQueries.MIN_X_FIELD_NAME);
        config.setMinYAttribute(TileStorageQueries.MIN_Y_FIELD_NAME);
        config.setMaxXAttribute(TileStorageQueries.MAX_X_FIELD_NAME);
        config.setMaxYAttribute(TileStorageQueries.MAX_Y_FIELD_NAME);

        config.validateConfig();

        return config;

        */

        return null;
    }

    public static org.geotools.styling.Style createDefaultRGBStyle(GridCoverage2D cov) {

        // We need at least three bands to create an RGB style
        int numBands = cov.getNumSampleDimensions();
        if (numBands < 3) {
            throw new IllegalStateException("Need more bands to make an RGB layerStyle: " + numBands);
        }

        // Get the names of the bands
        String[] sampleDimensionNames = new String[numBands];
        for (int i = 0; i < numBands; i++) {
            GridSampleDimension dim = cov.getSampleDimension(i);
            sampleDimensionNames[i] = dim.getDescription().toString();
        }

        final int RED = 0, GREEN = 1, BLUE = 2;
        int[] channelNum = {-1, -1, -1};
        // We examine the band names looking for "red...", "green...", "blue...".
        // Note that the channel numbers we record are indexed from 1, not 0.
        for (int i = 0; i < numBands; i++) {
            String name = sampleDimensionNames[i].toLowerCase();
            if (name != null) {
                if (name.matches("red.*")) {
                    channelNum[RED] = i + 1;
                } else if (name.matches("green.*")) {
                    channelNum[GREEN] = i + 1;
                } else if (name.matches("blue.*")) {
                    channelNum[BLUE] = i + 1;
                }
            }
        }

        // If we didn't find named bands "red...", "green...", "blue..."
        // we fall back to using the first three bands in order
        if (channelNum[RED] < 0 || channelNum[GREEN] < 0 || channelNum[BLUE] < 0) {
            channelNum[RED] = 1;
            channelNum[GREEN] = 2;
            channelNum[BLUE] = 3;
        }

        // Now we create a RasterSymbolizer using the selected channels
        SelectedChannelType[] sct = new SelectedChannelType[cov.getNumSampleDimensions()];
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NONE);
        for (int i = 0; i < 3; i++) {
            sct[i] = sf.createSelectedChannelType(String.valueOf(channelNum[i]), ce);
        }
        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN], sct[BLUE]);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

    private static Style createGreyscaleStyle(int band) {

        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        SelectedChannelType sct = sf.createSelectedChannelType(String.valueOf(band), ce);

        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

    private static String getJdbcUrlForH2(Path databasePath) {
        return "jdbc:h2:file:" + databasePath.toAbsolutePath().toString();
    }


}

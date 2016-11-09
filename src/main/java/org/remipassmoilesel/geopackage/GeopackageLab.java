package org.remipassmoilesel.geopackage;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.*;
import org.geotools.geopkg.mosaic.GeoPackageReader;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.remipassmoilesel.utils.GuiUtils;
import org.remipassmoilesel.utils.MiscUtils;
import org.remipassmoilesel.utils.SqlUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
    public static final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

    public static void main(String[] args) throws IOException, FactoryException, SQLException {

        Path dbpath = Paths.get("data/geopk.db");

        // create a geopackage
        cleanExistingDatabase(dbpath);
        createGeopkg(dbpath);

        // display a geopackage
//        readGeopkg(dbpath);

        // misc trials on jdbc
        //jdbcLab(dbpath);

        // add project metadatas
        addProjectMetadatas(dbpath);

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

    public static void cleanExistingDatabase(Path dbpath) throws IOException {
        // clean previous database
        if (Files.exists(dbpath)) {
            Files.delete(dbpath);
        }
    }

    public static void createGeopkg(Path dbpath) throws IOException {

        Files.createFile(dbpath);

        GeoPackage geopkg = new GeoPackage(dbpath.toFile());
        geopkg.init();

        GeometryFactory geom = JTSFactoryFinder.getGeometryFactory();

        // store features
        FeatureEntry fe = new FeatureEntry();
        fe.setDataType(Entry.DataType.Feature);
        fe.setTableName("points");

        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("point");

        // geometry is mandatory to avoid nullpointerexceptions
        tbuilder.setCRS(crs);
        tbuilder.add("geom", com.vividsolutions.jts.geom.Point.class);
        tbuilder.add("layer-id", String.class);
        tbuilder.add("layer-name", String.class);


        SimpleFeatureType ftype = tbuilder.buildFeatureType();
        SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(ftype);

        DefaultFeatureCollection coll = new DefaultFeatureCollection();

        for (int i = 0; i < 10; i++) {

            // geometry can be null here
            fbuilder.add(geom.createPoint(new Coordinate(i, i)));

            fbuilder.add("id" + i);
            fbuilder.add("Layer name " + i);

            coll.add(fbuilder.buildFeature("fid" + i));

        }

        geopkg.add(fe, coll);

        // store tiles
        TileEntry e = new TileEntry();
        e.setTableName("layer_1");
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

        geopkg.close();

    }

    public static void readGeopkg(Path dbpath) throws IOException, FactoryException {

        GeoPackageReader reader = new GeoPackageReader(dbpath.toFile(), null);
        System.out.println(Arrays.asList(reader.getGridCoverageNames()));

        GridCoverage2D gc = reader.read(reader.getGridCoverageNames()[0], null);

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

        SqlUtils.showSqliteTables(connection);
    }


}

package org.remipassmoilesel.geopackage;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.GeoPackage;
import org.geotools.geopkg.Tile;
import org.geotools.geopkg.TileEntry;
import org.geotools.geopkg.TileMatrix;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.remipassmoilesel.utils.MiscUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by remipassmoilesel on 07/11/16.
 */
public class GeopackageLab {

    public static void main(String[] args) throws IOException {

        Path dbpath = Paths.get("data/geopk.db");

        // clean previous database
        if (Files.exists(dbpath)) {
            Files.delete(dbpath);
        }

        Files.createFile(dbpath);

        GeoPackage geopkg = new GeoPackage(dbpath.toFile());
        geopkg.init();

        TileEntry e = new TileEntry();
        e.setTableName("foo");
        e.setBounds(new ReferencedEnvelope(-180, 180, -90, 90, DefaultGeographicCRS.WGS84));
        e.getTileMatricies().add(new TileMatrix(0, 1, 1, 256, 256, 0.1, 0.1));
        e.getTileMatricies().add(new TileMatrix(1, 2, 2, 256, 256, 0.1, 0.1));

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


}

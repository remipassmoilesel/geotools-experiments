package org.remipassmoilesel.worldfile;

import org.geotools.data.WorldFileWriter;
import org.opengis.referencing.crs.CRSFactory;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static java.nio.file.Files.newDirectoryStream;

/**
 * Created by remipassmoilesel on 28/10/16.
 */
public class WorldFileLab {

    /**
     * Create a world file to move a potential picture on world, as a raster object for example.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // simple world file
        AffineTransform translate = new AffineTransform(5, 0, 0, 5, 492169, 5426523);
        new WorldFileWriter(new File("data/generated.world"), translate);

        // for tiles in data
        String root = "data/arbitrary_images/";

        DirectoryStream<Path> dir = Files.newDirectoryStream(Paths.get(root));
        Iterator<Path> iter = dir.iterator();
        double x = 0;
        double y = 0;
        while (iter.hasNext()) {

            Path elmt = iter.next();
            TileWorldFileWriter.createForTile(elmt, x, y);

            x += 50;
            y += 20;
        }

    }
}

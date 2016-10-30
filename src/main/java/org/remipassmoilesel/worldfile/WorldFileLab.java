package org.remipassmoilesel.worldfile;

import com.google.common.primitives.Chars;
import org.geotools.data.WorldFileWriter;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.wkt.Formattable;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static java.nio.file.Files.newBufferedWriter;
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
    public static void main(String[] args) throws IOException, FactoryException {

//        // simple world file
//        AffineTransform translate = new AffineTransform(5, 0, 0, 5, 492169, 5426523);
//        new WorldFileWriter(new File("data/generated.world"), translate);
//
//        // process a directory
//        processDirectory("data/arbitrary-images");

        // print a CRS as a prj file
        printPrjForCode("data/generated.prj", "EPSG:4326");

    }

    public static void processDirectory(String root) throws IOException {

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

    public static void printPrjForCode(String destination, String epsgCode) throws IOException, FactoryException {

        Path dest = Paths.get(destination);

        // write WKT as a single line
        try(BufferedWriter writer = Files.newBufferedWriter(dest, Charset.forName("utf-8"))){
            Formattable f = (Formattable) CRS.decode(epsgCode, true);
            String wkt = f.toWKT(0); // use 0 indent for single line
            writer.write(wkt);
        }

    }

    public static void printEsriPrjForCode(String destination, String epsgCode) throws IOException, FactoryException {

        Path dest = Paths.get(destination);

        // write WKT as a single line
        try(BufferedWriter writer = Files.newBufferedWriter(dest, Charset.forName("utf-8"))){
            Formattable f = (Formattable) CRS.decode(epsgCode, true);
            String wkt = f.toWKT(Citations.ESRI, 0); // use 0 indent for single line
            writer.write(wkt);
        }

    }
}

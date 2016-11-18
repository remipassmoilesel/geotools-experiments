package org.remipassmoilesel.worldfile;

import org.apache.commons.io.FileUtils;
import org.geotools.geometry.jts.WKTWriter2;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.wkt.Formattable;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Writing a CRS as a WKT string
 *
 */
public class WktWriterLab {

    private static final Path ROOT = Paths.get("data/crs");

    public static void main(String[] args) throws FactoryException, IOException {

        // delete previous files
        FileUtils.deleteDirectory(ROOT.toFile());
        Files.createDirectories(ROOT);

        String code = "EPSG:4326";

        CoordinateReferenceSystem crs = CRS.decode(code);
        String wkt = crs.toWKT();

        // display reference system
        System.out.println("WKT for: " + code);
        System.out.println(wkt);

        // write on disk
        public static void printPrjForCode(String destination, String epsgCode) throws IOException, FactoryException {

            Path dest = Paths.get(destination);

            // write WKT as a single line
            try(BufferedWriter writer = Files.newBufferedWriter(dest, Charset.forName("utf-8"))){
                Formattable f = (Formattable) CRS.decode(epsgCode, true);
                String wkt = f.toWKT(0); // use 0 indent for single line
                writer.write(wkt);
            }

        }
        try(BufferedWriter writer = Files.newBufferedWriter(dest, Charset.forName("utf-8"))){
            Formattable f = (Formattable) CRS.decode(epsgCode, true);
            String wkt = f.toWKT(Citations.ESRI, 0); // use 0 indent for single line
            writer.write(wkt);
        }
        new
        BufferedWriter writer = Files.newWriter(new File("data/EPSG4326.wkt"), Charset.forName("utf-8"));
        writer.write(wkt, 0, wkt.length());
        writer.close();

    }



}

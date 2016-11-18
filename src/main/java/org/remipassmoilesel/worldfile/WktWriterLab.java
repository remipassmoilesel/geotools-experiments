package org.remipassmoilesel.worldfile;

import org.apache.commons.io.FileUtils;
import org.geotools.referencing.CRS;
import org.geotools.referencing.wkt.Formattable;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Writing a CRS as a WKT string
 * <p>
 * See WorldFileLab for writing PRJ files
 */
public class WktWriterLab {

    private static final Path ROOT = Paths.get("data/crs");

    public static void main(String[] args) throws FactoryException, IOException {

        // delete previous files
        FileUtils.deleteDirectory(ROOT.toFile());
        Files.createDirectories(ROOT);

        String code = "EPSG:4326";

        Path dest = ROOT.resolve(code + ".txt");

        CoordinateReferenceSystem crs = CRS.decode(code);
        String wkt = crs.toWKT();

        // display reference system
        System.out.println("WKT for: " + code);
        System.out.println(wkt);

        // write WKT as a single line
        try (BufferedWriter writer = Files.newBufferedWriter(dest, Charset.forName("utf-8"))) {
            Formattable f = (Formattable) CRS.decode(code, true);
            writer.write(wkt);
        }

    }


}

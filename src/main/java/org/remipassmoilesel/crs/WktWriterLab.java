package org.remipassmoilesel.crs;

import com.google.common.io.Files;
import org.geotools.geometry.jts.WKTWriter2;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Writing a CRS as a WKT string
 *
 */
public class WktWriterLab {

    public static void main(String[] args) throws FactoryException, IOException {

        String code = "EPSG:4326";

        CoordinateReferenceSystem crs = CRS.decode(code);
        String wkt = crs.toWKT();

        // display reference system
        System.out.println("wkt for: " + code);
        System.out.println(wkt);

        // write on disk
        BufferedWriter writer = Files.newWriter(new File("data/EPSG4326.wkt"), Charset.forName("utf-8"));
        writer.write(wkt, 0, wkt.length());
        writer.close();

    }

}

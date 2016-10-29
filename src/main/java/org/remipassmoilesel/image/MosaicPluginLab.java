package org.remipassmoilesel.image;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.gce.imagemosaic.ImageMosaicFormatFactory;
import org.geotools.gce.imagemosaic.ImageMosaicReader;
import org.geotools.gce.imagepyramid.ImagePyramidFormat;
import org.geotools.gce.imagepyramid.ImagePyramidFormatFactory;
import org.geotools.gce.imagepyramid.ImagePyramidReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.GridReaderLayer;
import org.geotools.map.RasterLayer;
import org.geotools.styling.SLD;
import org.remipassmoilesel.utils.GuiBuilder;
import org.remipassmoilesel.utils.GuiUtils;
import org.remipassmoilesel.worldfile.TileWorldFileWriter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.remipassmoilesel.worldfile.TileWorldFileWriter.createForTile;

/**
 * Created by remipassmoilesel on 29/10/16.
 */
public class MosaicPluginLab {

    public static void main(String[] args) {


        ImageMosaicFormatFactory factory = new ImageMosaicFormatFactory();
        ImageMosaicFormat format = (ImageMosaicFormat) factory.createFormat();
        ImageMosaicReader reader = format.getReader(new File("data/mosaic_sample/mosaic.shp"));

//        ImagePyramidFormatFactory factory = new ImagePyramidFormatFactory();
//        ImagePyramidFormat format = factory.createFormat();
//        ImagePyramidReader reader = format.getReader(new File("data/mosaic_sample/"));

        GuiBuilder.newMap("Mosaic").addLayer(new GridReaderLayer(reader, GuiUtils.getDefaultRasterStyle(reader))).show();


    }


    /**
     * Create a shapefile with tiles arbitrary positionned
     *
     * @throws IOException
     */
    public static void createShapeFile() throws IOException {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Images");
        builder.add("location", String.class);

        String root = "data/arbitrary_images/";

        DirectoryStream<Path> dir = Files.newDirectoryStream(Paths.get(root));
        Iterator<Path> iter = dir.iterator();
        double x = 0;
        double y = 0;
        while (iter.hasNext()) {

            Path elmt = iter.next();
            createFeatureForTile(elmt, x, y);

            x += 50;
            y += 20;
        }

    }

    public static void createFeatureForTile(Path name, double x, double y) throws IOException {

    }

}

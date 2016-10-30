package org.remipassmoilesel.image;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.gce.imagemosaic.ImageMosaicFormatFactory;
import org.geotools.gce.imagemosaic.ImageMosaicReader;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.Layer;
import org.geotools.referencing.CRS;
import org.geotools.styling.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.style.ContrastMethod;
import org.remipassmoilesel.utils.GuiBuilder;
import org.remipassmoilesel.utils.GuiUtils;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by remipassmoilesel on 29/10/16.
 */
public class MosaicPluginLab {

    public static void main(String[] args) throws IOException, FactoryException {

//        writeMosaic("data/geoserver-mosaic-sample", "data/generated.jpg");
        showMosaic("data/geoserver-mosaic-sample");

    }

    /**
     * Read a mosaic and write it to an image
     *
     * @param pathSrc
     * @param pathDest
     * @throws IOException
     */
    public static void writeMosaic(String pathSrc, String pathDest) throws IOException {

        ImageMosaicFormatFactory factory = new ImageMosaicFormatFactory();
        ImageMosaicFormat format = (ImageMosaicFormat) factory.createFormat();
        ImageMosaicReader reader = format.getReader(new File(pathSrc));

        GridCoverage2D coverage = reader.read(null);

        RenderedImage image = coverage.getRenderedImage();
        ImageIO.write(image, "jpg", new File(pathDest));

    }

    /**
     * Read a mosaic and display it on a window
     *
     * @param pathSrc
     * @throws IOException
     */
    public static void showMosaic(String pathSrc) throws IOException {

        ImageMosaicFormatFactory factory = new ImageMosaicFormatFactory();
        ImageMosaicFormat format = (ImageMosaicFormat) factory.createFormat();
        ImageMosaicReader reader = format.getReader(new File(pathSrc));

        GridCoverage2D coverage = reader.read(null);

        Layer rasterLayer = new GridCoverageLayer(coverage, GuiUtils.getDefaultRGBRasterStyle(reader));

        GuiBuilder.newMap("Mosaic: " + pathSrc).addLayer(rasterLayer).show();

    }

}

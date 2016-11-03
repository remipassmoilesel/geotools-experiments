package org.remipassmoilesel.coverageprocessor;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.processing.CoverageProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.resources.Arguments;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;

/**
 * The ImageLab tutorial covered loading and rendering coverages; this tutorial will demonstrate performing basic
 * operations – such as crop and scale – directly on a coverage using the CoverageProcessor and friends, as well as use
 * the Arguments tool to make command line processing a little simpler. We will be creating a simple utility application to
 * “tile” a coverage (for the sake of simplicity simply subdividing the envelope) and optionally scaling the resulting tiles.
 * <p>
 * Arguments sample: -f data/NE2_50M_SR/NE2_50M_SR.tif -htc 16 -vtc 8 -o data/tiled -scale 2.0
 * <p>
 * Simple tiling of a coverage based simply on the number vertical/horizontal tiles desired and
 * subdividing the geographic envelope. Uses coverage processing operations.
 */
public class ImageTiler {

    private final int NUM_HORIZONTAL_TILES = 16;
    private final int NUM_VERTICAL_TILES = 8;

    private Integer numberOfHorizontalTiles = NUM_HORIZONTAL_TILES;
    private Integer numberOfVerticalTiles = NUM_VERTICAL_TILES;
    private Double tileScale;
    private File inputFile;
    private File outputDirectory;

    /**
     * Main method for command Line utility
     * <p>
     * GeoTools provides utility classes to parse command line arguments: Arguments
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Arguments processedArgs = new Arguments(args);
        ImageTiler tiler = new ImageTiler();

        try {
            tiler.setInputFile(new File(processedArgs.getRequiredString("-f")));
            tiler.setOutputDirectory(new File(processedArgs.getRequiredString("-o")));
            tiler.setNumberOfHorizontalTiles(processedArgs.getOptionalInteger("-htc"));
            tiler.setNumberOfVerticalTiles(processedArgs.getOptionalInteger("-vtc"));
            tiler.setTileScale(processedArgs.getOptionalDouble("-scale"));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            printUsage();
            System.exit(1);
        }

        tiler.tile();
    }

    private static void printUsage() {
        System.out.println("Usage: -f inputFile -o outputDirectory [-tw tileWidth<default:256> "
                + "-th tileHeight<default:256> ");
        System.out.println("-htc horizontalTileCount<default:16> -vtc verticalTileCount<default:8>");
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    private void tile() throws IOException {

        // First we need to load the coverage; GeoTools provides GridFormatFinder and AbstractGridFormat
        // in order to do this abstractly. Note: there is a slight quirk with GeoTiff
        // handling as of this writing that we handle separately.
        AbstractGridFormat format = GridFormatFinder.findFormat(this.getInputFile());
        String fileExtension = this.getFileExtension(this.getInputFile());

        //working around a bug/quirk in geotiff loading via format.getReader which doesn't set this
        //correctly
        Hints hints = null;
        if (format instanceof GeoTiffFormat) {
            hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        }

        GridCoverage2DReader gridReader = format.getReader(
                this.getInputFile(),
                hints);
        GridCoverage2D gridCoverage = gridReader.read(null);

        // Next we’ll subdivide the coverage based on the requested horizontal and vertical tile counts by asking the
        // coverage for its envelope and dividing that envelope horizontally and vertically by our tile counts. This will give
        // us our tile envelope width and height. Then we’ll loop over our horizontal and vertical tile counts to crop and scale.
        Envelope2D coverageEnvelope = gridCoverage.getEnvelope2D();
        double coverageMinX = coverageEnvelope.getBounds().getMinX();
        double coverageMaxX = coverageEnvelope.getBounds().getMaxX();
        double coverageMinY = coverageEnvelope.getBounds().getMinY();
        double coverageMaxY = coverageEnvelope.getBounds().getMaxY();

        int htc = this.getNumberOfHorizontalTiles() != null
                ? this.getNumberOfHorizontalTiles() : NUM_HORIZONTAL_TILES;
        int vtc = this.getNumberOfVerticalTiles() != null
                ? this.getNumberOfVerticalTiles() : NUM_VERTICAL_TILES;

        double geographicTileWidth = (coverageMaxX - coverageMinX) / (double) htc;
        double geographicTileHeight = (coverageMaxY - coverageMinY) / (double) vtc;

        CoordinateReferenceSystem targetCRS = gridCoverage.getCoordinateReferenceSystem();

        //make sure to create our output directory if it doesn't already exist
        File tileDirectory = this.getOutputDirectory();
        if (!tileDirectory.exists()) {
            tileDirectory.mkdirs();
        }

        //iterate over our tile counts
        for (int i = 0; i < htc; i++) {
            for (int j = 0; j < vtc; j++) {

                System.out.println("Processing tile at indices i: " + i + " and j: " + j);
                //create the envelope of the tile
                Envelope envelope = getTileEnvelope(coverageMinX, coverageMinY, geographicTileWidth,
                        geographicTileHeight, targetCRS, i, j);

                GridCoverage2D finalCoverage = cropCoverage(gridCoverage, envelope);

                if (this.getTileScale() != null) {
                    finalCoverage = scaleCoverage(finalCoverage);
                }

                //use the AbstractGridFormat's writer to write out the tile
                File tileFile = new File(tileDirectory, i + "_" + j + "." + fileExtension);
                format.getWriter(tileFile).write(finalCoverage, null);
            }
        }

    }

    /**
     *  Cropping
     *  Now that we have the tile envelope width and height we’ll iterate over our tile counts and crop based on
     *  our target envelope. In this example we will manually create our parameters and use the coverage processor
     *  to perform the “CoverageCrop” operation. We’ll encounter slightly simpler ways to perform coverage operations in the next step.
     */
    private GridCoverage2D cropCoverage(GridCoverage2D gridCoverage, Envelope envelope) {
        CoverageProcessor processor = CoverageProcessor.getInstance();

        //An example of manually creating the operation and parameters we want
        final ParameterValueGroup param = processor.getOperation("CoverageCrop").getParameters();
        param.parameter("Source").setValue(gridCoverage);
        param.parameter("Envelope").setValue(envelope);

        return (GridCoverage2D) processor.doOperation(param);
    }

    /**
     * Creating our tile envelope
     * We’ll create the envelope of our tile based on our indexes and target enveloped width and height:
     *
     * @param coverageMinX
     * @param coverageMinY
     * @param geographicTileWidth
     * @param geographicTileHeight
     * @param targetCRS
     * @param horizontalIndex
     * @param verticalIndex
     * @return
     */
    private Envelope getTileEnvelope(double coverageMinX, double coverageMinY,
                                     double geographicTileWidth, double geographicTileHeight,
                                     CoordinateReferenceSystem targetCRS, int horizontalIndex, int verticalIndex) {

        double envelopeStartX = (horizontalIndex * geographicTileWidth) + coverageMinX;
        double envelopeEndX = envelopeStartX + geographicTileWidth;
        double envelopeStartY = (verticalIndex * geographicTileHeight) + coverageMinY;
        double envelopeEndY = envelopeStartY + geographicTileHeight;

        return new ReferencedEnvelope(
                envelopeStartX, envelopeEndX, envelopeStartY, envelopeEndY, targetCRS);
    }


    /**
     * Scaling
     * <p>
     * We can use the “Scale” operation to optionally scale our tiles. In this example we’ll use the Operations class to make our lives a little easier. This class wraps operations and provides a slightly more type safe interface to them. Here we will scale our X and Y dimensions by the same factor in order to preserve the aspect ratio of our original coverage.
     * <p>
     * As an alternative to using parameters to do the operations, we can use the
     * Operations class to do them in a slightly more type safe way.
     *
     * @param coverage the coverage to scale
     * @return the scaled coverage
     */
    private GridCoverage2D scaleCoverage(GridCoverage2D coverage) {
        Operations ops = new Operations(null);
        coverage = (GridCoverage2D) ops.scale(
                coverage, this.getTileScale(), this.getTileScale(), 0, 0);
        return coverage;
    }

    public Integer getNumberOfHorizontalTiles() {
        return numberOfHorizontalTiles;
    }

    public void setNumberOfHorizontalTiles(Integer numberOfHorizontalTiles) {
        this.numberOfHorizontalTiles = numberOfHorizontalTiles;
    }

    public Integer getNumberOfVerticalTiles() {
        return numberOfVerticalTiles;
    }

    public void setNumberOfVerticalTiles(Integer numberOfVerticalTiles) {
        this.numberOfVerticalTiles = numberOfVerticalTiles;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Double getTileScale() {
        return tileScale;
    }

    public void setTileScale(Double tileScale) {
        this.tileScale = tileScale;
    }

}
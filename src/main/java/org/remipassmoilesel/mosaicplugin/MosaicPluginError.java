package org.remipassmoilesel.mosaicplugin;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gce.imagemosaic.ImageMosaicFormat;
import org.geotools.gce.imagemosaic.ImageMosaicFormatFactory;
import org.geotools.gce.imagemosaic.ImageMosaicReader;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.Layer;
import org.geotools.styling.*;
import org.opengis.filter.FilterFactory2;
import org.opengis.style.ContrastMethod;
import org.remipassmoilesel.utils.GuiBuilder;
import org.remipassmoilesel.utils.GuiUtils;

import java.io.File;
import java.io.IOException;

/**
 * This example of mosaic plugin create a Java UnsupportedOperationError
 */
public class MosaicPluginError {

    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    public static void main(String[] args) throws IOException {
        showMosaic("data/geoserver-mosaic-sample");
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

        Layer rasterLayer = new GridCoverageLayer(coverage, getDefaultRGBRasterStyle(reader));

        GuiBuilder.newMap("Mosaic: " + pathSrc).addLayer(rasterLayer).show();

    }

    public static org.geotools.styling.Style getDefaultRGBRasterStyle(AbstractGridCoverage2DReader reader) {

        GridCoverage2D cov = null;
        try {
            cov = reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }
        // We need at least three bands to create an RGB style
        int numBands = cov.getNumSampleDimensions();
        if (numBands < 3) {
            return null;
        }
        // Get the names of the bands
        String[] sampleDimensionNames = new String[numBands];
        for (int i = 0; i < numBands; i++) {
            GridSampleDimension dim = cov.getSampleDimension(i);
            sampleDimensionNames[i] = dim.getDescription().toString();
        }
        final int RED = 0, GREEN = 1, BLUE = 2;
        int[] channelNum = {-1, -1, -1};
        // We examine the band names looking for "red...", "green...", "blue...".
        // Note that the channel numbers we record are indexed from 1, not 0.
        for (int i = 0; i < numBands; i++) {
            String name = sampleDimensionNames[i].toLowerCase();
            if (name != null) {
                if (name.matches("red.*")) {
                    channelNum[RED] = i + 1;
                } else if (name.matches("green.*")) {
                    channelNum[GREEN] = i + 1;
                } else if (name.matches("blue.*")) {
                    channelNum[BLUE] = i + 1;
                }
            }
        }
        // If we didn't find named bands "red...", "green...", "blue..."
        // we fall back to using the first three bands in order
        if (channelNum[RED] < 0 || channelNum[GREEN] < 0 || channelNum[BLUE] < 0) {
            channelNum[RED] = 1;
            channelNum[GREEN] = 2;
            channelNum[BLUE] = 3;
        }
        // Now we create a RasterSymbolizer using the selected channels
        SelectedChannelType[] sct = new SelectedChannelType[cov.getNumSampleDimensions()];
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        for (int i = 0; i < 3; i++) {
            sct[i] = sf.createSelectedChannelType(String.valueOf(channelNum[i]), ce);
        }
        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN], sct[BLUE]);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);

    }

    /**
     * Full stack trace:
     *

     /usr/lib/jvm/java-8-openjdk-amd64/bin/java -Didea.launcher.port=7533 -Didea.launcher.bin.path=/home/remipassmoilesel/intellij/bin -Dfile.encoding=UTF-8 -classpath /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/charsets.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/cldrdata.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/dnsns.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/icedtea-sound.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/jaccess.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/localedata.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/nashorn.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/sunec.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/sunjce_provider.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/sunpkcs11.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/zipfs.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jce.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jsse.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/management-agent.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/resources.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:/home/remipassmoilesel/projects/java/geotools-tutorial/target/classes:/home/remipassmoilesel/.m2/repository/org/geotools/gt-swing/15.2/gt-swing-15.2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-referencing/15.2/gt-referencing-15.2.jar:/home/remipassmoilesel/.m2/repository/com/googlecode/efficient-java-matrix-library/core/0.26/core-0.26.jar:/home/remipassmoilesel/.m2/repository/commons-pool/commons-pool/1.5.4/commons-pool-1.5.4.jar:/home/remipassmoilesel/.m2/repository/jgridshift/jgridshift/1.0/jgridshift-1.0.jar:/home/remipassmoilesel/.m2/repository/net/sf/geographiclib/GeographicLib-Java/1.44/GeographicLib-Java-1.44.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-render/15.2/gt-render-15.2.jar:/home/remipassmoilesel/.m2/repository/com/miglayout/miglayout/3.7/miglayout-3.7-swing.jar:/home/remipassmoilesel/.m2/repository/javax/media/jai_core/1.1.3/jai_core-1.1.3.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-shapefile/15.2/gt-shapefile-15.2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-data/15.2/gt-data-15.2.jar:/home/remipassmoilesel/.m2/repository/org/jdom/jdom/1.1.3/jdom-1.1.3.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-epsg-hsql/15.2/gt-epsg-hsql-15.2.jar:/home/remipassmoilesel/.m2/repository/org/hsqldb/hsqldb/2.3.0/hsqldb-2.3.0.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-svg/15.2/gt-svg-15.2.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-transcoder/1.7/batik-transcoder-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/fop/0.94/fop-0.94.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/xmlgraphics-commons/1.2/xmlgraphics-commons-1.2.jar:/home/remipassmoilesel/.m2/repository/org/apache/avalon/framework/avalon-framework-api/4.3.1/avalon-framework-api-4.3.1.jar:/home/remipassmoilesel/.m2/repository/org/apache/avalon/framework/avalon-framework-impl/4.3.1/avalon-framework-impl-4.3.1.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-awt-util/1.7/batik-awt-util-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-bridge/1.7/batik-bridge-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-anim/1.7/batik-anim-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-css/1.7/batik-css-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-ext/1.7/batik-ext-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-parser/1.7/batik-parser-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-script/1.7/batik-script-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-js/1.7/batik-js-1.7.jar:/home/remipassmoilesel/.m2/repository/xalan/xalan/2.6.0/xalan-2.6.0.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-dom/1.7/batik-dom-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-gvt/1.7/batik-gvt-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-svg-dom/1.7/batik-svg-dom-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-svggen/1.7/batik-svggen-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-util/1.7/batik-util-1.7.jar:/home/remipassmoilesel/.m2/repository/org/apache/xmlgraphics/batik-xml/1.7/batik-xml-1.7.jar:/home/remipassmoilesel/.m2/repository/xml-apis/xml-apis/1.3.04/xml-apis-1.3.04.jar:/home/remipassmoilesel/.m2/repository/xml-apis/xml-apis-ext/1.3.04/xml-apis-ext-1.3.04.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-geotiff/15.2/gt-geotiff-15.2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-main/15.2/gt-main-15.2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-coverage/15.2/gt-coverage-15.2.jar:/home/remipassmoilesel/.m2/repository/org/jaitools/jt-zonalstats/1.4.0/jt-zonalstats-1.4.0.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/affine/jt-affine/1.0.11/jt-affine-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/algebra/jt-algebra/1.0.11/jt-algebra-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/bandmerge/jt-bandmerge/1.0.11/jt-bandmerge-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/bandselect/jt-bandselect/1.0.11/jt-bandselect-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/bandcombine/jt-bandcombine/1.0.11/jt-bandcombine-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/border/jt-border/1.0.11/jt-border-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/buffer/jt-buffer/1.0.11/jt-buffer-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/crop/jt-crop/1.0.11/jt-crop-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/iterators/jt-iterators/1.0.11/jt-iterators-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/lookup/jt-lookup/1.0.11/jt-lookup-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/mosaic/jt-mosaic/1.0.11/jt-mosaic-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/nullop/jt-nullop/1.0.11/jt-nullop-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/rescale/jt-rescale/1.0.11/jt-rescale-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/scale/jt-scale/1.0.11/jt-scale-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/stats/jt-stats/1.0.11/jt-stats-1.0.11.jar:/home/remipassmoilesel/.m2/repository/com/google/guava/guava/17.0/guava-17.0.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/translate/jt-translate/1.0.11/jt-translate-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/utilities/jt-utilities/1.0.11/jt-utilities-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/warp/jt-warp/1.0.11/jt-warp-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/zonal/jt-zonal/1.0.11/jt-zonal-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/binarize/jt-binarize/1.0.11/jt-binarize-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/format/jt-format/1.0.11/jt-format-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/colorconvert/jt-colorconvert/1.0.11/jt-colorconvert-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/errordiffusion/jt-errordiffusion/1.0.11/jt-errordiffusion-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/orderdither/jt-orderdither/1.0.11/jt-orderdither-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/colorindexer/jt-colorindexer/1.0.11/jt-colorindexer-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/imagefunction/jt-imagefunction/1.0.11/jt-imagefunction-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/piecewise/jt-piecewise/1.0.11/jt-piecewise-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/classifier/jt-classifier/1.0.11/jt-classifier-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/rlookup/jt-rlookup/1.0.11/jt-rlookup-1.0.11.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/jaiext/vectorbin/jt-vectorbin/1.0.11/jt-vectorbin-1.0.11.jar:/home/remipassmoilesel/.m2/repository/javax/media/jai_imageio/1.1/jai_imageio-1.1.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/imageio-ext/imageio-ext-tiff/1.1.15/imageio-ext-tiff-1.1.15.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/imageio-ext/imageio-ext-utilities/1.1.15/imageio-ext-utilities-1.1.15.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-image/15.2/gt-image-15.2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-wms/15.2/gt-wms-15.2.jar:/home/remipassmoilesel/.m2/repository/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar:/home/remipassmoilesel/.m2/repository/commons-logging/commons-logging/1.0.4/commons-logging-1.0.4.jar:/home/remipassmoilesel/.m2/repository/commons-codec/commons-codec/1.2/commons-codec-1.2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-xml/15.2/gt-xml-15.2.jar:/home/remipassmoilesel/.m2/repository/org/apache/xml/xml-commons-resolver/1.2/xml-commons-resolver-1.2.jar:/home/remipassmoilesel/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-imagemosaic/15.2/gt-imagemosaic-15.2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-api/15.2/gt-api-15.2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-jdbc/15.2/gt-jdbc-15.2.jar:/home/remipassmoilesel/.m2/repository/commons-dbcp/commons-dbcp/1.4/commons-dbcp-1.4.jar:/home/remipassmoilesel/.m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:/home/remipassmoilesel/.m2/repository/com/vividsolutions/jts/1.13/jts-1.13.jar:/home/remipassmoilesel/.m2/repository/net/java/dev/jsr-275/jsr-275/1.0-beta-2/jsr-275-1.0-beta-2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-transform/15.2/gt-transform-15.2.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/imageio-ext/imageio-ext-streams/1.1.15/imageio-ext-streams-1.1.15.jar:/home/remipassmoilesel/.m2/repository/it/geosolutions/imageio-ext/imageio-ext-geocore/1.1.15/imageio-ext-geocore-1.1.15.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-cql/15.2/gt-cql-15.2.jar:/home/remipassmoilesel/.m2/repository/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.jar:/home/remipassmoilesel/.m2/repository/org/jaitools/jt-utils/1.4.0/jt-utils-1.4.0.jar:/home/remipassmoilesel/.m2/repository/org/jaitools/jt-vectorbinarize/1.4.0/jt-vectorbinarize-1.4.0.jar:/home/remipassmoilesel/.m2/repository/net/sf/ehcache/ehcache/1.6.2/ehcache-1.6.2.jar:/home/remipassmoilesel/.m2/repository/javax/media/jai_codec/1.1.3/jai_codec-1.1.3.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-metadata/15.2/gt-metadata-15.2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-opengis/15.2/gt-opengis-15.2.jar:/home/remipassmoilesel/.m2/repository/org/geotools/jdbc/gt-jdbc-postgis/15.2/gt-jdbc-postgis-15.2.jar:/home/remipassmoilesel/.m2/repository/org/postgresql/postgresql/9.4-1201-jdbc41/postgresql-9.4-1201-jdbc41.jar:/home/remipassmoilesel/.m2/repository/org/geotools/gt-imagepyramid/15.2/gt-imagepyramid-15.2.jar:/home/remipassmoilesel/intellij/lib/idea_rt.jar com.intellij.rt.execution.application.AppMain org.remipassmoilesel.image.MosaicPluginError
     oct. 30, 2016 3:38:12 PM org.geotools.gce.imagemosaic.Utils loadSampleImage
     AVERTISSEMENT: Unable to find sample image for path /home/remipassmoilesel/projects/java/geotools-tutorial/data/geoserver-mosaic-sample/sample_image
     oct. 30, 2016 3:38:12 PM org.geotools.gce.imagemosaic.RasterManager loadSampleImage
     AVERTISSEMENT: Unable to find sample image for path file:/home/remipassmoilesel/projects/java/geotools-tutorial/data/geoserver-mosaic-sample/mosaic.shp
     2016-10-30T15:38:14.178+0100  WARNING  This granule catalog was not properly dispose as it still points to:ServiceInfo
     description=Features from ShapefileDataStore
     java.lang.UnsupportedOperationException: Not implemented
     at org.jaitools.imageutils.ROIGeometry.getAsRectangleList(ROIGeometry.java:488)
     at com.sun.media.jai.opimage.ExtremaOpImage.accumulateStatistics(ExtremaOpImage.java:176)
     at javax.media.jai.StatisticsOpImage.getProperty(StatisticsOpImage.java:292)
     at com.sun.media.jai.opimage.ExtremaOpImage.getProperty(ExtremaOpImage.java:100)
     at javax.media.jai.RenderedOp$1.getProperty(RenderedOp.java:1808)
     at javax.media.jai.PropertyEnvironment.getProperty(PropertyEnvironment.java:197)
     at javax.media.jai.PropertySourceImpl.getProperty(PropertySourceImpl.java:277)
     at javax.media.jai.WritablePropertySourceImpl.getProperty(WritablePropertySourceImpl.java:130)
     at javax.media.jai.RenderedOp.getProperty(RenderedOp.java:1982)
     at org.geotools.image.ImageWorker.getComputedProperty(ImageWorker.java:961)
     at org.geotools.image.ImageWorker.getExtremas(ImageWorker.java:1015)
     at org.geotools.image.ImageWorker.getMinimums(ImageWorker.java:1167)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementType$4.process(ContrastEnhancementType.java:291)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.performContrastEnhancement(ContrastEnhancementNode.java:516)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:365)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.BandMergeNode.execute(BandMergeNode.java:166)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:274)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:57)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:240)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:78)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:58)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.symbolize(GridCoverageRenderer.java:474)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderCoverage(GridCoverageRenderer.java:447)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderImage(GridCoverageRenderer.java:393)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1131)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1093)
     at org.geotools.renderer.lite.StreamingRenderer$RenderRasterRequest.execute(StreamingRenderer.java:3381)
     at org.geotools.renderer.lite.StreamingRenderer$PainterThread.run(StreamingRenderer.java:3554)
     at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
     at java.lang.Thread.run(Thread.java:745)
     org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.BandMergeNode.execute(BandMergeNode.java:166)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:274)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:57)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:240)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:78)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:58)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.symbolize(GridCoverageRenderer.java:474)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderCoverage(GridCoverageRenderer.java:447)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderImage(GridCoverageRenderer.java:393)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1131)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1093)
     at org.geotools.renderer.lite.StreamingRenderer$RenderRasterRequest.execute(StreamingRenderer.java:3381)
     at org.geotools.renderer.lite.StreamingRenderer$PainterThread.run(StreamingRenderer.java:3554)
     at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
     at java.lang.Thread.run(Thread.java:745)
     Caused by: java.lang.UnsupportedOperationException: Not implemented
     at org.jaitools.imageutils.ROIGeometry.getAsRectangleList(ROIGeometry.java:488)
     at com.sun.media.jai.opimage.ExtremaOpImage.accumulateStatistics(ExtremaOpImage.java:176)
     at javax.media.jai.StatisticsOpImage.getProperty(StatisticsOpImage.java:292)
     at com.sun.media.jai.opimage.ExtremaOpImage.getProperty(ExtremaOpImage.java:100)
     at javax.media.jai.RenderedOp$1.getProperty(RenderedOp.java:1808)
     at javax.media.jai.PropertyEnvironment.getProperty(PropertyEnvironment.java:197)
     at javax.media.jai.PropertySourceImpl.getProperty(PropertySourceImpl.java:277)
     at javax.media.jai.WritablePropertySourceImpl.getProperty(WritablePropertySourceImpl.java:130)
     at javax.media.jai.RenderedOp.getProperty(RenderedOp.java:1982)
     at org.geotools.image.ImageWorker.getComputedProperty(ImageWorker.java:961)
     at org.geotools.image.ImageWorker.getExtremas(ImageWorker.java:1015)
     at org.geotools.image.ImageWorker.getMinimums(ImageWorker.java:1167)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementType$4.process(ContrastEnhancementType.java:291)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.performContrastEnhancement(ContrastEnhancementNode.java:516)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:365)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 46 more
     org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:274)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:57)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:240)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:78)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:58)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.symbolize(GridCoverageRenderer.java:474)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderCoverage(GridCoverageRenderer.java:447)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderImage(GridCoverageRenderer.java:393)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1131)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1093)
     at org.geotools.renderer.lite.StreamingRenderer$RenderRasterRequest.execute(StreamingRenderer.java:3381)
     at org.geotools.renderer.lite.StreamingRenderer$PainterThread.run(StreamingRenderer.java:3554)
     at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
     at java.lang.Thread.run(Thread.java:745)
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.BandMergeNode.execute(BandMergeNode.java:166)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 41 more
     Caused by: java.lang.UnsupportedOperationException: Not implemented
     at org.jaitools.imageutils.ROIGeometry.getAsRectangleList(ROIGeometry.java:488)
     at com.sun.media.jai.opimage.ExtremaOpImage.accumulateStatistics(ExtremaOpImage.java:176)
     at javax.media.jai.StatisticsOpImage.getProperty(StatisticsOpImage.java:292)
     at com.sun.media.jai.opimage.ExtremaOpImage.getProperty(ExtremaOpImage.java:100)
     at javax.media.jai.RenderedOp$1.getProperty(RenderedOp.java:1808)
     at javax.media.jai.PropertyEnvironment.getProperty(PropertyEnvironment.java:197)
     at javax.media.jai.PropertySourceImpl.getProperty(PropertySourceImpl.java:277)
     at javax.media.jai.WritablePropertySourceImpl.getProperty(WritablePropertySourceImpl.java:130)
     at javax.media.jai.RenderedOp.getProperty(RenderedOp.java:1982)
     at org.geotools.image.ImageWorker.getComputedProperty(ImageWorker.java:961)
     at org.geotools.image.ImageWorker.getExtremas(ImageWorker.java:1015)
     at org.geotools.image.ImageWorker.getMinimums(ImageWorker.java:1167)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementType$4.process(ContrastEnhancementType.java:291)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.performContrastEnhancement(ContrastEnhancementNode.java:516)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:365)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 46 more
     org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:274)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:57)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:240)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:78)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:58)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.symbolize(GridCoverageRenderer.java:474)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderCoverage(GridCoverageRenderer.java:447)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderImage(GridCoverageRenderer.java:393)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1131)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1093)
     at org.geotools.renderer.lite.StreamingRenderer$RenderRasterRequest.execute(StreamingRenderer.java:3381)
     at org.geotools.renderer.lite.StreamingRenderer$PainterThread.run(StreamingRenderer.java:3554)
     at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
     at java.lang.Thread.run(Thread.java:745)
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 36 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.BandMergeNode.execute(BandMergeNode.java:166)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 41 more
     Caused by: java.lang.UnsupportedOperationException: Not implemented
     at org.jaitools.imageutils.ROIGeometry.getAsRectangleList(ROIGeometry.java:488)
     at com.sun.media.jai.opimage.ExtremaOpImage.accumulateStatistics(ExtremaOpImage.java:176)
     at javax.media.jai.StatisticsOpImage.getProperty(StatisticsOpImage.java:292)
     at com.sun.media.jai.opimage.ExtremaOpImage.getProperty(ExtremaOpImage.java:100)
     at javax.media.jai.RenderedOp$1.getProperty(RenderedOp.java:1808)
     at javax.media.jai.PropertyEnvironment.getProperty(PropertyEnvironment.java:197)
     at javax.media.jai.PropertySourceImpl.getProperty(PropertySourceImpl.java:277)
     at javax.media.jai.WritablePropertySourceImpl.getProperty(WritablePropertySourceImpl.java:130)
     at javax.media.jai.RenderedOp.getProperty(RenderedOp.java:1982)
     at org.geotools.image.ImageWorker.getComputedProperty(ImageWorker.java:961)
     at org.geotools.image.ImageWorker.getExtremas(ImageWorker.java:1015)
     at org.geotools.image.ImageWorker.getMinimums(ImageWorker.java:1167)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementType$4.process(ContrastEnhancementType.java:291)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.performContrastEnhancement(ContrastEnhancementNode.java:516)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:365)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 46 more
     org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:240)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:78)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:58)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.symbolize(GridCoverageRenderer.java:474)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderCoverage(GridCoverageRenderer.java:447)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderImage(GridCoverageRenderer.java:393)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1131)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1093)
     at org.geotools.renderer.lite.StreamingRenderer$RenderRasterRequest.execute(StreamingRenderer.java:3381)
     at org.geotools.renderer.lite.StreamingRenderer$PainterThread.run(StreamingRenderer.java:3554)
     at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
     at java.lang.Thread.run(Thread.java:745)
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:274)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:57)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 29 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 36 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.BandMergeNode.execute(BandMergeNode.java:166)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 41 more
     Caused by: java.lang.UnsupportedOperationException: Not implemented
     at org.jaitools.imageutils.ROIGeometry.getAsRectangleList(ROIGeometry.java:488)
     at com.sun.media.jai.opimage.ExtremaOpImage.accumulateStatistics(ExtremaOpImage.java:176)
     at javax.media.jai.StatisticsOpImage.getProperty(StatisticsOpImage.java:292)
     at com.sun.media.jai.opimage.ExtremaOpImage.getProperty(ExtremaOpImage.java:100)
     at javax.media.jai.RenderedOp$1.getProperty(RenderedOp.java:1808)
     at javax.media.jai.PropertyEnvironment.getProperty(PropertyEnvironment.java:197)
     at javax.media.jai.PropertySourceImpl.getProperty(PropertySourceImpl.java:277)
     at javax.media.jai.WritablePropertySourceImpl.getProperty(WritablePropertySourceImpl.java:130)
     at javax.media.jai.RenderedOp.getProperty(RenderedOp.java:1982)
     at org.geotools.image.ImageWorker.getComputedProperty(ImageWorker.java:961)
     at org.geotools.image.ImageWorker.getExtremas(ImageWorker.java:1015)
     at org.geotools.image.ImageWorker.getMinimums(ImageWorker.java:1167)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementType$4.process(ContrastEnhancementType.java:291)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.performContrastEnhancement(ContrastEnhancementNode.java:516)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:365)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 46 more
     org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:78)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:58)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.symbolize(GridCoverageRenderer.java:474)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderCoverage(GridCoverageRenderer.java:447)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderImage(GridCoverageRenderer.java:393)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1131)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1093)
     at org.geotools.renderer.lite.StreamingRenderer$RenderRasterRequest.execute(StreamingRenderer.java:3381)
     at org.geotools.renderer.lite.StreamingRenderer$PainterThread.run(StreamingRenderer.java:3554)
     at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
     at java.lang.Thread.run(Thread.java:745)
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:240)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 22 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:274)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:57)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 29 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 36 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.BandMergeNode.execute(BandMergeNode.java:166)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 41 more
     Caused by: java.lang.UnsupportedOperationException: Not implemented
     at org.jaitools.imageutils.ROIGeometry.getAsRectangleList(ROIGeometry.java:488)
     at com.sun.media.jai.opimage.ExtremaOpImage.accumulateStatistics(ExtremaOpImage.java:176)
     at javax.media.jai.StatisticsOpImage.getProperty(StatisticsOpImage.java:292)
     at com.sun.media.jai.opimage.ExtremaOpImage.getProperty(ExtremaOpImage.java:100)
     at javax.media.jai.RenderedOp$1.getProperty(RenderedOp.java:1808)
     at javax.media.jai.PropertyEnvironment.getProperty(PropertyEnvironment.java:197)
     at javax.media.jai.PropertySourceImpl.getProperty(PropertySourceImpl.java:277)
     at javax.media.jai.WritablePropertySourceImpl.getProperty(WritablePropertySourceImpl.java:130)
     at javax.media.jai.RenderedOp.getProperty(RenderedOp.java:1982)
     at org.geotools.image.ImageWorker.getComputedProperty(ImageWorker.java:961)
     at org.geotools.image.ImageWorker.getExtremas(ImageWorker.java:1015)
     at org.geotools.image.ImageWorker.getMinimums(ImageWorker.java:1167)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementType$4.process(ContrastEnhancementType.java:291)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.performContrastEnhancement(ContrastEnhancementNode.java:516)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:365)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 46 more
     2016-10-30T15:38:15.376+0100  WARNING  org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.symbolize(GridCoverageRenderer.java:474)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderCoverage(GridCoverageRenderer.java:447)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderImage(GridCoverageRenderer.java:393)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1131)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1093)
     at org.geotools.renderer.lite.StreamingRenderer$RenderRasterRequest.execute(StreamingRenderer.java:3381)
     at org.geotools.renderer.lite.StreamingRenderer$PainterThread.run(StreamingRenderer.java:3554)
     at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
     at java.lang.Thread.run(Thread.java:745)
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:78)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:58)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 14 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:240)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 22 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:274)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:57)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 29 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 36 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.BandMergeNode.execute(BandMergeNode.java:166)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 41 more
     Caused by: java.lang.UnsupportedOperationException: Not implemented
     at org.jaitools.imageutils.ROIGeometry.getAsRectangleList(ROIGeometry.java:488)
     at com.sun.media.jai.opimage.ExtremaOpImage.accumulateStatistics(ExtremaOpImage.java:176)
     at javax.media.jai.StatisticsOpImage.getProperty(StatisticsOpImage.java:292)
     at com.sun.media.jai.opimage.ExtremaOpImage.getProperty(ExtremaOpImage.java:100)
     at javax.media.jai.RenderedOp$1.getProperty(RenderedOp.java:1808)
     at javax.media.jai.PropertyEnvironment.getProperty(PropertyEnvironment.java:197)
     at javax.media.jai.PropertySourceImpl.getProperty(PropertySourceImpl.java:277)
     at javax.media.jai.WritablePropertySourceImpl.getProperty(WritablePropertySourceImpl.java:130)
     at javax.media.jai.RenderedOp.getProperty(RenderedOp.java:1982)
     at org.geotools.image.ImageWorker.getComputedProperty(ImageWorker.java:961)
     at org.geotools.image.ImageWorker.getExtremas(ImageWorker.java:1015)
     at org.geotools.image.ImageWorker.getMinimums(ImageWorker.java:1167)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementType$4.process(ContrastEnhancementType.java:291)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.performContrastEnhancement(ContrastEnhancementNode.java:516)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:365)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 46 more

     2016-10-30T15:38:15.377+0100  SEVERE  org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.symbolize(GridCoverageRenderer.java:474)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderCoverage(GridCoverageRenderer.java:447)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.renderImage(GridCoverageRenderer.java:393)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1131)
     at org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer.paint(GridCoverageRenderer.java:1093)
     at org.geotools.renderer.lite.StreamingRenderer$RenderRasterRequest.execute(StreamingRenderer.java:3381)
     at org.geotools.renderer.lite.StreamingRenderer$PainterThread.run(StreamingRenderer.java:3554)
     at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
     at java.util.concurrent.FutureTask.run(FutureTask.java:266)
     at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
     at java.lang.Thread.run(Thread.java:745)
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:78)
     at org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerHelper.execute(RasterSymbolizerHelper.java:58)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 14 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:240)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 22 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:274)
     at org.geotools.renderer.lite.gridcoverage2d.ColorMapNode.execute(ColorMapNode.java:57)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 29 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.SubchainStyleVisitorCoverageProcessingAdapter.execute(SubchainStyleVisitorCoverageProcessingAdapter.java:116)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 36 more
     Caused by: org.geotools.coverage.processing.CoverageProcessingException: java.lang.UnsupportedOperationException: Not implemented
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:343)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:46)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter.getOutput(StyleVisitorCoverageProcessingNodeAdapter.java:141)
     at org.geotools.renderer.lite.gridcoverage2d.BandMergeNode.execute(BandMergeNode.java:166)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 41 more
     Caused by: java.lang.UnsupportedOperationException: Not implemented
     at org.jaitools.imageutils.ROIGeometry.getAsRectangleList(ROIGeometry.java:488)
     at com.sun.media.jai.opimage.ExtremaOpImage.accumulateStatistics(ExtremaOpImage.java:176)
     at javax.media.jai.StatisticsOpImage.getProperty(StatisticsOpImage.java:292)
     at com.sun.media.jai.opimage.ExtremaOpImage.getProperty(ExtremaOpImage.java:100)
     at javax.media.jai.RenderedOp$1.getProperty(RenderedOp.java:1808)
     at javax.media.jai.PropertyEnvironment.getProperty(PropertyEnvironment.java:197)
     at javax.media.jai.PropertySourceImpl.getProperty(PropertySourceImpl.java:277)
     at javax.media.jai.WritablePropertySourceImpl.getProperty(WritablePropertySourceImpl.java:130)
     at javax.media.jai.RenderedOp.getProperty(RenderedOp.java:1982)
     at org.geotools.image.ImageWorker.getComputedProperty(ImageWorker.java:961)
     at org.geotools.image.ImageWorker.getExtremas(ImageWorker.java:1015)
     at org.geotools.image.ImageWorker.getMinimums(ImageWorker.java:1167)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementType$4.process(ContrastEnhancementType.java:291)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.performContrastEnhancement(ContrastEnhancementNode.java:516)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:365)
     at org.geotools.renderer.lite.gridcoverage2d.ContrastEnhancementNode.execute(ContrastEnhancementNode.java:77)
     at org.geotools.renderer.lite.gridcoverage2d.StyleVisitorCoverageProcessingNodeAdapter$1.execute(StyleVisitorCoverageProcessingNodeAdapter.java:102)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.checkExecuted(BaseCoverageProcessingNode.java:238)
     at org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode.getOutput(BaseCoverageProcessingNode.java:341)
     ... 46 more


     Process finished with exit code 0

     *
     */
}

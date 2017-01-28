//package org.remipassmoilesel.mosaic;
//
//import org.geotools.coverage.grid.GridCoverage2D;
//import org.geotools.coverage.grid.GridEnvelope2D;
//import org.geotools.coverage.grid.GridGeometry2D;
//import org.geotools.coverage.grid.io.AbstractGridFormat;
//import org.geotools.data.DataStore;
//import org.geotools.factory.CommonFactoryFinder;
//import org.geotools.filter.text.cql2.CQL;
//import org.geotools.filter.text.cql2.CQLException;
//import org.geotools.gce.imagecollection.ImageCollectionFormat;
//import org.geotools.gce.imagecollection.ImageCollectionReader;
//import org.geotools.geometry.GeneralEnvelope;
//import org.geotools.jdbc.JDBCDataStore;
//import org.geotools.map.GridCoverageLayer;
//import org.geotools.referencing.factory.epsg.CartesianAuthorityFactory;
//import org.opengis.filter.Filter;
//import org.opengis.filter.FilterFactory;
//import org.opengis.filter.PropertyIsEqualTo;
//import org.opengis.parameter.GeneralParameterValue;
//import org.opengis.parameter.ParameterValue;
//import org.remipassmoilesel.utils.GuiUtils;
//
//import java.awt.*;
//import java.awt.image.RenderedImage;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
///**
// * Simple trial on image collection, non functional for now
// */
//public class ImageCollectionLab {
//
//    public static final FilterFactory ff = CommonFactoryFinder.getFilterFactory();
//
//    public static void main(String[] args) throws CQLException, IOException {
//
//        Path sourceDirectory = Paths.get("data/arbitrary-pictures-2");
//        final String string = "PATH='tops-of-pine-trees.jpg'";
//        Filter filter = CQL.toFilter(string);
//
//        PropertyIsEqualTo filter2 = ff.equal(ff.property("PATH"), ff.literal("4-trees.jpg"), true);
//
//        final ImageCollectionReader reader = new ImageCollectionReader(sourceDirectory.toString());
//
//        final ParameterValue<GridGeometry2D> gg = AbstractGridFormat.READ_GRIDGEOMETRY2D.createValue();
//        final GeneralEnvelope envelope = new GeneralEnvelope(new Rectangle(1000, -800, 1000, 400));
//        envelope.setCoordinateReferenceSystem(CartesianAuthorityFactory.GENERIC_2D);
//
//        final Rectangle rasterArea = new Rectangle(0, 0, 500, 800);
//        final GridEnvelope2D range = new GridEnvelope2D(rasterArea);
//        gg.setValue(new GridGeometry2D(range, envelope));
//
//        final ParameterValue<Filter> ff = ImageCollectionFormat.FILTER.createValue();
//        ff.setValue(filter);
//
//        final ParameterValue<double[]> background = ImageCollectionFormat.BACKGROUND_VALUES.createValue();
//        background.setValue(new double[]{0});
//
//        GeneralParameterValue[] params = new GeneralParameterValue[]{ff, gg, background};
//
//        // reading the coverage
//        GridCoverage2D coverage = reader.read(params);
//
//        GridCoverageLayer layer = new GridCoverageLayer(coverage, GuiUtils.createDefaultRGBStyle(reader));
//
//        GuiUtils.showInWindow(layer);
//    }
//}

package org.remipassmoilesel.mark;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.style.DynamicSymbolFactoryFinder;
import org.geotools.renderer.style.ExternalGraphicFactory;
import org.geotools.styling.*;
import org.opengis.filter.FilterFactory2;
import org.remipassmoilesel.utils.GuiUtils;
import org.remipassmoilesel.utils.SimpleFeatureUtils;

import javax.imageio.ImageIO;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.Random;

/**
 * Misc trials around custom icons creation
 */
public class CustomMarksTrial {

    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    private static final StyleBuilder builder = new StyleBuilder();

    public static void main(String[] args) throws MalformedURLException {

        printExternalFactories();

        System.out.println("");

        printSupportedFormatsByDefaultImageFactory();

        System.out.println();

        // create style for point
        // Style style = builder.createStyle(createShapeSymbolizer());
        // Style style = builder.createStyle(createDefaultImageSymbolizer());
        Style style = builder.createStyle(createCustomImageSymbolizer());

        // create a map content and a layer
        MapContent mapContent = new MapContent();

        DefaultFeatureCollection shapes = new DefaultFeatureCollection();
        FeatureLayer layer = new FeatureLayer(shapes, style);

        PrimitiveIterator.OfDouble rand = new Random().doubles(1, 100).iterator();
        for (int i = 0; i < 100; i++) {
            double x = rand.nextDouble();
            double y = rand.nextDouble();

            shapes.add(SimpleFeatureUtils.getPointFeature(geometryFactory.createPoint(new Coordinate(x, y))));
        }

        mapContent.addLayer(layer);

        // show all in window
        GuiUtils.showInWindow(mapContent);

    }

    /**
     * Create a point symbolizer from a custom Java shape
     * @return
     */
    public static PointSymbolizer createShapeSymbolizer() {
        Graphic splat = builder.createGraphic(null, builder.createMark("splat"), null);
        return builder.createPointSymbolizer(splat);
    }

    /**
     * Create a point symbolizer from a custom image created with the default image factory
     * @return
     */
    public static PointSymbolizer createDefaultImageSymbolizer() {
        ExternalGraphic icon = null;
        try {
            // must provide a full URL
            URL base = Paths.get(".").toAbsolutePath().normalize().toUri().toURL();
            icon = builder.createExternalGraphic(new URL(base, "data/splash.png"), "image/png");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Graphic g = builder.createGraphic();
        g.graphicalSymbols().add(icon);
        return builder.createPointSymbolizer(g);
    }

    /**
     * Create a point symbolizer from a custom icon using a custom icon factory. Usefull for using custom icons without
     * use absolute paths, mandatory with file:// protocol.
     * @return
     */
    public static PointSymbolizer createCustomImageSymbolizer() {
        ExternalGraphic icon = builder.createExternalGraphic("file://customicon/data/splash.jpg", "image/customicon");
        Graphic g = builder.createGraphic();
        g.graphicalSymbols().add(icon);
        return builder.createPointSymbolizer(g);
    }

    /**
     * Display list of available factories
     */
    public static void printExternalFactories() {
        System.out.println("DynamicSymbolFactoryFinder.getExternalGraphicFactories();");
        Iterator<ExternalGraphicFactory> it = DynamicSymbolFactoryFinder.getExternalGraphicFactories();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    /**
     * Dsipaly list of image formats which will be handle by default icon factory
     */
    public static void printSupportedFormatsByDefaultImageFactory() {
        System.out.println("ImageIO.getReaderMIMETypes();");
        String[] types = ImageIO.getReaderMIMETypes();
        for (int i = 0; i < types.length; i++) {
            System.out.println(types[i]);
        }
    }

}

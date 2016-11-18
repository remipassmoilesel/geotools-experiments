package org.remipassmoilesel.customMosaic;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.styling.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsEqualTo;
import org.remipassmoilesel.utils.GuiUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Simple trial to create a mosaic from external graphics. Not usable.
 * <p>
 * Results: Very fluid, but for 55 pictures ~ 22MP total it use 1.5GB heap space
 * <p>
 * To work on it: scale change, icons always keep original size
 */
public class CustomMosaic {

    public static final Path ROOT_FOLDER = Paths.get("data/arbitrary-pictures-2");
    private static final String TILE_ID_FIELD_NAME = "tile_id";
    private static Style style;
    private static DefaultFeatureCollection featureCollection;

    private static final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
    private static final StyleBuilder styleBuilder = new StyleBuilder();

    private static final SimpleFeatureBuilder fbuilder;
    private static FeatureTypeStyle featureTypeStyle;

    static {
        // create a feature type
        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("geometries");
        tbuilder.setCRS(DefaultEngineeringCRS.CARTESIAN_2D);
        tbuilder.add("geometry", Geometry.class);
        tbuilder.add(TILE_ID_FIELD_NAME, String.class);

        SimpleFeatureType type = tbuilder.buildFeatureType();

        // create a feature builder
        fbuilder = new SimpleFeatureBuilder(type);

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        Thread.sleep(3000);


        Iterator<Path> dir = Files.newDirectoryStream(ROOT_FOLDER).iterator();

        // create a layer
        style = sf.createStyle();
        featureTypeStyle = sf.createFeatureTypeStyle();
        style.featureTypeStyles().add(featureTypeStyle);

        featureCollection = new DefaultFeatureCollection();
        FeatureLayer layer = new FeatureLayer(featureCollection, style);

        // image count after changing row
        int width = 5;

        // pixel movement
        int mvX = 1000;
        int mvY = 1000;

        int w = 0;
        int x = 0;
        int y = 0;
        while (dir.hasNext()) {

            Path p = dir.next();
            System.out.println("Processing: " + p);

            String tileId = p.getFileName().toString();
            if (tileId.endsWith(".jpg") == false &&
                tileId.endsWith(".jpeg") == false &&
                tileId.endsWith(".png") == false) {
                continue;
            }
            createTile(new Coordinate(x, y), tileId);

            if (w < width) {
                x += mvX;
            } else {
                w = 0;
                x = 0;
                y += mvY;
            }

            w++;
        }

        // show in gui
        GuiUtils.showInWindow(layer);

    }

    public static void createTile(Coordinate position, String pictureId) {

        // create feature with tile id
        fbuilder.add(geometryFactory.createPoint(position));
        fbuilder.add(pictureId);
        SimpleFeature feature = fbuilder.buildFeature(null);

        featureCollection.add(feature);

        // create symbolizer
        ExternalGraphic icon = styleBuilder.createExternalGraphic("file://tile/" + pictureId, "image/tile");
        Graphic g = styleBuilder.createGraphic();
        g.graphicalSymbols().add(icon);
        PointSymbolizer sym = styleBuilder.createPointSymbolizer(g);

        // create rule and filter
        Rule r = sf.createRule();
        r.symbolizers().add(sym);
        PropertyIsEqualTo filter = ff.equal(ff.property(TILE_ID_FIELD_NAME), ff.literal(pictureId), true);
        r.setFilter(filter);

        featureTypeStyle.rules().add(r);

    }


}

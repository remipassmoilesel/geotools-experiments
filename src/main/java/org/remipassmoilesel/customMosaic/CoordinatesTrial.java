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
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.remipassmoilesel.utils.GuiUtils;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by remipassmoilesel on 16/11/16.
 */
public class CoordinatesTrial {

    private static final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
    private static final GeometryFactory geom = JTSFactoryFinder.getGeometryFactory();

    private static final SimpleFeatureBuilder fbuilder;

    static {
        // create a feature type
        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("geometries");
        tbuilder.setCRS(DefaultEngineeringCRS.CARTESIAN_2D);
        tbuilder.add("geometry", Geometry.class);

        SimpleFeatureType type = tbuilder.buildFeatureType();

        // create a feature builder
        fbuilder = new SimpleFeatureBuilder(type);

    }

    public static void main(String[] args) throws IOException {

        Style style = sf.createStyle();
        DefaultFeatureCollection coll = new DefaultFeatureCollection();
        FeatureLayer layer = new FeatureLayer(coll, style);

        FeatureTypeStyle featureTypeStyle = sf.createFeatureTypeStyle();
        style.featureTypeStyles().add(featureTypeStyle);

        createPoint(new Coordinate(0, 0), Color.blue, coll, featureTypeStyle);
        createPoint(new Coordinate(200, 200), Color.red, coll, featureTypeStyle);

        System.out.println(style);
        System.out.println(style.featureTypeStyles());
        System.out.println(style.featureTypeStyles().get(0).rules());

        GuiUtils.showInWindow(layer);

    }

    public static SimpleFeature createPoint(Coordinate coords, Color color, DefaultFeatureCollection coll, FeatureTypeStyle featureTypeStyle) {

        fbuilder.add(geom.createPoint(coords));
        SimpleFeature feature = fbuilder.buildFeature(null);
        coll.add(feature);

        System.out.println(feature.getIdentifier());

        // create point symbolizer
        org.geotools.styling.Stroke stroke = sf.stroke(ff.literal(color), null, null, null, null, null, null);
        Fill fill = sf.fill(null, ff.literal(color), ff.literal(1.0));

        Mark mark = sf.getCircleMark();
        mark.setFill(fill);
        mark.setStroke(stroke);

        Graphic graphic = sf.createDefaultGraphic();
        graphic.graphicalSymbols().clear();
        graphic.graphicalSymbols().add(mark);
        graphic.setSize(ff.literal(20));

        PointSymbolizer pointSym = sf.createPointSymbolizer(graphic, null);

        // create rule
        Rule r = sf.createRule();
        r.symbolizers().add(pointSym);

        // apply on specified id
        HashSet<FeatureId> ids = new HashSet<>();
        ids.add(feature.getIdentifier());
        Id filter = ff.id(ids);
        r.setFilter(filter);

        featureTypeStyle.rules().add(r);

        return feature;
    }

}
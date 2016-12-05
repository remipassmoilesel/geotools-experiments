package org.remipassmoilesel.worldfile;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WMSUtils;
import org.geotools.data.wms.WebMapServer;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.CRS;
import org.geotools.styling.*;
import org.geotools.styling.Stroke;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.remipassmoilesel.utils.GuiUtils;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.PrimitiveIterator;
import java.util.Random;

public class MultipleCRSLab {

    private static final byte POINT_SIZE = 10;

    static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    static FilterFactory ff = CommonFactoryFinder.getFilterFactory();
    static GeometryFactory geomf = JTSFactoryFinder.getGeometryFactory();

    public static void main(String[] args) throws FactoryException, IOException, ServiceException {

        /**
         * We will create two layers:
         *
         *  - One with WMS layer CRS WGS84
         *  - One other with shapes on default cartesian CRS
         */

        MapContent map = new MapContent();

        CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");
        CoordinateReferenceSystem crs2D = CRS.decode("EPSG:404000");

        // add a wms layer
        String wmsUrl = "http://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities";
        int wmsLayerIndex = 0;
        WebMapServer wms = new WebMapServer(new URL(wmsUrl));
        WMSCapabilities capabilities = wms.getCapabilities();

        Layer[] namedLayers = WMSUtils.getNamedLayers(capabilities);

        for (Layer namedLayer : namedLayers) {
            System.out.println(namedLayer);
        }

        WMSLayer wmsLayer = new WMSLayer(wms, namedLayers[wmsLayerIndex]);

        System.out.println("wmsLayer.getCoordinateReferenceSystem()");
        System.out.println(wmsLayer.getCoordinateReferenceSystem());

        map.addLayer(wmsLayer);

        // create random points
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("points");

        // set to crs2d look the same as null
        builder.setCRS(crs2D);
        //builder.setCRS(null);

        builder.add("geometry", Point.class);

        SimpleFeatureType type = builder.buildFeatureType();

        DefaultFeatureCollection geomCollection = new DefaultFeatureCollection();
        SimpleFeatureBuilder featb = new SimpleFeatureBuilder(type);

//        PrimitiveIterator.OfDouble randLat = new Random().doubles(42.88, 51.04).iterator();
//        PrimitiveIterator.OfDouble randLng = new Random().doubles(-4.73, 7.51).iterator();
        PrimitiveIterator.OfDouble randLat = new Random().doubles(5120074, 6605773).iterator();
        PrimitiveIterator.OfDouble randLng = new Random().doubles(-553187, 1002732).iterator();
        for (int i = 0; i < 100; i++) {
            featb.add(geomf.createPoint(new Coordinate(randLng.next(), randLat.next())));
            SimpleFeature f = featb.buildFeature(null);
            geomCollection.add(f);
            //System.out.println(f);
        }

        // create a style for points
        Stroke stroke = sf.stroke(ff.literal(Color.blue), ff.literal(2), null, null, null, null, null);
        Fill fill = sf.fill(null, ff.literal(Color.green), ff.literal(0.5));

        Mark mark = sf.getCircleMark();
        mark.setFill(fill);
        mark.setStroke(stroke);

        Graphic graphic = sf.createDefaultGraphic();
        graphic.graphicalSymbols().clear();
        graphic.graphicalSymbols().add(mark);
        graphic.setSize(ff.literal(POINT_SIZE));

        PointSymbolizer pointsym = sf.createPointSymbolizer(graphic, null);

        Rule rule = sf.createRule();
        rule.symbolizers().add(pointsym);
        rule.setElseFilter(true);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle(new Rule[]{rule});
        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);

        // create a layer for points and add it to map
        FeatureLayer geomLayer = new FeatureLayer(geomCollection, style);
        map.addLayer(geomLayer);

        GuiUtils.showInWindow(map);

    }
}

package org.remipassmoilesel.draw;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.Layer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.remipassmoilesel.utils.SimpleFeatureUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * Created by remipassmoilesel on 02/11/16.
 */
public class LineBuilder {

    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    private Style lineStyle;
    private SimpleFeature currentFeature;
    private ArrayList<Coordinate> points;
    private Layer layer;
    private Style pointStyle;
    private DefaultFeatureCollection shapelist;

    public LineBuilder() {

        setStyle(Color.blue, 5);

    }

    public void start(Layer layer, DefaultFeatureCollection shapelist, Point2D worldPoint) {

        this.layer = layer;
        this.shapelist = shapelist;

        // add style to layer
        List<FeatureTypeStyle> fstyles = layer.getStyle().featureTypeStyles();
        fstyles.add(lineStyle.featureTypeStyles().get(0));
        fstyles.add(pointStyle.featureTypeStyles().get(0));

        // store point
        points = new ArrayList<>();
        points.add(new Coordinate(worldPoint.getX(), worldPoint.getY()));

        // create a first point
        Point point = geometryFactory.createPoint(new Coordinate(worldPoint.getX(), worldPoint.getY()));
        currentFeature = SimpleFeatureUtils.getLineFeature(point);

        shapelist.add(currentFeature);

    }

    public void addPoint(Point2D worldPoint) {

        throwIfNotDrawing();

        points.add(new Coordinate(worldPoint.getX(), worldPoint.getY()));
        LineString line = geometryFactory.createLineString(points.toArray(new Coordinate[points.size()]));

        currentFeature.setDefaultGeometry(line);

    }

    public void finish(Point2D worldPoint) {

        addPoint(worldPoint);

        System.out.println("Finish feature: " + currentFeature);
        System.out.println(currentFeature.getDefaultGeometry());

        currentFeature = null;
        points = null;

    }

    public void setStyle(Color color, int width) {
        if (isDrawing() == true) {
            throw new IllegalStateException("Cannot change style while drawing");
        }
        this.lineStyle = SimpleStyleBuilder.createLineStyle(color, width);
        this.pointStyle = SimpleStyleBuilder.createPointStyle(color, width);
    }

    public boolean isDrawing(){
        return currentFeature != null;
    }

    private void throwIfNotDrawing(){
        if (isDrawing() == false) {
            throw new IllegalStateException("Cannot perform this operation while not drawing");
        }
    }

}

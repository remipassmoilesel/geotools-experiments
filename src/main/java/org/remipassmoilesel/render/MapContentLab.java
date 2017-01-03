package org.remipassmoilesel.render;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.remipassmoilesel.utils.GuiUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by remipassmoilesel on 01/01/17.
 */
public class MapContentLab {

    public static void main(String[] args) throws IOException, FactoryException, TransformException {

        // retrieve a shape file and add it to a mapcontent
        String shapePath = "data/france-communes/communes-20160119.shp";
        Path shape = Paths.get(shapePath);

        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shape.toFile());
        SimpleFeatureStore shapeFileSource = (SimpleFeatureStore) dataStore.getFeatureSource();

        FeatureLayer shapeLayer = new FeatureLayer(shapeFileSource, SLD.createLineStyle(Color.blue, 0.2f));

        MapContent mapContent = new MapContent();
        mapContent.addLayer(shapeLayer);

        // render two images, with same area but different systems
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(mapContent);

        mapContent.getViewport().setMatchingAspectRatio(true);

        int renderedWidthPx = 500;
        int renderedHeightPx = 500;

        ReferencedEnvelope shapeBounds = shapeLayer.getBounds();
        ReferencedEnvelope envWgs84 = shapeBounds.transform(DefaultGeographicCRS.WGS84, true);
        ReferencedEnvelope envEd50 = shapeBounds.transform(CRS.decode("EPSG:4230"), true);

        System.out.println();
        System.out.println("envEd50");
        System.out.println(envEd50);
        System.out.println("envWgs84");
        System.out.println(envWgs84);

        // draw a wgs84 envelope
        BufferedImage img2 = new BufferedImage(renderedWidthPx, renderedHeightPx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d2 = (Graphics2D) img2.getGraphics();
        renderer.paint(g2d2, new Rectangle(renderedWidthPx, renderedHeightPx), envWgs84);

        GuiUtils.showImage("envWgs84", img2);

        // draw an ed50 envelope
        BufferedImage img = new BufferedImage(renderedWidthPx, renderedHeightPx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        renderer.paint(g2d, new Rectangle(renderedWidthPx, renderedHeightPx), envEd50);

        GuiUtils.showImage("envEd50", img);

        // draw original bounds
        BufferedImage img3 = new BufferedImage(renderedWidthPx, renderedHeightPx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d3 = (Graphics2D) img3.getGraphics();
        renderer.paint(g2d3, new Rectangle(renderedWidthPx, renderedHeightPx), shapeBounds);

        GuiUtils.showImage("original", img3);

        ReferencedEnvelope newEnvEd50 = envEd50.transform(DefaultGeographicCRS.WGS84, true);

        // draw transformed bounds
        BufferedImage img4 = new BufferedImage(renderedWidthPx, renderedHeightPx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d4 = (Graphics2D) img4.getGraphics();
        renderer.paint(g2d4, new Rectangle(renderedWidthPx, renderedHeightPx), newEnvEd50);

        GuiUtils.showImage("newEnvEd50", img4);
    }

}

package org.remipassmoilesel.render;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.remipassmoilesel.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Small trials on map rendering for Swing
 *
 * Goal is to create a custom Swing component, to experiment Geotools rendering
 *
 */
public class RenderLab extends JPanel {

    private BufferedImage contentImage;
    private Dimension contentDim;
    private ReferencedEnvelope mapBoundsToRender;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame();
            frame.setTitle("First rendering trial");
            frame.setSize(new Dimension(810, 610));
            frame.setLayout(new BorderLayout());

            RenderLab content = new RenderLab();
            frame.add(content, BorderLayout.CENTER);

            frame.setVisible(true);
        });

    }

    public RenderLab() throws HeadlessException {
        super();
        setSize(new Dimension(800, 600));
        new Thread(() -> {
            try {
                createImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createImage() throws IOException {

        // retrieve a shapefile and add it to a mapcontent
        Path shape = Paths.get("data/france-communes/communes-20160119.shp");

        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shape.toFile());
        SimpleFeatureSource shapeFileSource = dataStore
                .getFeatureSource();

        FeatureLayer layer = new FeatureLayer(shapeFileSource, SLD.createLineStyle(Color.blue, 0.2f));

        MapContent content = new MapContent();
        content.addLayer(layer);

        // for debug purposes
//        GuiUtils.showInWindow(content);

        // get a renderer instance
        GTRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(content);

        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderer.setJava2DHints(hints);

        renderer.addRenderListener(new RenderListener() {

            @Override
            public void featureRenderer(SimpleFeature feature) {
//                System.out.println(feature);
            }

            @Override
            public void errorOccurred(Exception e) {
                System.out.println(e);
            }
        });

        // create an image to draw into
        this.contentDim = new Dimension(800, 600);
        this.contentImage = new BufferedImage(contentDim.width, contentDim.height, BufferedImage.TYPE_INT_ARGB);

        System.out.println("Max bounds of layer:");
        // original layer max bounds: ReferencedEnvelope[-61.80976386945311 : 55.83669235015563, -21.389730741571007 : 51.08984197104998]
        System.out.println(layer.getBounds());

        // France metrop
        this.mapBoundsToRender = new ReferencedEnvelope(-5.40d, 9.91d, 41d, 51.08d, DefaultGeographicCRS.WGS84);

        // preserve ratio, optionnal
        //MapViewport vp = content.getViewport();
        //vp.setMatchingAspectRatio(true);

        Graphics2D g2d = contentImage.createGraphics();
        renderer.paint(g2d, new Rectangle(contentDim), mapBoundsToRender);

        this.repaint();

        System.out.println("End of rendering process");

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (contentImage != null) {

            Graphics2D g2d = (Graphics2D) g;

            // paint background
            g2d.setColor(new Color(255, 248, 210));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // paint map
            g2d.drawImage(contentImage, 0, 0, contentDim.width, contentDim.height, null);

        }

    }
}

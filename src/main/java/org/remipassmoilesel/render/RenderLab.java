package org.remipassmoilesel.render;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wms.WMSUtils;
import org.geotools.data.wms.WebMapServer;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.RenderListener;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.opengis.feature.simple.SimpleFeature;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Small trials on map rendering for Swing. Goal is to create a custom Swing component, to experiment Geotools rendering speed
 * <p>
 * You can drag the map and reset its position.
 * <p>
 * With a single layer it is possible to render the map on the fly with little work,
 * but with several layers optimisation is necessary.
 */
public class RenderLab extends JPanel implements MouseListener, MouseMotionListener {

    private final DefaultGeographicCRS crs;
    private final ReentrantLock renderLock;
    private final ReferencedEnvelope originalBoundsToRender;
    private Point2D imageTranslation;
    private BufferedImage contentImage;
    private Dimension contentDim;
    private ReferencedEnvelope mapBoundsToRender;
    private Point lastDragPoint;
    private MapContent mapContent;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // frame
            JFrame frame = new JFrame();
            frame.setTitle("First rendering trial");
            frame.setSize(new Dimension(810, 610));
            frame.setLayout(new BorderLayout());

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // panel where is displayed map
            RenderLab mapPanel = new RenderLab();
            frame.add(mapPanel, BorderLayout.CENTER);

            // button to reset map
            JButton resetButton = new JButton("Reset");
            resetButton.addActionListener((actionEvent) -> {
                mapPanel.resetContent();
            });
            frame.add(resetButton, BorderLayout.NORTH);

            frame.setVisible(true);
        });

    }


    public RenderLab() throws HeadlessException {
        super();
        setSize(new Dimension(800, 600));

        addMouseMotionListener(this);
        addMouseListener(this);

        this.crs = DefaultGeographicCRS.WGS84;

        imageTranslation = new Point2D.Double();

        // original layer max bounds: ReferencedEnvelope[-61.80976386945311 : 55.83669235015563, -21.389730741571007 : 51.08984197104998]

        // France metrop
        // this.mapBoundsToRender = new ReferencedEnvelope(-5.40d, 9.91d, 41d, 51.08d, DefaultGeographicCRS.WGS84);

        // Cotentin
        this.originalBoundsToRender = new ReferencedEnvelope(-2.90d, 1.32d, 47.60d, 50.41d, this.crs);
        this.mapBoundsToRender = new ReferencedEnvelope(originalBoundsToRender);

        this.renderLock = new ReentrantLock();

        try {
            setupMapContent();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        renderImageInThread(null);

    }

    private void renderImageInThread(Runnable whenFinished) {
        new Thread(() -> {
            renderImage();

            if (whenFinished != null) {
                whenFinished.run();
            }
        }).start();
    }

    private void setupMapContent() throws IOException, ServiceException {

        String shapePath = "data/france-communes/communes-20160119.shp";
        String wmsUrl = "http://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities";
        int wmsLayerIndex = 0;

        mapContent = new MapContent();

        // retrieve and add wms layer
        WebMapServer wms = new WebMapServer(new URL(wmsUrl));
        WMSCapabilities capabilities = wms.getCapabilities();
        Layer[] namedLayers = WMSUtils.getNamedLayers(capabilities);

        WMSLayer wmsLayer = new WMSLayer(wms, namedLayers[wmsLayerIndex]);
        mapContent.addLayer(wmsLayer);

        // retrieve a shape file and add it to a mapcontent
        Path shape = Paths.get(shapePath);

        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shape.toFile());
        SimpleFeatureSource shapeFileSource = dataStore
                .getFeatureSource();

        FeatureLayer shapeLayer = new FeatureLayer(shapeFileSource, SLD.createLineStyle(Color.blue, 0.2f));

        mapContent.addLayer(shapeLayer);

    }


    private void renderImage() {

        if (renderLock.tryLock() == false) {
            return;
        }

        // get a renderer instance
        GTRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(mapContent);

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

        // preserve ratio, optionnal
        // MapViewport vp = content.getViewport();
        // vp.setMatchingAspectRatio(true);

        Graphics2D g2d = contentImage.createGraphics();
        renderer.paint(g2d, new Rectangle(contentDim), mapBoundsToRender);

        this.repaint();

        System.out.println("End of rendering process");

        renderLock.unlock();

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // paint background
        g2d.setColor(new Color(255, 248, 210));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (contentImage != null) {

            // translate image if needed
            g2d.setTransform(AffineTransform.getTranslateInstance(imageTranslation.getX(), imageTranslation.getY()));

            // paint map
            g2d.drawImage(contentImage, 0, 0, contentDim.width, contentDim.height, null);

        }

    }

    @Override
    public void mouseDragged(MouseEvent e) {

        if (this.lastDragPoint == null) {
            this.lastDragPoint = e.getPoint();
            return;
        }

        // get map move in pixel
        Point mpos = e.getPoint();
        double mxpx = lastDragPoint.x - mpos.x;
        double mypx = lastDragPoint.y - mpos.y;

        // get map move in world unit
        double mxwld;
        double mywld;
        try {

            AffineTransform worldToScreen = RendererUtilities.worldToScreenTransform(mapBoundsToRender, new Rectangle(contentDim));
            AffineTransform screenToWorld = worldToScreen.createInverse();

            mxwld = mxpx * screenToWorld.getScaleX();
            mywld = mypx * screenToWorld.getScaleY();

        } catch (NoninvertibleTransformException e1) {
            throw new RuntimeException();
        }

        lastDragPoint = mpos;

        // adapt map bounds to render
        double x1 = mapBoundsToRender.getMinX() + mxwld;
        double x2 = mapBoundsToRender.getMaxX() + mxwld;
        double y1 = mapBoundsToRender.getMinY() + mywld;
        double y2 = mapBoundsToRender.getMaxY() + mywld;

        mapBoundsToRender.setBounds(new ReferencedEnvelope(x1, x2, y1, y2, crs));

        // adapt image translation
        imageTranslation.setLocation(imageTranslation.getX() - mxpx,
                imageTranslation.getY() - mypx);

        repaint();

    }

    @Override
    public void mouseReleased(MouseEvent e) {

        // render image
        renderImageInThread(() -> {
            // reset translation
            this.imageTranslation = new Point2D.Double();

            repaint();
        });
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // reset last drag point to avoid drag errors
        this.lastDragPoint = null;
    }

    /**
     * Reset display to original bounds
     */
    private void resetContent() {
        this.mapBoundsToRender = new ReferencedEnvelope(originalBoundsToRender);
        renderImageInThread(null);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }


    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

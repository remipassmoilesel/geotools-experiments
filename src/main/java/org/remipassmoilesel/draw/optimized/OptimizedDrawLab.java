package org.remipassmoilesel.draw.optimized;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wms.WMSUtils;
import org.geotools.data.wms.WebMapServer;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.WMSLayer;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.styling.SLD;
import org.geotools.styling.StyleFactory;
import org.remipassmoilesel.draw.LineBuilder;

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

/**
 * Here drawing is a little optimized. Only modified layers are rendered when a user draw on map.
 */
public class OptimizedDrawLab extends JPanel implements MouseListener, MouseMotionListener {

    private static final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private FeatureLayer drawLayer;
    private final LineBuilder lineBuilder;
    private DefaultFeatureCollection shapesCollection;
    private final MapLayersManager layersManager;
    private final ReferencedEnvelope originalBoundsToRender;
    private final DefaultGeographicCRS crs;
    protected Point2D imageTranslation;
    private Point lastDragPoint;
    private ReferencedEnvelope mapBoundsToRender;
    private int drawLayerId;

    public static void main(String[] args) {
        launchWindow();
    }

    public static void launchWindow() {

        SwingUtilities.invokeLater(() -> {

            // frame
            JFrame frame = new JFrame();
            frame.setTitle("Optimized draw lab");
            frame.setSize(new Dimension(810, 610));
            frame.setLayout(new BorderLayout());

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            OptimizedDrawLab mapPanel = new OptimizedDrawLab();

            // panel where is displayed map
            frame.add(mapPanel, BorderLayout.CENTER);

            // button to reset map
            JButton resetButton = new JButton("Reset");
            resetButton.addActionListener((actionEvent) -> {
                mapPanel.resetMapPosition();
            });
            frame.add(resetButton, BorderLayout.NORTH);

            frame.setVisible(true);
        });
    }

    public OptimizedDrawLab() {

        setSize(new Dimension(800, 600));

        addMouseMotionListener(this);
        addMouseListener(this);

        this.crs = DefaultGeographicCRS.WGS84;

        // Cotentin
        this.originalBoundsToRender = new ReferencedEnvelope(-2.90d, 1.32d, 47.60d, 50.41d, this.crs);
        this.mapBoundsToRender = new ReferencedEnvelope(originalBoundsToRender);

        imageTranslation = new Point2D.Double();

        layersManager = new MapLayersManager();

        // add a line builder
        lineBuilder = new LineBuilder();

        try {
            setupMapContent();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        renderAllLayersLater(() -> {
            repaint();
        });

    }

    private void setupMapContent() throws IOException, ServiceException {

        String shapePath = "data/france-communes/communes-20160119.shp";
        String wmsUrl = "http://ows.terrestris.de/osm/service?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities";
        int wmsLayerIndex = 0;

        // retrieve and add wms layer
        WebMapServer wms = new WebMapServer(new URL(wmsUrl));

        WMSCapabilities capabilities = wms.getCapabilities();
        Layer[] namedLayers = WMSUtils.getNamedLayers(capabilities);

        WMSLayer wmsLayer = new WMSLayer(wms, namedLayers[wmsLayerIndex]);

        layersManager.addLayer(wmsLayer);

        // retrieve a shape file and add it to a mapcontent
        Path shape = Paths.get(shapePath);

        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shape.toFile());
        SimpleFeatureSource shapeFileSource = dataStore
                .getFeatureSource();

        FeatureLayer shapeLayer = new FeatureLayer(shapeFileSource, SLD.createLineStyle(Color.darkGray, 0.2f));
        layersManager.addLayer(shapeLayer);

        // add a layer to draw on
        drawLayerId = 2;
        shapesCollection = new DefaultFeatureCollection();
        drawLayer = new FeatureLayer(shapesCollection, sf.getDefaultStyle());

        layersManager.addLayer(drawLayer);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // paint background
        g2d.setColor(new Color(255, 248, 210));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int width = this.getWidth();
        int height = this.getHeight();

        // translate image if needed
        g2d.setTransform(AffineTransform.getTranslateInstance(imageTranslation.getX(), imageTranslation.getY()));

        for (BufferedImage image : layersManager.getRenderedImages()) {

            // paint map
            g2d.drawImage(image, 0, 0, width, height, null);

        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {

        // if control down stop dragging
        if (e.isControlDown() == true) {

            renderAllLayersLater(() -> {
                this.imageTranslation = new Point2D.Double();
                repaint();
            });

            return;
        }

        Point2D worldPosition;

        int width = this.getWidth();
        int height = this.getHeight();

        // transform mouse position
        try {
            AffineTransform worldToScreen = RendererUtilities.worldToScreenTransform(mapBoundsToRender, new Rectangle(width, height));
            AffineTransform screenToWorld = worldToScreen.createInverse();

            worldPosition = screenToWorld.transform(e.getPoint(), null);

        } catch (NoninvertibleTransformException e1) {
            throw new RuntimeException("Error while converting positions: " + e1.getMessage());
        }


        if (e.getClickCount() < 2) {

            // 1: create line if not already drawing
            if (lineBuilder.isDrawing() == false) {
                lineBuilder.start(drawLayer, shapesCollection, worldPosition);
            }

            // 2: add points if already drawing
            else {
                lineBuilder.addPoint(worldPosition);
            }
        }

        // 3: terminate line if double click
        else if (e.getClickCount() > 1 && lineBuilder.isDrawing()) {
            lineBuilder.finish(worldPosition);
        }

        renderLayerLater(drawLayerId, () -> {
            repaint();
        });

    }

    public void renderLayerLater(int layerId, Runnable whenFinished) {
        layersManager.setRenderedDimensions(getSize());
        layersManager.setMapBoundsToRender(mapBoundsToRender);
        layersManager.renderLayerLater(layerId, whenFinished);
    }

    private void renderAllLayersLater(Runnable whenFinished) {
        layersManager.setRenderedDimensions(getSize());
        layersManager.setMapBoundsToRender(mapBoundsToRender);
        layersManager.renderAllLayersLater(whenFinished);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        // drag map when control down
        if (e.isControlDown() == true) {

            if (this.lastDragPoint == null) {
                this.lastDragPoint = e.getPoint();
                return;
            }

            int width = this.getWidth();
            int height = this.getHeight();

            // get map move in pixel
            Point mpos = e.getPoint();
            double mxpx = lastDragPoint.x - mpos.x;
            double mypx = lastDragPoint.y - mpos.y;

            // get map move in world unit
            double mxwld;
            double mywld;
            try {

                AffineTransform worldToScreen = RendererUtilities.worldToScreenTransform(mapBoundsToRender, new Rectangle(width, height));
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

    }

    @Override
    public void mousePressed(MouseEvent e) {

        // only move when control down
        if (e.isControlDown() == false) {
            return;
        }

        // reset last drag point to avoid drag errors
        this.lastDragPoint = null;
    }

    /**
     * Reset display to original bounds
     */
    protected void resetMapPosition() {
        this.mapBoundsToRender = new ReferencedEnvelope(originalBoundsToRender);
        renderAllLayersLater(null);
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

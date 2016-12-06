package org.remipassmoilesel.partialrenderer;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.remipassmoilesel.utils.ThreadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Display a map by using partial cache system
 * <p>
 * Cache is managed by a RenderedPartialFactory. This partial factory produce portions of map and store it.
 */
public class CacheMapPane extends JPanel {

    /**
     * Cache render that render an image to paint in JPanel
     */
    private final CacheRenderer renderer;

    /**
     * Lock to prevent too much thread rendering
     */
    private final ReentrantLock lock;

    /**
     * Last time of rendering in ms
     */
    private long lastRender = -1;

    /**
     * Minimum interval between rendering in ms
     */
    private long renderMinIntervalMs = 50;

    /**
     * Or ULC point to render from
     */
    private Point2D worldPosition;

    /**
     * Map to render
     */
    private MapContent map;
    private BufferedImage contentImage;

    public CacheMapPane(MapContent map) {
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        this.map = map;
        this.renderer = new CacheRenderer(map);

        lock = new ReentrantLock();

        this.addComponentListener(new RefreshMapComponentListener());
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(contentImage, 0, 0, null);

    }

    public void refreshMap() {

        if (checkRenderInterval() == false) {
            return;
        }

        ThreadManager.runLater(() -> {

            if(lock.tryLock() == false){
                //System.err.println("Already rendering !");
                return;
            }

            try{
                //System.out.println("Render task launched");

                Dimension dim = CacheMapPane.this.getSize();

                if (dim.width < 1 || dim.height < 1) {
                    System.out.println("Screen bounds too small");
                    return;
                }

                BufferedImage newContent = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
                Graphics g = newContent.getGraphics();

                renderer.setWorldPosition(worldPosition);
                renderer.render(g, dim);

                contentImage = newContent;

                repaint();

                //System.out.println("Render task complete");
            } finally {
                lock.unlock();
            }

        });

    }

    private boolean checkRenderInterval() {
        boolean render = System.currentTimeMillis() - lastRender > renderMinIntervalMs;
        if (render) {
            lastRender = System.currentTimeMillis();
        }

        return render;
    }

    public void initializeMap() {
        refreshMap();
    }

    public void setWorldPosition(Point2D worldPoint) {
        this.worldPosition = worldPoint;
    }

    public Point2D getWorldPosition() {
        return new Point2D.Double(worldPosition.getX(), worldPosition.getY());
    }

    public double getPartialSideDg() {
        return renderer.getPartialSideDg();
    }

    public int getPartialSidePx() {
        return renderer.getPartialSidePx();
    }

    public void setPartialSideDg(double partialSideDg) {
        renderer.setPartialSideDg(partialSideDg);
    }

    public class RefreshMapComponentListener implements ComponentListener{

        @Override
        public void componentResized(ComponentEvent e) {
            refreshMap();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            refreshMap();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            refreshMap();
        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }
    }
}

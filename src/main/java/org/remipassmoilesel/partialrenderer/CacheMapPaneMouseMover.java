package org.remipassmoilesel.partialrenderer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * Created by remipassmoilesel on 05/12/16.
 */
public class CacheMapPaneMouseMover extends MouseAdapter {

    private final CacheMapPane pane;
    private double move = 0.1;
    private Point lastPosition;

    public CacheMapPaneMouseMover(CacheMapPane pane) {
        this.pane = pane;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);

        if (lastPosition == null) {
            lastPosition = e.getPoint();
            return;
        }

        // get mouse move
        Point m = e.getPoint();
        double mx = lastPosition.getX() - m.getX();
        double my = lastPosition.getY() - m.getY();

        // scale it
        int psx = pane.getPartialSidePx();
        double psd = pane.getPartialSideDg();
        mx = mx * psd / psx;
        my = my * psd / psx;

        // adapt world position
        Point2D p = pane.getWorldPosition();
        pane.setWorldPosition(new Point2D.Double(p.getX() + mx, p.getY() - my));

        pane.repaint();

        lastPosition = m;

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        lastPosition = null;
    }
}

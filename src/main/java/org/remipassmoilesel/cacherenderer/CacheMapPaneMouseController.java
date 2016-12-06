package org.remipassmoilesel.cacherenderer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

/**
 * Move map when user drag it on component and change scale when user use mouse wheel
 */
public class CacheMapPaneMouseController extends MouseAdapter {

    private final CacheMapPane pane;
    private double move = 0.1;
    private Point lastPosition;

    public CacheMapPaneMouseController(CacheMapPane pane) {
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

        pane.refreshMap();

        lastPosition = m;

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        lastPosition = null;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);

        double zoomUnit = 0.3;

        if (e.getWheelRotation() < 0) {
            zoomUnit = -zoomUnit;
        }

        pane.setPartialSideDg(pane.getPartialSideDg() + zoomUnit);
        pane.refreshMap();
    }
}

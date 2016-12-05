package org.remipassmoilesel.partialrenderer;

import org.geotools.map.event.MapAdapter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * Created by remipassmoilesel on 05/12/16.
 */
public class CachedMapPaneMouseMover extends MouseAdapter {

    private final CachedMapPane pane;
    private double move = 0.05;
    private Point lastPosition;

    public CachedMapPaneMouseMover(CachedMapPane pane) {
        this.pane = pane;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);

        if (lastPosition == null) {
            lastPosition = e.getPoint();
            return;
        }

        Point m = e.getPoint();
        double mx = lastPosition.getX() - m.getX() > 0 ? +move : -move;
        double my = lastPosition.getY() - m.getY() > 0 ? -move : +move;

        //System.out.println("pane.addMouseMotionListener(new MouseAdapter() {");
        //System.out.println(mx);
        //System.out.println(my);

        Point2D p = pane.getWorldPosition();

        //System.out.println(p);

        pane.setWorldPosition(new Point2D.Double(p.getX() + mx, p.getY() + my));

        pane.repaint();

        lastPosition = m;

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        lastPosition = null;
    }
}

package org.remipassmoilesel.worldfile;

import org.geotools.data.WorldFileWriter;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.AbstractSingleCRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.factory.epsg.CartesianAuthorityFactory;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Create a worldfile for a tile with a given position, transformation and CRS.
 */
public class TileWorldFileWriter {


    private AbstractSingleCRS crs;
    private double x;
    private double y;

    public TileWorldFileWriter() {
        this.crs = CartesianAuthorityFactory.GENERIC_2D;
        this.x = 0d;
        this.y = 0d;
    }

    public TileWorldFileWriter(double x, double y) {
        super();
        this.x = x;
        this.y = y;
    }

    public TileWorldFileWriter(double x, double y, AbstractSingleCRS crs) {
        super();
        this.x = x;
        this.y = y;
        this.crs = crs;
    }

    public void write(Path destination) throws IOException {
        AffineTransform translate = new AffineTransform(1, 0, 0, 1, this.x, this.y);
        new WorldFileWriter(destination.toFile(), translate);
    }

    /**
     * Create a world file for the tile "p" with the appropriate extension.
     *
     * @param p the final tile (png or tiff file)
     * @param x
     * @param y
     * @throws IOException
     */
    public static void createForTile(Path p, double x, double y) throws IOException {
        TileWorldFileWriter writer = new TileWorldFileWriter(x, y);

        String strPath = p.toString();
        String dest = strPath.substring(0, strPath.length() - 1) + "w";

        writer.write(Paths.get(dest));
    }
}


package org.remipassmoilesel.utils;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.MapContent;
import org.geotools.styling.*;
import org.geotools.swing.JMapFrame;
import org.opengis.filter.FilterFactory2;
import org.opengis.style.ContrastMethod;
import org.remipassmoilesel.swing.CustomMapFrame;

import javax.swing.*;
import java.io.IOException;

/**
 * Utilities for tutorials
 */
public class GuiUtils {

    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    /**
     * Show a MapContent in a window
     *
     * @param content
     */
    public static void showInWindow(MapContent content) {

        SwingUtilities.invokeLater(() -> {

            // Create a JMapFrame with a menu to choose the display style for the
            JMapFrame frame = new JMapFrame(content);
            frame.setSize(800, 600);
            frame.enableStatusBar(true);
            frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN, JMapFrame.Tool.RESET);
            frame.enableToolBar(true);
            frame.enableLayerTable(true);
            frame.setVisible(true);

        });

    }

    /**
     * Show a MapContent in a window
     *
     * @param content
     */
    public static void showInCustomWindow(MapContent content) {

        SwingUtilities.invokeLater(() -> {

            // Create a JMapFrame with a menu to choose the display style for the
            CustomMapFrame frame = new CustomMapFrame(content);
            frame.setSize(800, 600);
            frame.enableStatusBar(true);
            frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN, JMapFrame.Tool.RESET);
            frame.enableToolBar(true);
            frame.enableLayerTable(true);
            frame.setVisible(true);

        });

    }

    public static org.geotools.styling.Style getDefaultRGBRasterStyle(AbstractGridCoverage2DReader reader) {

        GridCoverage2D cov = null;
        try {
            cov = reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }
        // We need at least three bands to create an RGB style
        int numBands = cov.getNumSampleDimensions();
        if (numBands < 3) {
            return null;
        }
        // Get the names of the bands
        String[] sampleDimensionNames = new String[numBands];
        for (int i = 0; i < numBands; i++) {
            GridSampleDimension dim = cov.getSampleDimension(i);
            sampleDimensionNames[i] = dim.getDescription().toString();
        }
        final int RED = 0, GREEN = 1, BLUE = 2;
        int[] channelNum = {-1, -1, -1};
        // We examine the band names looking for "red...", "green...", "blue...".
        // Note that the channel numbers we record are indexed from 1, not 0.
        for (int i = 0; i < numBands; i++) {
            String name = sampleDimensionNames[i].toLowerCase();
            if (name != null) {
                if (name.matches("red.*")) {
                    channelNum[RED] = i + 1;
                } else if (name.matches("green.*")) {
                    channelNum[GREEN] = i + 1;
                } else if (name.matches("blue.*")) {
                    channelNum[BLUE] = i + 1;
                }
            }
        }
        // If we didn't find named bands "red...", "green...", "blue..."
        // we fall back to using the first three bands in order
        if (channelNum[RED] < 0 || channelNum[GREEN] < 0 || channelNum[BLUE] < 0) {
            channelNum[RED] = 1;
            channelNum[GREEN] = 2;
            channelNum[BLUE] = 3;
        }
        // Now we create a RasterSymbolizer using the selected channels
        SelectedChannelType[] sct = new SelectedChannelType[cov.getNumSampleDimensions()];
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NONE);
        for (int i = 0; i < 3; i++) {
            sct[i] = sf.createSelectedChannelType(String.valueOf(channelNum[i]), ce);
        }
        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN], sct[BLUE]);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);

    }

    public static org.geotools.styling.Style getDefaultGrayScaleRasterStyle(AbstractGridCoverage2DReader reader, Integer bandNum) {

        GridCoverage2D cov = null;
        try {
            cov = reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }

        // Now we create a RasterSymbolizer using the selected channels
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NONE);
        SelectedChannelType sct = sf.createSelectedChannelType(String.valueOf(bandNum), ce);
        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);

    }

}


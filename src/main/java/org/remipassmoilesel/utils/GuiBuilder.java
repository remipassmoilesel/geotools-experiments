package org.remipassmoilesel.utils;

import org.geotools.map.Layer;
import org.geotools.map.MapContent;

/**
 * Utility for tutorials
 *
 * Example: GuiBuilder.newMap("Map 1").addLayer(layer).show();
 */
public class GuiBuilder {

    private final MapContent mapContent;

    public static GuiBuilder newMap(String title) {
        return new GuiBuilder(title);
    }

    public GuiBuilder(String title){
        this.mapContent = new MapContent();
        mapContent.setTitle(title);
    }

    public GuiBuilder addLayer(Layer layer) {
        mapContent.addLayer(layer);
        return this;
    }

    public GuiBuilder show(){
        GuiUtils.showInWindow(mapContent);
        return this;
    }

}

package org.remipassmoilesel.geopackage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by remipassmoilesel on 08/11/16.
 */
public class ProjectPropertie {

    private String key;
    private String value;

    public ProjectPropertie(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [" + key + ": " + value + "]";
    }
}

package org.remipassmoilesel.geotools;

import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.xml.NullEntityResolver;

/**
 * Example of Geotools library config.
 */
public class GeoToolsConfiguration {

    public static void main(String[] args) {
        System.out.println("GeoTools.getVersion()");
        System.out.println(GeoTools.getVersion());

        Hints.putSystemDefault(Hints.ENTITY_RESOLVER, NullEntityResolver.INSTANCE);
        Hints.removeSystemDefault(Hints.ENTITY_RESOLVER);
    }

}

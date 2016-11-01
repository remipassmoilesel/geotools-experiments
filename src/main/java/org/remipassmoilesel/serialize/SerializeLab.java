package org.remipassmoilesel.serialize;

import org.geotools.data.FeatureReader;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.awt.*;
import java.beans.XMLEncoder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple serialisation trial of datastore
 */
public class SerializeLab {

    public static void main(String[] args) throws IOException {

        // retrieve a shapefile and add it to a mapcontent
        Path shape = Paths.get("data/france-communes/communes-20160119.shp");

        FileDataStore dataStore = FileDataStoreFinder.getDataStore(shape.toFile());
        SimpleFeatureSource shapeFileSource = dataStore
                .getFeatureSource();

        FeatureLayer layer = new FeatureLayer(shapeFileSource, SLD.createLineStyle(Color.blue, 0.2f));

        MapContent mapContent = new MapContent();
        mapContent.addLayer(layer);

        XMLEncoder out = new XMLEncoder(new FileOutputStream("data/mapcontent.serialized"));

        FeatureReader<SimpleFeatureType,SimpleFeature> reader = dataStore.getFeatureReader();

        SimpleFeatureType schema = dataStore.getSchema();
        int acount = schema.getAttributeCount();

        // save fields
        Object[] ctr = new Object[acount];
        for (int j = 0; j < acount; j++) {
            AttributeDescriptor desc = schema.getDescriptor(j);
            ctr[j] = desc.getType().getBinding().getSimpleName() + ":::" + desc.getName().toString();
        }
        out.writeObject(ctr);

        int i = 0;
        int max = 50;

        while(reader.hasNext()){

            SimpleFeature feature = reader.next();

            ctr = new Object[acount];
            for (int j = 0; j < acount; j++) {
                ctr[j] = feature.getAttribute(j).toString();
            }

            // save geom
            out.writeObject(ctr);

            if(i > max){
                break;
            }

            i++;
        }

        out.close();

    }

}

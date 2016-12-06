package org.remipassmoilesel.partialrenderer;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.FactoryException;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by remipassmoilesel on 06/12/16.
 */
public class RenderedPartialStore {

    private final Dao dao;

    private static long inMemoryUsedPartials = 0;
    private static long inDatabaseUsedPartials = 0;
    private static long addedInDatabase = 0;

    private final Path databasePath;
    private final JdbcPooledConnectionSource connectionSource;

    private ArrayList<RenderedPartialImage> inMemory;

    public RenderedPartialStore(Path databasePath) throws SQLException {
        this.databasePath = databasePath;
        inMemory = new ArrayList<>();

        this.connectionSource = new JdbcPooledConnectionSource("jdbc:h2:./" + databasePath + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE", "", "");
        connectionSource.setMaxConnectionAgeMillis(5 * 60 * 1000);
        connectionSource.setTestBeforeGet(true);
        connectionSource.initialize();

        // create tables
        TableUtils.createTableIfNotExists(connectionSource, RenderedPartialImage.class);

        // create dao object
        this.dao = DaoManager.createDao(connectionSource, RenderedPartialImage.class);
    }

    public RenderedPartialImage getPartial(ReferencedEnvelope area) throws SQLException {

        RenderedPartialImage searched = new RenderedPartialImage(null, area);

        // search an existing partial in memory with a valid image
        // if found, return it
        int index = inMemory.indexOf(searched);
        if (index != -1) {
            RenderedPartialImage part = inMemory.get(index);
            if (part.getImage() != null) {
                inMemoryUsedPartials++;
                return part;
            }
        }

        // no valid partial where found in memory, check database
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(RenderedPartialImage.PARTIAL_X1_FIELD_NAME, area.getMinX());
        fields.put(RenderedPartialImage.PARTIAL_Y1_FIELD_NAME, area.getMinY());
        fields.put(RenderedPartialImage.PARTIAL_X2_FIELD_NAME, area.getMaxX());
        fields.put(RenderedPartialImage.PARTIAL_Y2_FIELD_NAME, area.getMaxY());
        fields.put(RenderedPartialImage.PARTIAL_CRS_FIELD_NAME, RenderedPartialImage.crsToId(area.getCoordinateReferenceSystem()));

        List<RenderedPartialImage> results = dao.queryForFieldValues(fields);

        // one result found, prepare it and return it
        if (results.size() == 1) {

            inDatabaseUsedPartials++;

            RenderedPartialImage part = results.get(0);
            part.setupImageSoftReference();
            part.updateImageDimensions();
            try {
                part.setUpCRS();
            } catch (FactoryException e) {
                throw new SQLException("Invalid partial, wrong CRS: " + part.getCrsId(), e);
            }

            return part;
        }

        // too much results, throw exception
        if (results.size() > 1) {
            throw new SQLException("More than one result found: " + results.size());
        }

        // no result found, return it
        return null;
    }

    public void addPartial(RenderedPartialImage part) throws SQLException {

        if (part.getImage() == null) {
            throw new NullPointerException("Image is null");
        }

        dao.create(part);
        inMemory.add(part);

        addedInDatabase ++;
    }

    public static long getInDatabaseUsedPartials() {
        return inDatabaseUsedPartials;
    }

    public static long getInMemoryUsedPartials() {
        return inMemoryUsedPartials;
    }

    public static long getAddedInDatabase() {
        return addedInDatabase;
    }
}

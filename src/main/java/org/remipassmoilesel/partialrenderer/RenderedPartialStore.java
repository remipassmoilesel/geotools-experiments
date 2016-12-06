package org.remipassmoilesel.partialrenderer;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.FactoryException;

import java.nio.file.Path;
import java.nio.file.Paths;
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

    private final Path databasePath;
    private final JdbcPooledConnectionSource connectionSource;

    private ArrayList<RenderedPartial> inMemory;

    public RenderedPartialStore(Path databasePath) throws SQLException {
        this.databasePath = databasePath;
        inMemory = new ArrayList<>();

        this.connectionSource = new JdbcPooledConnectionSource("jdbc:h2:./" + databasePath + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE", "", "");
        connectionSource.setMaxConnectionAgeMillis(5 * 60 * 1000);
        connectionSource.setTestBeforeGet(true);
        connectionSource.initialize();

        // create tables
        TableUtils.createTableIfNotExists(connectionSource, RenderedPartial.class);

        // create dao object
        this.dao = DaoManager.createDao(connectionSource, RenderedPartial.class);
    }

    public RenderedPartial getPartial(ReferencedEnvelope area) throws SQLException {

        RenderedPartial searched = new RenderedPartial(null, area);

        // search an existing partial in memory with a valid image
        // if found, return it
        int index = inMemory.indexOf(searched);
        if (index != -1) {
            RenderedPartial part = inMemory.get(index);
            if (part.getImage() != null) {
                inMemoryUsedPartials++;
                return part;
            }
        }

        // no valid partial where found in memory, check database
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(RenderedPartial.PARTIAL_X1_FIELD_NAME, area.getMinX());
        fields.put(RenderedPartial.PARTIAL_Y1_FIELD_NAME, area.getMinY());
        fields.put(RenderedPartial.PARTIAL_X2_FIELD_NAME, area.getMaxX());
        fields.put(RenderedPartial.PARTIAL_Y2_FIELD_NAME, area.getMaxY());
        fields.put(RenderedPartial.PARTIAL_CRS_FIELD_NAME, RenderedPartial.crsToId(area.getCoordinateReferenceSystem()));

        List<RenderedPartial> rs = dao.queryForFieldValues(fields);

        // one result found, prepare it and return it
        if (rs.size() == 1) {

            inDatabaseUsedPartials++;

            RenderedPartial part = rs.get(0);
            part.setupImageSoftReference();

            try {
                part.setUpCRS();
            } catch (FactoryException e) {
                throw new SQLException("Invalid partial, wrong CRS: " + part.getCrsId(), e);
            }
            return part;
        }

        // too much results, throw exception
        if (rs.size() > 1) {
            throw new SQLException("More than one result found: " + rs.size());
        }

        // no result found, return it
        return null;
    }

    public void addPartial(RenderedPartial part) throws SQLException {

        if (part.getImage() == null) {
            throw new NullPointerException("Image is null");
        }

        dao.create(part);
        inMemory.add(part);
    }

    public static long getInDatabaseUsedPartials() {
        return inDatabaseUsedPartials;
    }

    public static long getInMemoryUsedPartials() {
        return inMemoryUsedPartials;
    }
}

package org.remipassmoilesel.geopackage;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.jdbc.JDBCDataStore;
import org.remipassmoilesel.utils.SqlUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by remipassmoilesel on 08/11/16.
 */
public class ProjectPropertiesDAO {

    private final static String TABLE_NAME = "project_properties";
    private Connection connection;

    public ProjectPropertiesDAO(Path p) throws IOException, SQLException {
        initializeDatabase(p);
    }

    private void initializeDatabase(Path p) throws IOException, SQLException {

        Map params = new HashMap();
        params.put("dbtype", "geopkg");
        params.put("database", p.toString());

        JDBCDataStore datastore = (JDBCDataStore) DataStoreFinder.getDataStore(params);
        connection = datastore.getConnection(Transaction.AUTO_COMMIT);

        // try to find the properties table, or create it
        if (SqlUtils.getTableList(connection).contains(TABLE_NAME) == false) {
            SqlUtils.runScript("/create_properties.sql", connection);
        }

    }

    public ArrayList<ProjectPropertie> getValues() {

        PreparedStatement prepare = null;
        ArrayList<ProjectPropertie> results = new ArrayList<>();
        try {

            prepare = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + ";");
            prepare.execute();

            ResultSet rs = prepare.getResultSet();
            while (rs.next()) {
                ProjectPropertie prop = new ProjectPropertie(rs.getObject(1).toString(), rs.getObject(2).toString());
                results.add(prop);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public boolean updateValue(String key, String value) {

        PreparedStatement prepare = null;
        ArrayList<ProjectPropertie> results = new ArrayList<>();
        try {

            prepare = connection.prepareStatement(
                    "UPDATE  " + TABLE_NAME + " SET property_key=?, property_value=? " +
                            "WHERE property_key=?;");
            prepare.setString(1, key);
            prepare.setString(2, value);
            prepare.setString(3, key);

            int uc = prepare.executeUpdate();
            return uc > 0;

        } catch (SQLException e) {
            return false;
        }


    }

    public boolean addValue(String key, String value) {

        PreparedStatement prepare = null;
        ArrayList<ProjectPropertie> results = new ArrayList<>();
        try {

            prepare = connection.prepareStatement(
                    "INSERT INTO  " + TABLE_NAME + " (property_key, property_value) " +
                            "VALUES (?, ?);");
            prepare.setString(1, key);
            prepare.setString(2, value);

            int uc = prepare.executeUpdate();

            return uc > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }


    }

}

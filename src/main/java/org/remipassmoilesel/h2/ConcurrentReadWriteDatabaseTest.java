package org.remipassmoilesel.h2;

import org.remipassmoilesel.utils.SQLUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Simple test case to show concurrency errors that happen with SQLite (Geopackage) (one connection writing at a time)
 * <p>
 * These problems disappear with H2
 */
public class ConcurrentReadWriteDatabaseTest {

    private static final String H2 = "h2";
    private static final String SQLITE = "sqlite";

    /**
     * Change this var to switch between SQLite and H2
     */
    private static final String CURRENT_DRIVER = H2;

    public static final String TEST_TABLE_NAME = "test_table";
    private static int tasksDone = 0;
    private static Path databasePath;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:" + CURRENT_DRIVER + ":./" + databasePath, "", "");
    }

    public static void main(String[] args) throws Exception {

        // check if drivers are present
        Class.forName("org.h2.Driver");
        Class.forName("org.sqlite.JDBC");

        System.out.println("Launching all tasks");

        Path rootDir = Paths.get("data", "databaseTest");
        Files.createDirectories(rootDir);

        databasePath = rootDir.resolve("test." + CURRENT_DRIVER);
        Files.deleteIfExists(databasePath);

        System.out.println("Preparing database ...");

        createTestDatabase();

        System.out.println("Launching tasks ...");

        for (int i = 0; i < 20; i++) {

            new Thread(new WriteAccessTest(getConnection(), (id) -> {
                tasksDone++;
                return null;
            })).start();

            new Thread(new ReadAccessTest(getConnection(), (id) -> {
                tasksDone++;
                return null;
            })).start();

        }

        System.out.println("Launching done");

        do {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } while (tasksDone < 40);

        System.err.println("Finished !");

    }

    public static void createTestDatabase() throws Exception {

        SQLUtils.processTransaction(getConnection(), (conn) -> {

            PreparedStatement createStat = conn.prepareStatement("CREATE TABLE " + TEST_TABLE_NAME + " (columnA TEXT NOT NULL, columnB TEXT NOT NULL);");
            createStat.execute();

            for (int i = 0; i < 8000; i++) {

                PreparedStatement stat = conn.prepareStatement("INSERT INTO " + TEST_TABLE_NAME + " (columnA, columnB) VALUES(?,?);");
                stat.setString(1, "fakeValue_" + System.nanoTime());
                stat.setString(2, "fakeValue_" + System.nanoTime());

                stat.execute();

            }

            return null;
        });

    }


}

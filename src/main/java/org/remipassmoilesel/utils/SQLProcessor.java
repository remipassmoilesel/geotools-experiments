package org.remipassmoilesel.utils;

import java.sql.Connection;

/**
 * Interface used with SQL operations
 * <p>
 * "Function" is not used here because it throw SQLException
 */
public interface SQLProcessor {
    public Object process(Connection conn) throws Exception;
}

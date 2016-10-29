package org.remipassmoilesel.utils;

import org.geotools.util.Utilities;
import org.geotools.xml.xsi.XSISimpleTypes;

/**
 * Created by remipassmoilesel on 25/10/16.
 */
public class GeotoolsUtilities {

    public static void main(String[] args) {

        String string1 = "a";
        String string2 = null;

        // equals provides a null safe equals check
        Utilities.equals(string1, string2); // null safe equals check
        Utilities.equals(null, "Hello"); // false
        Utilities.equals(null, null); // true!

        // deepEquals will check the contents of arrays
        Utilities.deepEquals(new double[] { 1.0 }, new double[] { 1.0 });

        // deepToString will print out objects and arrays
        Utilities.deepToString(new double[] { 1.0, Math.PI, Math.E });

        String value = "aaaaaaaa";
        Integer seed = 55586;

        String object = "bbbbbbbb";

        // when implementing your own object the following are handy
        Utilities.hash(value, seed);
        Utilities.equals(value, object);
        Utilities.ensureNonNull("parameter", object);

    }

}

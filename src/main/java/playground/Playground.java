package playground;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by remipassmoilesel on 19/10/16.
 */
public class Playground {

    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    public static void main(String[] args) throws FactoryException {


        System.out.println(System.nanoTime());

        Path p = Paths.get("/path/to/file.abm");
        System.out.println(p);
        System.out.println(p.getParent());

        System.out.println(CRS.decode("EPSG:404000"));

        System.out.println(CRS.parseWKT("LOCAL_CS[\"Wildcard 2D cartesian plane in metric unit\", \n" +
                "  LOCAL_DATUM[\"Unknown\", 0], \n" +
                "  UNIT[\"m\", 1.0], \n" +
                "  AXIS[\"x\", EAST], \n" +
                "  AXIS[\"y\", NORTH], \n" +
                "  AUTHORITY[\"EPSG\",\"404000\"]]"));
    }


}

package playground;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.FilterFactory2;

import java.text.SimpleDateFormat;

/**
 * Created by remipassmoilesel on 19/10/16.
 */
public class Playground {

    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    public static void main(String[] args) {


        System.out.println(ff.literal("a"));


    }
    


}

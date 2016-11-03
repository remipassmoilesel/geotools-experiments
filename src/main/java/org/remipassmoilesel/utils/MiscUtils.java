package org.remipassmoilesel.utils;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by remipassmoilesel on 03/11/16.
 */
public class MiscUtils {

    public static void showClassPath() {

        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader) cl).getURLs();

        for (URL url : urls) {
            System.out.println(url.getFile());
        }

    }
}

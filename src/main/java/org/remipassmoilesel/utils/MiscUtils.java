package org.remipassmoilesel.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.ArrayList;

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

    /**
     * Conversion d'image vers Byte
     *
     * @param img
     * @return
     */
    public static byte[] imageToByte(BufferedImage img) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = null;
        try {
            ImageIO.write(img, "jpg", baos);
            baos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

}

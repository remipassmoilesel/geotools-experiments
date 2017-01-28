package org.remipassmoilesel.image;

import it.geosolutions.jaiext.JAIExt;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Created by remipassmoilesel on 27/01/17.
 */
public class ImageIOLab {

    static {
        JAIExt.initJAIEXT();
    }

    public static void main(String[] args) throws IOException {

        Rectangle sourceRegion = new Rectangle(800, 800, 1600, 1600); // The region you want to extract

        Path source = Paths.get("data/iroise-raster/RASTERMARINE_OPENDATA_IROISE/RasterMARINE400_MC_WGS84_AI_36   .jpg :-O ");

        if(Files.isRegularFile(source) == false){
            throw new IOException("Fle does not exist: " + source);
        };

        ImageInputStream stream = ImageIO.createImageInputStream(source);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

        if (readers.hasNext()) {
            ImageReader reader = readers.next();
            reader.setInput(stream);

            ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceRegion(sourceRegion); // Set region

            BufferedImage image = reader.read(0, param); // Will read only the region specified

            ImageIO.write(image, "png", Paths.get("writed.png").toFile());
        }



    }

}

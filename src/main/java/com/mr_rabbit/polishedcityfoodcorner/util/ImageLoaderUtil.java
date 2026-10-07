package com.mr_rabbit.polishedcityfoodcorner.util;

import javafx.scene.image.Image;

import java.io.File;
import java.io.InputStream;


public class ImageLoaderUtil {

    private static final String FALLBACK_RESOURCE = "/IconAndPicture/iconlogo.png";

    private ImageLoaderUtil() {
    }

    public static Image loadProductImage(String imagePath) {
        if (imagePath != null && !imagePath.isBlank()) {
            // Looks like a full path from an imported file -> load straight from disk.
            File file = new File(imagePath);
            if (file.isAbsolute() && file.exists()) {
                return new Image(file.toURI().toString());
            }
            // Otherwise treat it as a bundled classpath resource in /FoodItemPic.
            String resourcePath = imagePath.startsWith("/") ? imagePath : "/FoodItemPic/" + imagePath;
            InputStream stream = ImageLoaderUtil.class.getResourceAsStream(resourcePath);
            if (stream != null) {
                return new Image(stream);
            }
        }
        return new Image(ImageLoaderUtil.class.getResourceAsStream(FALLBACK_RESOURCE));
    }
}

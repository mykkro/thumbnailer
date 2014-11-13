package cz.cvut.kbss.mondis.thumbnailer;

import java.awt.image.BufferedImage;

/**
 * Created by myrousz on 11/12/14.
 */
public class Util {

    /**
     * Tests two Buffered images whether they are equally sized and have the same content.
     * Not to be used to compare images after JPEG compression.
     *
     * @param image1
     * @param image2
     * @return true or false
     */
    public static boolean areImagesEqual(BufferedImage image1, BufferedImage image2) {
        if (!areImagesEqualInSize(image1, image2)) {
            return false;
        }
        for (int x = 1; x < image2.getWidth(); x++) {
            for (int y = 1; y < image2.getHeight(); y++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tests two Buffered images whether they are equally sized.
     *
     * @param image1
     * @param image2
     * @return true or false
     */
    public static boolean areImagesEqualInSize(BufferedImage image1, BufferedImage image2) {
        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight()) {
            return false;
        }
        return true;
    }

}

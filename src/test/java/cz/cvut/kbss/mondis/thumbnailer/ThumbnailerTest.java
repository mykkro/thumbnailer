package cz.cvut.kbss.mondis.thumbnailer;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

import static org.junit.Assert.*;

public class ThumbnailerTest {

    private Thumbnailer t;

    @org.junit.Before
    public void setUp() throws Exception {
        t = new Thumbnailer(100, 100, "/home/myrousz/.thumbnailer-test", 1000000);
    }

    @org.junit.After
    public void tearDown() throws Exception {

    }

    BufferedImage loadImage(String resourceName) throws Exception {
        BufferedImage originalImage = ImageIO.read(ThumbnailCacheTest.class.getResource(resourceName));
        BufferedImage img;
        //img = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        //img.setData(originalImage.getData());
        return originalImage;
    }


    @Test
    public void testGetImageThumbnail() throws Exception {
        URL url = new URL("http://www.lokigames.com/press/downloads/tux.jpg");
        BufferedImage thumb = t.getThumbnail(url);
        assertNotNull("The thumbnail should not be null", thumb);
        //BufferedImage thumb2 = loadImage("tux-thumb.jpg");
        //assertTrue("The thumbnail should be the same as cached thumbnail", Util.areImagesEqual(thumb, thumb2));

    }

    @Test
    public void testGetVideoThumbnail() throws Exception {
        URL url = new URL("http://techslides.com/demos/sample-videos/small.mp4");
        BufferedImage thumb = t.getThumbnail(url);
        assertNotNull("The thumbnail should not be null", thumb);
        //BufferedImage thumb2 = loadImage("tux-thumb.jpg");
        //assertTrue("The thumbnail should be the same as cached thumbnail", Util.areImagesEqual(thumb, thumb2));
    }

}
package cz.cvut.kbss.mondis.thumbnailer;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

import static org.junit.Assert.*;

public class ThumbnailCacheTest {

    private ThumbnailCache tc;

    @org.junit.Before
    public void setUp() throws Exception {
        // TODO different directory for test cache...
        tc = new ThumbnailCache("/home/myrousz/.thumbnailer-test");
    }

    @org.junit.After
    public void tearDown() throws Exception {
        tc.removeAll();
    }

    BufferedImage loadImage(String resourceName) throws Exception {
        BufferedImage originalImage = ImageIO.read(ThumbnailCacheTest.class.getResource(resourceName));
        BufferedImage img;
        //img = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        //img.setData(originalImage.getData());
        return originalImage;
    }


    @Test
    public void putGetRemoveThumbnailTest() throws Exception {
        URL url = new URL("http://example.org");
        // Tux image downloaded from: http://www.lokigames.com/press/downloads/tux.jpg
        assertFalse("Cache should not contain this URL", tc.hasThumbnail(url));

        assertNull("Cache should not contain this URL", tc.getThumbnail(url));

        BufferedImage thumb = loadImage("tux-thumb.jpg");
        tc.putThumbnail(url, thumb);
        assertTrue("Cache should contain a thumbnail with this URL", tc.hasThumbnail(url));

        BufferedImage thumbFromCache = tc.getThumbnail(url);
        assertTrue("The thumbnails should be the same size before and after putting to the cache", Util.areImagesEqualInSize(thumb, thumbFromCache));

        tc.removeThumbnail(url);
        assertFalse("The cache should not contain a thumbnail with this URL", tc.hasThumbnail(url));
    }


}
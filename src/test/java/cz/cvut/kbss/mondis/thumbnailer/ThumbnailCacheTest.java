package cz.cvut.kbss.mondis.thumbnailer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class ThumbnailCacheTest {

    private ThumbnailCache tc;
    private Path tempDirPath;

    private Log log = LogFactory.getLog(ThumbnailCacheTest.class);

    @org.junit.Before
    public void setUp() throws Exception {
        tempDirPath = Files.createTempDirectory("thumbnail-cache-test");
        log.info("Test thumbnail cache is stored in "+tempDirPath.toString());
        tc = new ThumbnailCache(tempDirPath.toString());
    }

    @org.junit.After
    public void tearDown() throws Exception {
        tc.removeAll();
        FileUtils.deleteDirectory(tempDirPath.toFile());

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
        String u = url.toString();
        // Tux image downloaded from: http://www.lokigames.com/press/downloads/tux.jpg
        assertFalse("Cache should not contain this URL", tc.hasThumbnail(u));

        assertNull("Cache should not contain this URL", tc.getThumbnail(u));

        BufferedImage thumb = loadImage("tux-thumb.jpg");
        tc.putThumbnail(u, thumb);
        assertTrue("Cache should contain a thumbnail with this URL", tc.hasThumbnail(u));

        BufferedImage thumbFromCache = tc.getThumbnail(u);
        assertTrue("The thumbnails should be the same size before and after putting to the cache", Util.areImagesEqualInSize(thumb, thumbFromCache));

        tc.removeThumbnail(u);
        assertFalse("The cache should not contain a thumbnail with this URL", tc.hasThumbnail(u));
    }


}
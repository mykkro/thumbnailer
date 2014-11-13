package cz.cvut.kbss.mondis.thumbnailer;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

public class DownloaderTest {

    private Downloader dl;

    @org.junit.Before
    public void setUp() throws Exception {
        dl = new Downloader(1000000);
    }

    @org.junit.After
    public void tearDown() throws Exception {

    }

    @Test
    public void testDownloadFile() throws Exception {
        URL url = new URL("http://www.lokigames.com/press/downloads/tux.jpg");
        DownloadedFile dfile = dl.downloadFile(url);
        assertNotNull("The file descriptor should not be null", dfile);
        File local = new File(DownloaderTest.class.getResource("tux.jpg").toURI());
        assertEquals("The file size should match", dfile.getContentLength(), local.length());
        assertEquals("The MIME type should be image/jpeg", dfile.getMimeType(), "image/jpeg");
        dfile.delete();
    }

    @Test(expected = Downloader.FileTooLargeException.class)
    public void testDownloadLargeFile() throws Exception {
        URL url = new URL("http://clips.vorwaerts-gmbh.de/big_buck_bunny.ogv");
        DownloadedFile dfile = dl.downloadFile(url);
        dfile.delete();
    }

    @Test(expected = Downloader.UnsupportedMediaTypeException.class)
    public void testDownloadInvalidMedia() throws Exception {
        URL url = new URL("http://google.com");
        DownloadedFile dfile = dl.downloadFile(url);
        dfile.delete();
    }
}
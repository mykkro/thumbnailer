package cz.cvut.kbss.mondis.thumbnailer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Properties;


/**
 * Created by myrousz on 11/11/14.
 */
public class Thumbnailer  {

    private int thumbWidth;
    private int thumbHeight;
    private String thumbnailCacheDir;
    private int downloadMaxSize;
    private String ffmpegCommand = "ffmpeg";

    private ThumbnailCache thumbnailCache;

    private Log log = LogFactory.getLog(Thumbnailer.class);

    /**
     * Creates a Thumbnailer instance
     */
    public Thumbnailer(int w, int h, String thumbsDir, int dlFileMaxSize) {
        thumbWidth = w;
        thumbHeight = h;
        thumbnailCacheDir = thumbsDir;
        downloadMaxSize = dlFileMaxSize;
        thumbnailCache = new ThumbnailCache(thumbnailCacheDir);
    }

    public static Properties getDefaultProperties() throws IOException {
        final Properties properties = new Properties();
        properties.load(Thumbnailer.class.getResourceAsStream("/thumbnailer.properties"));
        return properties;
    }


    public static Thumbnailer fromProperties(Properties p) {
        int thumbWidth = Integer.parseInt(p.getProperty("thumbWidth"));
        int thumbHeight = Integer.parseInt(p.getProperty("thumbHeight"));
        String cacheDir = p.getProperty("thumbnailCacheDir");
        int downloadMaxSize = Integer.parseInt(p.getProperty("downloadMaxSize"));
        return new Thumbnailer(thumbWidth, thumbHeight, cacheDir, downloadMaxSize);
    }

    /**
     * Gets a thumbnail for the given URL resource.
     *
     * @param url
     * @return
     */
    public BufferedImage getThumbnail(URL url) throws IOException {
        BufferedImage thumb = thumbnailCache.getThumbnail(url);
        if(thumb == null) {
            // thumbnail is not in the cache...
            thumb = generateThumbnail(url);
            thumbnailCache.putThumbnail(url, thumb);
        }
        return thumb;
    }

    /**
     * Generates a thumbnail for resource identified by the URL.
     * @param url
     * @return generated thumbnail as BufferedImage
     */
    public BufferedImage generateThumbnail(URL url) {
        log.debug("Generating thumbnail for URL: " + url);
        Downloader dl = new Downloader(downloadMaxSize);
        BufferedImage out = null;
        DownloadedFile downloadedFile = null;
        try {
            // download file to temporary location...
            downloadedFile = dl.downloadFile(url);
            // if it is image, create a thumbnail...
            if (downloadedFile.type.equals("image")) {
                out = createImageThumbnail(downloadedFile.getFile());
                return out;
            } else if (downloadedFile.type.equals("video")) {
                out = createVideoThumbnail(downloadedFile.getFile());
                return out;
            } else {
                // unknown media type...
                return null;
            }
        }
        catch(Exception e) {
            log.error(e);
            return null;
        }
        finally {
            if(downloadedFile != null) {
                downloadedFile.delete();
            }
        }
    }

    public BufferedImage createImageThumbnail(File imgFile) throws IOException {
        BufferedImage img = ImageIO.read(imgFile);
        BufferedImage thumbImg = Scalr.resize(
                img,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_EXACT,
                this.thumbWidth,
                this.thumbHeight,
                Scalr.OP_ANTIALIAS);
        return thumbImg;
    }

    /*
    Example command line:
    ffmpeg -i /tmp/thumbnailer-downloader4919221186540797301.mp4 -s 100X100 -ss 00:00:01.00 -vcodec mjpeg -vframes 1 -f image2 /tmp/thumbnailer-videothumb1266500720689049848.jpg
     */
    public BufferedImage createVideoThumbnail(File vid) throws IOException {
        String pathToSrcFile = vid.getAbsolutePath();
        File tempThumbFile = File.createTempFile("thumbnailer-videothumb", ".jpg");
        String tempFilePath = tempThumbFile.getAbsolutePath();
        String dimension = thumbHeight+"X"+thumbWidth;

        ProcessBuilder pb = new ProcessBuilder(
                ffmpegCommand,
                "-i",pathToSrcFile,
                "-s",dimension,
                "-ss","00:00:01.00",
                "-vcodec","mjpeg",
                "-vframes","1",
                "-f","image2",
                tempFilePath,
                "-y" // force overwrite temp file...
        );

        pb.redirectErrorStream(true);

        //System.out.println("---------"+pb.command());

        try {
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            // consume output...
            String line = null;
            while ((line = br.readLine()) != null) {
                //System.out.println("---------" + line);
            }
            p.waitFor();
            // reload the generated thumbnail
            BufferedImage thumb = ImageIO.read(tempThumbFile);
            return thumb;
        }
        catch(Exception ex) {
            log.error(ex);
        }
        finally {
            tempThumbFile.delete();
        }
        return null;

    }

    /* do something... */
    public static void main(String[] args) throws Exception {
        //URL url = new URL("http://www.w3schools.com/html/mov_bbb.ogg");
        URL url = new URL("http://mtgcommander.net/images/Daretti,%20Scrap%20Savant.jpg");
        final Properties properties = Thumbnailer.getDefaultProperties();
        Thumbnailer tb = Thumbnailer.fromProperties(properties);
        BufferedImage img = tb.getThumbnail(url);
        LogFactory.getLog(Thumbnailer.class).info("Generated thumbnail");
    }
}

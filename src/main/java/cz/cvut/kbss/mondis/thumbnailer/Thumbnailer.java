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

    private String thumbnailCacheDir;
    private int downloadMaxSize;
    private String ffmpegCommand = "ffmpeg";
    private ThumbnailProperties defaultThumbnailProperties;

    private ThumbnailCache thumbnailCache;

    private Log log = LogFactory.getLog(Thumbnailer.class);

    /**
     * Creates a Thumbnailer instance
     */
    public Thumbnailer(ThumbnailProperties tp, String thumbsDir, int dlFileMaxSize) {
        this.defaultThumbnailProperties = tp;
        thumbnailCacheDir = thumbsDir;
        downloadMaxSize = dlFileMaxSize;
        thumbnailCache = new ThumbnailCache(thumbnailCacheDir);
    }

    public ThumbnailProperties getDefaultThumbnailProperties() {
        return defaultThumbnailProperties;
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
        ThumbnailProperties tp = new ThumbnailProperties(thumbWidth, thumbHeight, ThumbnailScaleMode.STRETCH);
        return new Thumbnailer(tp, cacheDir, downloadMaxSize);
    }

    /**
     * Gets a thumbnail for the given URL resource.
     *
     * @param url
     * @return
     */
    public BufferedImage getThumbnail(URL url) throws IOException {
        return getThumbnail(url, getDefaultThumbnailProperties());
    }

    /**
     * Gets a thumbnail for the given URL resource.
     *
     * @param url
     * @return
     */
    public BufferedImage getThumbnail(URL url, ThumbnailProperties tp) throws IOException {
        String key = getThumbKey(url, tp);
        BufferedImage thumb = thumbnailCache.getThumbnail(key);
        if(thumb == null) {
            // thumbnail is not in the cache...
            thumb = generateThumbnail(url, tp);
            thumbnailCache.putThumbnail(key, thumb);
        }
        return thumb;
    }

    public String getThumbKey(URL url, ThumbnailProperties tp) {
        String key = url.toString()+"@"+tp.getWidth()+"x"+tp.getHeight();
        return key;
    }

    /**
     * Generates a thumbnail for resource identified by the URL.
     * @param url
     * @return generated thumbnail as BufferedImage
     */
    public BufferedImage generateThumbnail(URL url, ThumbnailProperties tp) {
        log.debug("Generating thumbnail for URL: " + url);
        Downloader dl = new Downloader(downloadMaxSize);
        BufferedImage out = null;
        DownloadedFile downloadedFile = null;
        try {
            // download file to temporary location...
            downloadedFile = dl.downloadFile(url);
            // if it is image, create a thumbnail...
            if (downloadedFile.type.equals("image")) {
                out = createImageThumbnail(downloadedFile.getFile(), tp);
                return out;
            } else if (downloadedFile.type.equals("video")) {
                out = createVideoThumbnail(downloadedFile.getFile(), tp);
                return out;
            } else if (downloadedFile.type.equals("audio")) {
                out = createAudioThumbnail(downloadedFile.getFile(), tp);
                return out;
            } else {
                // unknown media type...
            }
        }
        catch(Exception e) {
            log.error(e);
        }
        finally {
            if(downloadedFile != null) {
                downloadedFile.delete();
            }
        }
        if(out == null) {
            try {
                // create default thumbnail for all other file types
                out = createUnknownThumbnail(tp);
            }
            catch(IOException e) {
                log.error(e);
            }
        }
        return out;
    }

    private BufferedImage scaleImage(BufferedImage img, ThumbnailProperties tp) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        int desiredWidth = tp.getWidth();
        int desiredHeight = tp.getHeight();
        Scalr.Mode scaleMode = Scalr.Mode.FIT_EXACT;
        if(desiredWidth>0 && desiredHeight<0) {
            // height not set, keep the width...
            desiredHeight = (int)(desiredWidth*imgHeight/imgWidth);
            scaleMode = Scalr.Mode.FIT_TO_WIDTH;
        } else if(desiredWidth<0 && desiredHeight>0) {
            // width not set, keep the height...
            desiredWidth = (int)(desiredHeight*imgWidth/imgHeight);
            scaleMode = Scalr.Mode.FIT_TO_HEIGHT;
        } else if(desiredWidth<0 && desiredHeight<0) {
            // no dimensions set, use original size
            desiredWidth = imgWidth;
            desiredHeight = imgHeight;
        }
        BufferedImage thumbImg = Scalr.resize(
                img,
                Scalr.Method.QUALITY,
                scaleMode,
                desiredWidth,
                desiredHeight,
                Scalr.OP_ANTIALIAS);
        return thumbImg;
    }

    public BufferedImage createImageThumbnail(File imgFile, ThumbnailProperties tp) throws IOException {
        BufferedImage img = ImageIO.read(imgFile);
        return scaleImage(img, tp);
    }

    public BufferedImage createAudioThumbnail(File soundFile, ThumbnailProperties tp) throws IOException {
        BufferedImage img = ImageIO.read(Thumbnailer.class.getResource("type-sound.jpg"));
        return scaleImage(img, tp);
    }

    public BufferedImage createUnknownThumbnail(ThumbnailProperties tp) throws IOException {
        BufferedImage img = ImageIO.read(Thumbnailer.class.getResource("type-unknown.jpg"));
        return scaleImage(img, tp);
    }

    /*
    Example command line:
    ffmpeg -i /tmp/thumbnailer-downloader4919221186540797301.mp4 -s 100X100 -ss 00:00:01.00 -vcodec mjpeg -vframes 1 -f image2 /tmp/thumbnailer-videothumb1266500720689049848.jpg
     */
    public BufferedImage createVideoThumbnail(File vid, ThumbnailProperties tp) throws IOException {
        String pathToSrcFile = vid.getAbsolutePath();
        File tempThumbFile = File.createTempFile("thumbnailer-videothumb", ".jpg");
        String tempFilePath = tempThumbFile.getAbsolutePath();
        String filter = "scale='"+tp.getWidth()+":"+tp.getHeight()+"'";

        ProcessBuilder pb = new ProcessBuilder(
                ffmpegCommand,
                "-i",pathToSrcFile,
                "-filter:v",filter,
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

    public String checkFFMPEGVersion() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegCommand,
                "-version"
        );

        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            // consume output...
            String line = null;
            String firstLine = null;
            while ((line = br.readLine()) != null) {
                if(firstLine == null) firstLine = line;
                //System.out.println("---------" + line);
            }
            p.waitFor();
            return firstLine.startsWith("ffmpeg version") ? firstLine.replaceFirst("ffmpeg version ", "") : null;
        }
        catch(Exception ex) {
            log.error(ex);
        }
        return null;

    }

    /* do something... */
    public static void main(String[] args) throws Exception {
        //URL url = new URL("http://www.w3schools.com/html/mov_bbb.ogg");
        //URL url = new URL("http://techslides.com/demos/sample-videos/small.webm");
        URL url = new URL("https://ia600504.us.archive.org/16/items/AppleComputersQuicktimeSample/sample.mp4");
        //URL url = new URL("http://www.engr.colostate.edu/me/facil/dynamics/files/drop.avi");
        //URL url = new URL("http://mtgcommander.net/images/Daretti,%20Scrap%20Savant.jpg");
        //URL url = new URL("https://archive.org/download/testmp3testfile/mpthreetest.mp3");
        //URL url = new URL("https://www.google.com");
        //URL url = new URL("http://upload.wikimedia.org/wikipedia/en/4/45/ACDC_-_Back_In_Black-sample.ogg");
        //URL url = new URL("http://localhost:8080/logo-nomime.gif");
        final Properties properties = Thumbnailer.getDefaultProperties();
        Thumbnailer tb = Thumbnailer.fromProperties(properties);
        String version = tb.checkFFMPEGVersion();
        System.out.println("FFMPEG available: "+version);
        BufferedImage img = tb.getThumbnail(url);
        LogFactory.getLog(Thumbnailer.class).info("Generated thumbnail");

        ThumbnailProperties tp2 = new ThumbnailProperties(200, -1, ThumbnailScaleMode.STRETCH);
        BufferedImage img2 = tb.getThumbnail(url, tp2);

        ThumbnailProperties tp3 = new ThumbnailProperties(-1, 200, ThumbnailScaleMode.STRETCH);
        BufferedImage img3 = tb.getThumbnail(url, tp3);

        ThumbnailProperties tp4 = new ThumbnailProperties(-1, -1, ThumbnailScaleMode.STRETCH);
        BufferedImage img4 = tb.getThumbnail(url, tp4);
    }
}

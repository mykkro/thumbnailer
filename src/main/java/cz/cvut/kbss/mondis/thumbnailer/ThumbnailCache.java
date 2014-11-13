package cz.cvut.kbss.mondis.thumbnailer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;

/**
 * Created by myrousz on 11/12/14.
 */
public class ThumbnailCache {

    private String thumbnailCacheDir;

    public ThumbnailCache(String thumbnailCacheDir) {
        this.thumbnailCacheDir = thumbnailCacheDir;
    }

    /**
     * Helper method to convert thumbnail key (URL) to its path.
     * @param url
     * @return
     */
    String urlToPath(URL url) {
        String filename="";
        try {
            filename = URLEncoder.encode(url.toString(), "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return thumbnailCacheDir + "/" + filename + ".jpg";
    }

    /**
     * Retrieves a thumbnail from cache by its key. Returns null if no thumbnail found.
     *
     * @param url
     * @return
     */
    public BufferedImage getThumbnail(URL url) {
        File f = new File(urlToPath(url));
        BufferedImage thumb = null;
        if(f.isFile()) {
            try {
                BufferedImage originalImage = ImageIO.read(f);
                //thumb = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                //thumb.setData(originalImage.getData());
                thumb = originalImage;
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return thumb;
    }

    /**
     * Checks if cache contains a thumbnail with a given key.
     *
     * @param url
     * @return
     */
    public boolean hasThumbnail(URL url) {
        File f = new File(urlToPath(url));
        return f.isFile();
    }

    /**
     * Stores thumbnail to cache. Thumbnail is saved as a JPG file.
     *
     * @param url the key
     * @param thumbnail the image
     */
    public void putThumbnail(URL url, BufferedImage thumbnail) throws IOException {
        File f = new File(urlToPath(url));
        ImageIO.write(thumbnail, "jpg", f);
    }

    /**
     * Deletes thumbnail from cache.
     * @param url
     */
    public void removeThumbnail(URL url) {
        File f = new File(urlToPath(url));
        f.delete();
    }

    /**
     * Clears entire cache.
     */
    public void removeAll() throws IOException {
        FileUtils.cleanDirectory(new File(thumbnailCacheDir));
    }

}

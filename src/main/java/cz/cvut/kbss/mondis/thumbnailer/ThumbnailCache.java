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
     * @param key
     * @return
     */
    String keyToPath(String key) {
        String filename="";
        try {
            filename = URLEncoder.encode(key, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return thumbnailCacheDir + "/" + filename + ".jpg";
    }

    /**
     * Retrieves a thumbnail from cache by its key. Returns null if no thumbnail found.
     *
     * @param key
     * @return
     */
    public BufferedImage getThumbnail(String key) {
        File f = new File(keyToPath(key));
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
     * @param key
     * @return
     */
    public boolean hasThumbnail(String key) {
        File f = new File(keyToPath(key));
        return f.isFile();
    }

    /**
     * Stores thumbnail to cache. Thumbnail is saved as a JPG file.
     *
     * @param key the key
     * @param thumbnail the image
     */
    public void putThumbnail(String key, BufferedImage thumbnail) throws IOException {
        File f = new File(keyToPath(key));
        ImageIO.write(thumbnail, "jpg", f);
    }

    /**
     * Deletes thumbnail from cache.
     * @param key
     */
    public void removeThumbnail(String key) {
        File f = new File(keyToPath(key));
        f.delete();
    }

    /**
     * Clears entire cache.
     */
    public void removeAll() throws IOException {
        FileUtils.cleanDirectory(new File(thumbnailCacheDir));
    }

}

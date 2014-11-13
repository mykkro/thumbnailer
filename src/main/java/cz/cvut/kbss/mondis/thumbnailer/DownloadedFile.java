package cz.cvut.kbss.mondis.thumbnailer;

import java.io.File;
import java.net.URL;

/**
 * Created by myrousz on 11/12/14.
 */
public class DownloadedFile {
    URL url;
    File file;
    String mimeType;
    String type;
    long contentLength;

    public DownloadedFile(URL url, File file, String mimeType, String type, long contentLength) {
        this.url = url;
        this.file = file;
        this.mimeType = mimeType;
        this.type = type;
        this.contentLength = contentLength;
    }

    public URL getURL() {
        return url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getType() {
        return type;
    }

    public long getContentLength() {
        return contentLength;
    }

    public File getFile() {
        return file;
    }

    public void delete() {
        file.delete();
    }
}

package cz.cvut.kbss.mondis.thumbnailer;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.URI;
import java.net.URL;

/**
 * Created by myrousz on 11/12/14.
 */
public class Downloader {

    public class FileTooLargeException extends Exception {}

    public class UnknownContentLengthException extends Exception {}

    public class EmptyContentException extends Exception {}

    public class UnsupportedMediaTypeException extends Exception {}

    // Maximum allowed file size (10M)
    public final int MAX_FILE_SIZE;

    public Downloader(int maxFileSize) {
        this.MAX_FILE_SIZE = maxFileSize;

    }

    /**
     * Downloads file from URL to a temporary location.
     * @param url
     * @return
     */
    public DownloadedFile downloadFile(URL url) throws Exception {
        // keep the file extension...
        String uri = url.toURI().toASCIIString();
        String extension = uri.substring(uri.lastIndexOf("."));
        File f = File.createTempFile("thumbnailer-downloader", extension);
        return downloadFile(url, f);
    }


    /**
     * Downloads file from URL.
     * @param url
     * @return
     */
    public DownloadedFile downloadFile(URL url, File f) throws Exception {

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url.toURI());
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        // check file type and size
        DownloadedFile downloadedFile = null;
        try {
            downloadedFile = checkResponse(url, f, entity);
        }
        catch(Exception e) {
            httpget.abort();
            httpclient.getConnectionManager().shutdown();
            throw e;
        }

        // download the file...
        InputStream instream = null;
        try {
            instream = entity.getContent();
            downloadContent(instream, new FileOutputStream(f));
        } catch (IOException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            httpget.abort();
            throw ex;
        } finally {
            if(instream != null) instream.close();
        }
        httpclient.getConnectionManager().shutdown();
        return downloadedFile;
    }

    DownloadedFile checkResponse(URL url, File f, HttpEntity entity) throws Exception {
        if (entity == null) {
            throw new EmptyContentException();
        }

        // test content type in header...
        ContentType contentType = ContentType.get(entity);
        String mimeType = contentType.getMimeType();
        String type = getMediaType(mimeType);
        if(type == null) {
            throw new UnsupportedMediaTypeException();
        }

        // test content length...
        long contentLength = entity.getContentLength();
        if(contentLength<0) {
            throw new UnknownContentLengthException();
        } else if(contentLength>MAX_FILE_SIZE) {
            throw new FileTooLargeException();
        }
        return new DownloadedFile(url, f, mimeType, type, contentLength);
    }

    void downloadContent(InputStream is, OutputStream os) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        byte[] buffer = new byte[4096];
        int length;
        while((length = bis.read(buffer)) > 0) {
            bos.write(buffer, 0, length);
        }
        bis.close();
        bos.close();

    }

    String getMediaType(String mimeType) {
        if(isImage(mimeType)) {
            // image - download it
            return "image";
        } else if(isVideo(mimeType)) {
            // video - download it
            return "video";
        } else {
            return null;
        }
    }

    boolean isImage(String mimetype) {
        final String[] mimetypes = new String[]{
                "image/jpeg",
                "image/png",
                "image/gif"
        };
        for(String mt : mimetypes) {
            if(mt.equals(mimetype)) return true;
        }
        return false;
    }

    boolean isVideo(String mimetype) {
        final String[] mimetypes = new String[]{
                "video/mp4",
                "video/ogg",
                "video/webm",
                "video/x-flv",
                "video/quicktime",
                "video/mp4",
                "video/mpeg",
                "video/x-ms-wmv"
        };
        for(String mt : mimetypes) {
            if(mt.equals(mimetype)) return true;
        }
        return false;
    }
}

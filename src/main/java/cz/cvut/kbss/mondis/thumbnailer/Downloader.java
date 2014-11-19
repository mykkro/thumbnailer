package cz.cvut.kbss.mondis.thumbnailer;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.net.URLConnection;

/**
 * Created by myrousz on 11/12/14.
 */
public class Downloader {

    public class FileTooLargeException extends Exception {}

    public class UnknownContentLengthException extends Exception {}

    public class EmptyContentException extends Exception {}

    public class UnsupportedMediaTypeException extends Exception {}

    private Log log = LogFactory.getLog(Downloader.class);

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
        String mimeType = null;
        String type;
        long contentLength = 0;
        try {
            log.debug("Checking resource: "+url.toString());
            if (entity == null) {
                throw new EmptyContentException();
            }

            // test content type in header...
            ContentType contentType = ContentType.get(entity);
            log.debug("Content-Type: "+contentType);
            if(contentType != null) {
                mimeType = contentType.getMimeType();
            }
            // test content length...
            contentLength = entity.getContentLength();
            if(contentLength<0) {
                // handle separately...
                ////throw new UnknownContentLengthException();
            } else if(contentLength>MAX_FILE_SIZE) {
                throw new FileTooLargeException();
            }
        }
        catch(Exception e) {
            httpget.abort();
            httpclient.getConnectionManager().shutdown();
            throw e;
        }

        // download the file...
        InputStream instream = null;
        boolean isComplete = true;
        try {
            instream = entity.getContent();
            OutputStream os =  new FileOutputStream(f);
            BufferedInputStream bis = new BufferedInputStream(instream);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            byte[] buffer = new byte[4096];
            int length;
            int totalLength = 0;
            while((length = bis.read(buffer)) > 0) {
                // if we need to do some MIME magic...
                if(totalLength==0 && mimeType==null) {
                    mimeType = magicGuessMimetype(buffer);
                }
                bos.write(buffer, 0, length);
                totalLength += length;
                if(totalLength > MAX_FILE_SIZE) {
                    log.debug("Maximum allowed file size exceeded!");
                    isComplete = false;
                    break;
                }
            }
            bis.close();
            bos.close();
        } catch (IOException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            httpget.abort();
            throw ex;
        } finally {
            if(instream != null) instream.close();
        }
        if(!isComplete) {
            throw new FileTooLargeException();
        }
        httpclient.getConnectionManager().shutdown();
        type = getMediaType(mimeType);
        if(type == null) {
            throw new UnsupportedMediaTypeException();
        }
        return new DownloadedFile(url, f, mimeType, type, contentLength);
    }

    String magicGuessMimetype(byte[] buffer) {
        try {
            // use jmimemagic...
            Magic parser = new Magic();
            // getMagicMatch accepts Files or byte[],
            // which is nice if you want to test streams
            MagicMatch match = parser.getMagicMatch(buffer);
            return match.getMimeType();
        }
        catch(Exception e) {
            return null;
        }
    }


    String getMediaType(String mimeType) {
        if(isImage(mimeType)) {
            // image - download it
            return "image";
        } else if(isVideo(mimeType)) {
            // video - download it
            return "video";
        } else if(isAudio(mimeType)) {
            // audio - download it
            return "audio";
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

    boolean isAudio(String mimetype) {
        final String[] mimetypes = new String[]{
                "audio/aac", // .aac
                "audio/mp4", // .mp4 .m4a
                "audio/mpeg", // .mp1 .mp2 .mp3 .mpg .mpeg
                "audio/ogg", // .oga .ogg
                "application/ogg",
                "audio/wav", // .wav
                "audio/webm" // .webm
        };
        for(String mt : mimetypes) {
            if(mt.equals(mimetype)) return true;
        }
        return false;
    }
}

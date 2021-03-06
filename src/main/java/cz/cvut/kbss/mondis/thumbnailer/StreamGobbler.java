package cz.cvut.kbss.mondis.thumbnailer;

// from: http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2

import java.util.*;
import java.io.*;

public class StreamGobbler extends Thread
{
    InputStream is;
    String type;

    public StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                System.out.println(type + ">" + line);
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
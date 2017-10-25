package asdcp.crawler;

import edu.uci.ics.crawler4j.url.WebURL;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Set;

public class IOUtils {
    private static final String LINKS_FILE_NAME = "crawlerData\\links.txt";
    private static final String TEXT_FILE_NAME = "crawlerData\\text.txt";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static OutputStreamWriter textOutputStreamWriter;
    private static OutputStreamWriter linksOutputStreamWriter;

    public IOUtils() {
        try {
            textOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(TEXT_FILE_NAME));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getTextFileName() {
        return TEXT_FILE_NAME;
    }

    public static OutputStreamWriter getTextOutputStreamWriter() {
        return textOutputStreamWriter;
    }

    public static void createResources(){
        try {
            textOutputStreamWriter = new OutputStreamWriter(new FileOutputStream( TEXT_FILE_NAME));
            linksOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(LINKS_FILE_NAME));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void closeResources(){
        try {
            textOutputStreamWriter.close();
            linksOutputStreamWriter.close();
            System.out.println("Resources closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeText(String text, String url){
        try {
            textOutputStreamWriter.write(LINE_SEPARATOR);
            textOutputStreamWriter.write("Page with url " + url + " text: " + LINE_SEPARATOR);
            textOutputStreamWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeLinks(Set<WebURL> links, String url){
        if (links != null){
            try {
                linksOutputStreamWriter.write(LINE_SEPARATOR);
                linksOutputStreamWriter.write("Page with url " + url + " links: " + LINE_SEPARATOR);
                for (WebURL webURL : links) {
                    String urlStr = webURL.getURL();
                    linksOutputStreamWriter.write(urlStr + LINE_SEPARATOR);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

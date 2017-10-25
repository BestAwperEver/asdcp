package asdcp.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.regex.Pattern;
import asdcp.HTMLParser;

public class BasicCrawler extends WebCrawler {
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");

    private static String domen;
    private static OutputStreamWriter textOutputStreamWriter;

    public static void setDomen(String domen) {
        BasicCrawler.domen = domen;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith(domen);
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);
        textOutputStreamWriter = IOUtils.getTextOutputStreamWriter();

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            //System.out.println(text);
            String html = htmlParseData.getHtml();
            String clearedText = HTMLParser.readFromHTML(html, url);
            IOUtils.writeText(clearedText, url);
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            IOUtils.writeLinks(links, url);

            System.out.println("Text length: " + text.length());
            System.out.println("Html length: " + html.length());
            System.out.println("Number of outgoing links: " + links.size());
        }
    }

}

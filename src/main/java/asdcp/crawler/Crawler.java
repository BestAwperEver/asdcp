package asdcp.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import asdcp.HTMLParser;

public class Crawler extends WebCrawler {
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");

    private static final int MAX_DEPTH_OF_CRAWLING = 2;
    //private static final int MAX_PAGES_TO_FETCH = 10;
    // concurent threads for crawling
    private static final int NUMBER_OF_CRAWLERS = 4;

    private static String domen;
    private static OutputStreamWriter textOutputStreamWriter;
    private static CrawlController controller;
    // not used, if we don't download data
    private String crawlStorageFolder = "E:\\testCrawlerData";
    private static CrawlConfig config;
    private static PageFetcher pageFetcher;
    private static RobotstxtConfig robotstxtConfig;
    private static RobotstxtServer robotstxtServer;
    private static List<String> textList = new ArrayList<>();
    private static List<String> links = new ArrayList<>();

    public List<String> getText() {
        return textList;
    }

    public List<String> getLinks() {
        return links;
    }


    public Crawler(){
        
    }

    public Crawler(String domen) {
        Crawler.domen = domen;
        // prepare
        Crawler.config = new CrawlConfig();
        Crawler.config.setCrawlStorageFolder(crawlStorageFolder);
        Crawler.config.setMaxDepthOfCrawling(MAX_DEPTH_OF_CRAWLING);
        //Crawler.config.setMaxPagesToFetch(MAX_PAGES_TO_FETCH);
        Crawler.config.setIncludeBinaryContentInCrawling(false);
        Crawler.pageFetcher = new PageFetcher(config);
        Crawler.robotstxtConfig = new RobotstxtConfig();
        Crawler.robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        try {
            IOUtils.createResources();
            Crawler.controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.addSeed(domen);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(){
        try {
            controller.start(Crawler.class, NUMBER_OF_CRAWLERS);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            IOUtils.closeResources();
        }

    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.contains("spbu.ru");
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
            textList.add(clearedText);
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            IOUtils.writeLinks(links, url);
            addLinks(links);

            System.out.println("Text length: " + text.length());
            System.out.println("Html length: " + html.length());
            System.out.println("Number of outgoing links: " + links.size());
        }
    }

    private void addLinks(Set<WebURL> setLinks){
        for (WebURL webURL: setLinks) {
            links.add(webURL.getURL());
        }
    }

}

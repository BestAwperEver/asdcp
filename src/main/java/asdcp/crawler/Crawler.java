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

import java.util.*;
import java.util.regex.Pattern;
import asdcp.HTMLParser;

public class Crawler extends WebCrawler implements CrawlerTestMethods {
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz|pdf|txt|doc|docx))$");

    private static final int MAX_DEPTH_OF_CRAWLING = 1;
    // concurent threads for crawling
    private static final int NUMBER_OF_CRAWLERS = 4;

    private static String domen;
    private static CrawlController controller;
    private static CrawlConfig config;
    private static PageFetcher pageFetcher;
    private static RobotstxtConfig robotstxtConfig;
    private static RobotstxtServer robotstxtServer;
    private static List<String> textList = new ArrayList<>();
    private static List<String> links = new ArrayList<>();
    private static Set<String> visitedLinksSet = new HashSet<>();
    private static ArrayList<String> ListExceptionUrls = new ArrayList<String>();

    public List<String> getText() {
        return textList;
    }

    public List<String> getLinks() {
        return links;
    }

    public ArrayList<String> getUnreachableLinks() {
        return ListExceptionUrls;
    }

    public static Set<String> getVisitedLinksSet() {
        return visitedLinksSet;
    }

    public Crawler(){
        
    }

    public Crawler(String url) {
        String[] splittedUrl = url.split("/");
        Crawler.domen = splittedUrl[splittedUrl.length - 1];
        // prepare
        String crawlStorageFolder = "crawlerData";
        Crawler.config = new CrawlConfig();
        Crawler.config.setIncludeBinaryContentInCrawling(false);
        Crawler.config.setPolitenessDelay(1);
        Crawler.config.setCrawlStorageFolder(crawlStorageFolder);
        Crawler.config.setMaxDepthOfCrawling(MAX_DEPTH_OF_CRAWLING);
        Crawler.pageFetcher = new PageFetcher(config);
        Crawler.robotstxtConfig = new RobotstxtConfig();
        Crawler.robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        try {
            Crawler.controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.addSeed(url);

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
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        String newurl = href.replace("https://", "").replace("https://www.", "").replace("http://", "").replace("http://www.", "").replace("www.", "");
        if (newurl.endsWith("/")) {
            newurl = newurl.substring(0, newurl.length() - 1);
        }
        return !FILTERS.matcher(newurl).matches()
                && newurl.contains(domen) && !visitedLinksSet.contains(newurl);
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            String clearedText = HTMLParser.readFromHTML(html, url);
            textList.add(clearedText);
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            addLinks(links);
        }
    }

    @Override
    protected void onUnhandledException(WebURL webUrl, Throwable e) {
        String urlStr = (webUrl == null ? "NULL" : webUrl.getURL());
        ListExceptionUrls.add(urlStr);
    }


    private void addLinks(Set<WebURL> setLinks){
        for (WebURL webURL: setLinks) {
            String url = webURL.getURL();
            String newurl = url.replace("https://", "").replace("https://www.", "").replace("http://", "").replace("http://www.", "").replace("www.", "");
            if (newurl.endsWith("/")) {
                newurl = newurl.substring(0, newurl.length() - 1);
            }
            links.add(newurl);
            visitedLinksSet.add(newurl);
        }
    }
}

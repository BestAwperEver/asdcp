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

import static java.lang.Class.forName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

import asdcp.HTMLParser;

import org.javatuples.KeyValue;

public class Crawler extends WebCrawler implements CrawlerTestMethods {
    private final static Pattern FILTERS = Pattern.compile
    		(".*(\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz|pdf|txt|doc|docx))$");

    private static final int MAX_DEPTH_OF_CRAWLING = 4;
    // Concurrent threads for crawling
    private static final int NUMBER_OF_CRAWLERS = 24;

    private static String domen;
    private static String normalizedDomen;
    volatile private static String table_name;
    private static CrawlController controller;
    private static CrawlConfig config;
    private static PageFetcher pageFetcher;
    private static RobotstxtConfig robotstxtConfig;
    private static RobotstxtServer robotstxtServer;
    //private static List<String> textList = new ArrayList<>();
    private static List<String> links = java.util.Collections.synchronizedList(new ArrayList<>());
    private static List<String> internalLinks = new ArrayList<>();
    private static List<String> externalLinks = new ArrayList<>();
    private static List<String> subdomenLinks = new ArrayList<>();
    private static List<String> unreachableLinks = new ArrayList<>();
    //private static volatile Set<String> visitedLinksSet = java.util.Collections.synchronizedSet(new HashSet<>());
    private static Map<String, String> texts = new HashMap<>();
    
    public static BlockingQueue<KeyValue<String, String>> links_content = new ArrayBlockingQueue<KeyValue<String, String>>(10240);
    
    public Map<String, String> getTexts(){
    	return texts;
    }
    
//    public List<String> getText() {
//        return textList;
//    }
    
    public String getTableName() { return table_name; }
    public List<String> getLinks() {
        return links;
    }
    public List<String> getInternalLinks() {
        return internalLinks;
    }
    public List<String> getExternalLinks() {
        return externalLinks;
    }
    public List<String> getSubdomenLinks() {
        return subdomenLinks;
    }
    public Set<String> getUniqueLinks() {
        return new HashSet<String>(links);
    }
    public List<String> getUnreachableLinks() {
        return unreachableLinks;
    }
    public Set<String> getUniqueSubdomenLinks() {
        return new HashSet<String>(subdomenLinks);
    }
    public Set<String> getUniqueUnreachableLinks() {
        return new HashSet<String>(unreachableLinks);
    }
    public Set<String> getUniqueInternalLinks() {
        return new HashSet<String>(internalLinks);
    }
    public Set<String> getUniqueExternalLinks() {
        return new HashSet<String>(externalLinks);
    }
//    public static Set<String> getVisitedLinksSet() {
//        return visitedLinksSet;
//    }
    
    public Crawler() {}

    public Crawler(String url) {
    	// links.add(normalizeUrl(url));
    	// internalLinks.add(normalizeUrl(url));
        String[] splittedUrl = url.split("/");
        Crawler.domen = splittedUrl[splittedUrl.length - 1];
        //normalizedDomen = normalizeUrl(domen);
        Crawler.table_name = domen.replace('.', '_')
        		+ "_" + MAX_DEPTH_OF_CRAWLING
        		+ "_" + NUMBER_OF_CRAWLERS;

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
        String newurl = normalizeUrl(href);
        return !FILTERS.matcher(newurl).matches()
                && newurl.contains(domen) && !newurl.contains("." + domen);
    }

    @Override
    public void visit(Page page) {
        String url = normalizeUrl(page.getWebURL().getURL());
        if (page.getStatusCode() < 200 || page.getStatusCode() > 299) {
        	String urlStr = (url == null ? "NULL" : url);
            unreachableLinks.add(urlStr);
        }
//        System.out.println(page.getStatusCode() + " " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            try {
            	String clearedText = HTMLParser.readFromHTML(html, url);
            	//textList.add(clearedText);
                //texts.put(normalizeUrl(url), clearedText);
            	links_content.put(new KeyValue<String, String>(url, clearedText));
            } catch (IndexOutOfBoundsException | InterruptedException e) {
            	System.out.println("Patience..."); // lol
            }
            
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            addLinks(links);
            
        }
    }

    @Override
    protected void onUnhandledException(WebURL webUrl, Throwable e) {
        String urlStr = (webUrl == null ? "NULL" : webUrl.getURL());
        unreachableLinks.add(normalizeUrl(urlStr));
    }
    
    @Override
    protected void onUnexpectedStatusCode(String urlStr, int statusCode, String contentType,
            String description) {
        String urlStr2 = (urlStr == null ? "NULL" : urlStr);
        unreachableLinks.add(normalizeUrl(urlStr2));
    }
    
    private String normalizeUrl(String url) {
    	String newurl = url.replace("https://", "").replace("https://www.", "").replace("http://", "").replace("http://www.", "").replace("www.", "");
        if (newurl.endsWith("/")) {
            newurl = newurl.substring(0, newurl.length() - 1);
        }
        if (newurl.contains(";")) {
        	newurl = newurl.substring(0, newurl.indexOf(";"));
        } else if (newurl.contains("?")) {
        	newurl = newurl.substring(0, newurl.indexOf("?"));
        } else if (newurl.endsWith(",")) {
        	newurl = newurl.substring(0, newurl.length() - 1);
        }
        return newurl;
    }


    private void addLinks(Set<WebURL> setLinks){    	
        for (WebURL webURL: setLinks) {
            String url = webURL.getURL();
            String newurl = normalizeUrl(url);
            
            //visitedLinksSet.add(newurl);
            
            links.add(newurl);
            
//            try {
//            	addUrlToDatabase(newurl);
//		    } catch (SQLException ex) {
//			    System.out.println("SQLException: " + ex.getMessage());
//			    System.out.println("SQLState: " + ex.getSQLState());
//			    System.out.println("VendorError: " + ex.getErrorCode());
//			}
            
            boolean isSubdomain = newurl.contains("." + domen);
			boolean isDomain = newurl.contains(domen);
			if (isSubdomain) {
				subdomenLinks.add(newurl);
			} else if (isDomain) {
				internalLinks.add(newurl);
			} else {
				externalLinks.add(newurl);
			}
            
        }
    }
}

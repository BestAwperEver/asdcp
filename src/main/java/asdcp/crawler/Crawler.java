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
import java.util.regex.Pattern;

import asdcp.HTMLParser;

public class Crawler extends WebCrawler implements CrawlerTestMethods {
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz|pdf|txt|doc|docx))$");

    private static final int MAX_DEPTH_OF_CRAWLING = 3;
    // Concurrent threads for crawling
    private static final int NUMBER_OF_CRAWLERS = 4;

    private static String domen;
    private static String table_name;
    private static CrawlController controller;
    private static CrawlConfig config;
    private static PageFetcher pageFetcher;
    private static RobotstxtConfig robotstxtConfig;
    private static RobotstxtServer robotstxtServer;
    private static List<String> links = java.util.Collections.synchronizedList(new ArrayList<>());
    private static List<String> internalLinks = new ArrayList<>();
    private static List<String> externalLinks = new ArrayList<>();
    private static List<String> subdomenLinks = new ArrayList<>();
    private static List<String> unreachableLinks = new ArrayList<>();
    private static Set<String> visitedLinksSet = java.util.Collections.synchronizedSet(new HashSet<>());
    private static Map<String, String> texts = new HashMap<>();

    private Connection mysqlConn;
    private static boolean tableCreated = false;
    
    public Map<String, String> getTexts(){
    	return texts;
    }

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

    public Crawler(){
    	initDBConnetion();
    }

    public Crawler(String url) {
        String[] splittedUrl = url.split("/");
        Crawler.domen = splittedUrl[splittedUrl.length - 1];
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
                && newurl.contains(domen) && !visitedLinksSet.contains(normalizeUrl(newurl));
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        if (page.getStatusCode() < 200 || page.getStatusCode() > 299) {
        	String urlStr = (url == null ? "NULL" : url);
            unreachableLinks.add(normalizeUrl(urlStr));
        }

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            String clearedText = HTMLParser.readFromHTML(html, url);
            texts.put(normalizeUrl(url), clearedText);
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

    private void initDBConnetion() {
    	try {
			forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} 
    	
		String username = "testuser";
		String password = "test";
	       
	    //Create Connection
	    Properties info = new Properties();
	    info.put("user", username);
	    info.put("password", password);
	    info.put("autoReconnect", "true");
	    try {
			mysqlConn = DriverManager.getConnection("jdbc:mysql://radagast.asuscomm.com:3306/testdb", info);
			if (Crawler.tableCreated == false) {
				Crawler.tableCreated = true;
				Statement stmt = mysqlConn.createStatement();
				stmt.execute("DROP TABLE IF EXISTS " + table_name);
				stmt.execute("CREATE TABLE IF NOT EXISTS " + table_name + " (url VARCHAR(1024))");
				stmt.close();
			}
	    } catch (SQLException ex) {
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
    }
    
    private void addUrlToDatabase(String url) throws SQLException {
		Statement stmt = mysqlConn.createStatement();
		stmt.executeUpdate("insert into " + table_name + " value ('" + url + "')");
		stmt.close();
    }
    
    private void addLinks(Set<WebURL> setLinks){    	
        for (WebURL webURL: setLinks) {
            String url = webURL.getURL();
            String newurl = normalizeUrl(url);
            
            visitedLinksSet.add(newurl);
            
            links.add(newurl);
            
            try {
            	addUrlToDatabase(newurl);
		    } catch (SQLException ex) {
			    System.out.println("SQLException: " + ex.getMessage());
			    System.out.println("SQLState: " + ex.getSQLState());
			    System.out.println("VendorError: " + ex.getErrorCode());
			}
            
            boolean isSubdomain = newurl.contains("." + normalizeUrl(Crawler.domen)); 
			boolean isDomain = newurl.contains(normalizeUrl(Crawler.domen)); 
			if (isSubdomain) {
				subdomenLinks.add(newurl);
			} else if (isDomain) {
				internalLinks.add(newurl);
			} else {
				externalLinks.add(newurl);
			}
            
        }
    }

    @Override
    public void onBeforeExit() {
        try {
            mysqlConn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.onBeforeExit();
    }
}

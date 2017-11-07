package asdcp.crawler;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class CrawlerTest {

	static Crawler crawler = null;
	
	{
		if (null == crawler) {
			crawler = new Crawler("http://radagast.asuscomm.com");
			crawler.start();
		}
		
	}

	@Test
	public void testGetLinksCount() {
		List<String> links = crawler.getLinks();
		assertEquals(19, links.size());
	}
	
	@Test
	public void testGetUniqueLinksCount() {
		Set<String> uniqueLinks = crawler.getUniqueLinks();
		assertEquals(10, uniqueLinks.size());
	}
	
	@Test
	public void testGetUnreachableLinksCount() {
		List<String> unreachableLinks = crawler.getUnreachableLinks();
		assertEquals(1, unreachableLinks.size());
	}
	
	@Test
	public void testGetInternalLinksCount() {
		List<String> internalLinks = crawler.getInternalLinks();
		assertEquals(8, internalLinks.size());
	}
	
	@Test
	public void testGetUniqueInternalLinksCount() {
		Set<String> internalUniqueLinks = crawler.getUniqueInternalLinks();
		assertEquals(6, internalUniqueLinks.size());
	}
	
	@Test
	public void testGetExternalLinksCount() {
		List<String> externalLinks = crawler.getExternalLinks();
		assertEquals(11, externalLinks.size());
	}
	
	@Test
	public void testGetUniqueExternalLinksCount() {
		Set<String> uniqueExternalLinks = crawler.getUniqueExternalLinks();
		assertEquals(4, uniqueExternalLinks.size());
	}
	
	@Test
	public void testGetSubdomenLinksCount() {
		List<String> subdomenLinks = crawler.getSubdomenLinks();
		assertEquals(0, subdomenLinks.size());
	}
	
	@Test
	public void testGetUniqueSubdomenLinksCount() {
		Set<String> subdomenUniqueLinks = crawler.getUniqueSubdomenLinks();
		assertEquals(0, subdomenUniqueLinks.size());
	}
	
	@Test
	public void testGetUniqueLinks() {
		Set<String> uniqueLinks = crawler.getUniqueLinks();
		assertEquals(uniqueLinks, new HashSet<>(Arrays.asList(
				"radagast.asuscomm.com/error",
				"radagast.asuscomm.com",
				"radagast.asuscomm.com/testlogin.jsp",
				"radagast.asuscomm.com/testlogin.css",
				"radagast.asuscomm.com/about.jsp",
				"radagast.asuscomm.com/account",
				
				"netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css",
				"netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap-theme.min.css",
				"netdna.bootstrapcdn.com/bootstrap/3.0.2/js/bootstrap.min.js",
				
				"code.jquery.com/jquery.js")));		
	}
	
	@Test
	public void testGetUniqueExternalLinks() {
		Set<String> uniqueLinks = crawler.getUniqueExternalLinks();
		assertEquals(uniqueLinks, new HashSet<>(Arrays.asList(
				"netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap.min.css",
				"netdna.bootstrapcdn.com/bootstrap/3.0.2/css/bootstrap-theme.min.css",
				"netdna.bootstrapcdn.com/bootstrap/3.0.2/js/bootstrap.min.js",
				"code.jquery.com/jquery.js")));
	}
	
	@Test
	public void testGetUniqueSubdomenLinks() {
		Set<String> uniqueSubdomenLinks = crawler.getUniqueSubdomenLinks();
		assertEquals(uniqueSubdomenLinks, new HashSet<>(Arrays.asList()));
	}
	
	@Test
	public void testGetUniqueUnreachableLinks() {
		Set<String> uniqueUnreachableLinks = crawler.getUniqueUnreachableLinks();
		assertEquals(uniqueUnreachableLinks, new HashSet<>(Arrays.asList(
				"radagast.asuscomm.com/error")));
	}

}

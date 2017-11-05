package asdcp.crawler;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class CrawlerTest {

	Crawler crawler = null;
	
	public CrawlerTest() {
		super();
		crawler = new Crawler("http://radagast.asuscomm.com");
		crawler.start();
	}

	@Test
	public void testGetLinksCount() {
		List<String> links = crawler.getLinks();
		assertEquals(5, links.size());
	}
	
	@Test
	public void testGetUniqueLinksCount() {
		Set<String> uniqueLinks = crawler.getUniqueLinks();
		assertEquals(4, uniqueLinks.size());
	}
	
	@Test
	public void testGetUnreachableLinksCount() {
		List<String> unreachableLinks = crawler.getUnreachableLinks();
		assertEquals(0, unreachableLinks.size());
	}
	
	@Test
	public void testGetInternalLinksCount() {
		List<String> internalLinks = crawler.getInternalLinks();
		assertEquals(5, internalLinks.size());
	}
	
	@Test
	public void testGetExternalLinksCount() {
		List<String> externalLinks = crawler.getInternalLinks();
		assertEquals(0, externalLinks.size());
	}
	
	@Test
	public void testGetSubdomenLinksCount() {
		List<String> subdomenLinks = crawler.getSubdomenLinks();
		assertEquals(0, subdomenLinks.size());
	}
	
	@Test
	public void testGetUniqueLinks() {
		Set<String> uniqueLinks = crawler.getUniqueLinks();
		assertEquals(uniqueLinks, new HashSet<>(Arrays.asList(
				"http://radagast.asuscomm.com", 
				"http://radagast.asuscomm.com/testlogin.jsp",
				"http://radagast.asuscomm.com/about.jsp",
				"http://radagast.asuscomm.com/account")));
	}
	
	@Test
	public void testGetUniqueExternalLinks() {
		Set<String> uniqueLinks = crawler.getUniqueExternalLinks();
		assertEquals(uniqueLinks, new HashSet<>(Arrays.asList()));
	}
	
	@Test
	public void testGetUniqueSubdomenLinks() {
		Set<String> uniqueSubdomenLinks = crawler.getUniqueSubdomenLinks();
		assertEquals(uniqueSubdomenLinks, new HashSet<>(Arrays.asList()));
	}
	
	@Test
	public void testGetUniqueUnreachableLinks() {
		Set<String> uniqueUnreachableLinks = crawler.getUniqueUnreachableLinks();
		assertEquals(uniqueUnreachableLinks, new HashSet<>(Arrays.asList()));
	}

}

package asdcp.crawler;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SearcherTest {
	static Searcher searcher = null;
	
	{
		if (null == searcher) {
			searcher = new Searcher();
			searcher.initDBConnetion();
		}
		
	}
	
	@Test
	public void testGetDocumentsByWord_null() {
		List<String> urls = searcher.getDocumentsByWord_Test("thereisnosuchword");
		assertNull(urls);
	}
	
	@Test
	public void testGetDocumentsByWord_Test() {
		List<String> urls = searcher.getDocumentsByWord_Test("you");
		assertEquals(urls, Arrays.asList(
				"radagast.asuscomm.com",
				"radagast.asuscomm.com/about.jsp"));
	}
	
}

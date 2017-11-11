package asdcp;

import asdcp.crawler.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EntryPoint {

	public static void main(String[] args) {
		String startUrl = "http://spbu.ru/";
		//String startUrl = "http://radagast.asuscomm.com";
		if (args.length > 0){
			startUrl  = args[0];
		}

		long startTime = System.currentTimeMillis();
		Crawler crawler = new Crawler(startUrl);
		crawler.start();
		long timeSpent = System.currentTimeMillis() - startTime;

		System.out.println("Time of crawler work: " + timeSpent);
		System.out.println("Count of all links: " + crawler.getLinks().size());
		System.out.println("Count of internal links: " + crawler.getInternalLinks().size());
		System.out.println("Count of external links: " + crawler.getExternalLinks().size());
		System.out.println("Count of subdomain links: " + crawler.getSubdomenLinks().size());
		System.out.println("Count of unreachable links: " + crawler.getUnreachableLinks().size());
		System.out.println("Count of unique links: " + crawler.getUniqueLinks().size() + "  links:  " + crawler.getUniqueLinks());
		System.out.println("Count of unique internal links: " + crawler.getUniqueInternalLinks().size()  + "  links:  " + crawler.getUniqueInternalLinks());
		System.out.println("Count of unique external links: " + crawler.getUniqueExternalLinks().size() + "  links:  " + crawler.getUniqueExternalLinks());
		System.out.println("Count of unique subdomain links: " + crawler.getUniqueSubdomenLinks().size()  + "  links:  " + crawler.getUniqueSubdomenLinks());
		System.out.println("Count of unique unreachable links: " + crawler.getUniqueUnreachableLinks().size()  + "  links:  " + crawler.getUniqueUnreachableLinks());
		//System.out.println("Map: " + crawler.getTexts().keySet());
	
		crawler.writeToDatabase();
	
	}
}

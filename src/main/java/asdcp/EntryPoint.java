package asdcp;

import asdcp.crawler.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EntryPoint {

	public static void main(String[] args) {
//		String startUrl = "http://spbu.ru/";
		String startUrl = "http://radagast.asuscomm.com";
		if (args.length > 0){
			startUrl  = args[0];
		}
		int countintlinks = 0;
		int countdomainlinks = 0;
		int countextlinks = 0;
		long startTime = System.currentTimeMillis();
		Crawler crawler = new Crawler(startUrl);
		crawler.start();
		long timeSpent = System.currentTimeMillis() - startTime;

		Set<String> uniqset = new LinkedHashSet<>(crawler.getLinks());
		Set<String> uniqunreacheable = new LinkedHashSet<>(crawler.getUnreachableLinks());

		List<String> text = crawler.getText();
		List<String> links = crawler.getLinks();

		int lol = 0;
		for (String s : text) {
			lol += s.length();
		}

		for (String url : uniqset) {

			boolean isContain = url.contains(".radagast.asuscomm.com");
			boolean isContain1 = url.contains("radagast.asuscomm.com");
			if (isContain) {
				++countdomainlinks;
			}
			if (!isContain && isContain1) {
				++countintlinks;
			}
			if (!isContain && !isContain1) {
				++countextlinks;
			}
		}

		System.out.println("Time of crawler work: " + timeSpent);
		System.out.println("Count of all links: " + uniqset.size());
		System.out.println("Count of symbols: " + lol);
		System.out.println("Count of unreacheable links: " + uniqunreacheable.size());
		System.out.println("Count of internal links: " + countintlinks);
		System.out.println("Count of domain links: " + countdomainlinks);
		System.out.println("Count of external links: " + countextlinks);
	}
}

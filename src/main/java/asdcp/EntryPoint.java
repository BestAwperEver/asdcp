package asdcp;

import java.io.File;
import java.io.IOException;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import asdcp.crawler.*;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public final class EntryPoint {

	private static final int MAX_DEPTH_OF_CRAWLING = 1;
	private static final int MAX_PAGES_TO_FETCH = 10;
	// concurent threads for crawling
	private static final int NUMBER_OF_CRAWLERS = 4;

	public static void main(String[] args) {
		
//		String fileName;
//
//		if (args.length == 1) {
//			fileName = args[0];
//		} else {
//			fileName = "D:\\Test.pdf";
//		}
//
//		File f = new File(fileName);
//		if(f.exists() && !f.isDirectory()) {
//
//			String[] splitted = fileName.split("\\\\");
//			splitted = splitted[splitted.length-1].split("\\.");
//
//			if (splitted.length < 2) {
//				System.out.println("Unsupported file format.");
//				System.exit(1);
//			}
//
//			String Type = splitted[splitted.length-1].toLowerCase();
//
//			@SuppressWarnings("rawtypes")
//			FileParser xp = null;
//
//			switch (Type) {
//				case "doc": xp = new DocParser(); break;
//				case "docx": {
//					xp = new DocxParser();
//					((DocxParser)xp).setUseDocx4j(false);
//				} break;
//				case "pdf": {
//					xp = new PdfParser();
//					((PdfParser)xp).setUseJPod(false);
//					((PdfParser)xp).setUseIText(false);
//				} break;
//				case "html":{
//					xp = new HTMLParser();
//					((HTMLParser)xp).setUseCleaner(false);
//					break;
//				}
//
//			}
//
//			if (xp != null) {
//				try {
//					xp.readDocument(fileName);
//
//				} catch (IOException e) {
//					e.printStackTrace();
//					System.exit(3);
//				}
//			} else {
//				System.out.println("Unsupported file format.");
//				System.exit(1);
//			}
//
//			System.out.print(xp.getText());
//
//		} else {
//			System.out.println("Cannot open " + fileName + ". Is it valid file path?");
//			System.exit(2);
//		}

		String crawlStorageFolder = "E:\\testCrawlerData";
		String domen = "http://spbu.ru/";
		CrawlConfig config = new CrawlConfig();

		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setMaxDepthOfCrawling(MAX_DEPTH_OF_CRAWLING);
		config.setMaxPagesToFetch(MAX_PAGES_TO_FETCH);
		config.setIncludeBinaryContentInCrawling(false);
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		BasicCrawler.setDomen(domen);

		try {
			IOUtils.createResources();
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
			controller.addSeed(domen);
			controller.start(BasicCrawler.class, NUMBER_OF_CRAWLERS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			IOUtils.closeResources();
		}

	}
}

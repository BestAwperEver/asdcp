package asdcp;

import asdcp.crawler.*;

import java.util.List;

public final class EntryPoint {

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

		String domen = "http://spbu.ru/";

		Crawler crawler = new Crawler(domen);
		crawler.start();
		List<String> text = crawler.getText();
		List<String> links = crawler.getLinks();

	}
}

package asdcp;

import java.io.File;
import java.io.IOException;

public final class EntryPoint {

	public static void main(String[] args) {

		HTMLParser htmlParser = new HTMLParser();
		try {
			htmlParser.readDocument("spbu.html");
			System.out.println("File readed");
		} catch (IOException e) {
			System.out.println("Error happen during reading process");
			e.printStackTrace();
		}
		
		String fileName;
		
		if (args.length == 1) {
			fileName = args[0];
		} else {
			fileName = "D:\\Test.pdf";
		}
		
		File f = new File(fileName);
		if(f.exists() && !f.isDirectory()) { 
			
			String[] splitted = fileName.split("\\\\");
			splitted = splitted[splitted.length-1].split("\\.");
			
			if (splitted.length < 2) {
				System.out.println("Unsupported file format.");
				System.exit(1);
			}
			
			String Type = splitted[splitted.length-1].toLowerCase();
			
			FileParser xp = null;
			
			switch (Type) {
				case "doc": xp = new DocParser(); break;
				case "docx": xp = new DocxParser(); break;
				case "pdf": xp = new PdfParser(); break;
			}
			
			if (xp != null) {
				try {
					xp.readDocument(fileName);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(3);
				}
			} else {
				System.out.println("Unsupported file format.");
				System.exit(1);
			}
		} else {
			System.out.println("Cannot open " + fileName + ". Is it valid file path?");
			System.exit(2);
		}
		

	}

}

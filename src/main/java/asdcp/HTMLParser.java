package asdcp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class HTMLParser extends FileParser<Document> {
    private String title;
    private List<String> links = new ArrayList<>();
    private List<String> linksText = new ArrayList<>();

    @Override
    public void readDocument(String fileName) throws IOException {

        Document htmlFile = Jsoup.parse(new File(fileName), "UTF-8");

        readWords(htmlFile);

    }
    
    @Override
    public void readWords(Document htmlFile) throws IOException {
        String lineSeparator = System.getProperty("line.separator");
        
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("log.txt"))) {
	            
	        // get title
	        title = htmlFile.title();
	        System.out.println("Title " + title);
	        outputStreamWriter.write("Title " + title + lineSeparator);
	
	        // get text from body
	        Element body = htmlFile.body();
	        Elements divElements = body.getElementsByTag("div");
	
	        for (int i = 0; i < divElements.size(); i++) {
	            Element divElement = divElements.get(i);
	            String divText = divElement.text();
	            String[] splittedText = divText.split("[\\s]+");
                Collections.addAll(words, splittedText);
	            System.out.println("DivText: " + divText);
	            outputStreamWriter.write("DivText: " + divText + lineSeparator);
	        }
	
	        // We can get all text in spbu.ru from first div wrapper, because he includes all others
	
	        // get links
	        Elements aElements = body.getElementsByTag("a");
	
	        for (int i = 0; i < aElements.size(); i++) {
	            Element aElement = aElements.get(i);
	            String link = aElement.attr("href");
	            links.add(link);
	            String linkText = aElement.text();
	            linksText.add(linkText);
	            System.out.println(link);
	            outputStreamWriter.write("Link: " + link + lineSeparator);
	            System.out.println("Link text: " + linkText);
	            outputStreamWriter.write("Link text: " + linkText + lineSeparator);
	        }
	
	        // remove duplicates, order isn't saved
	        Set<String> tmp = new HashSet<>(links);
	        links = new ArrayList<>(tmp);
	
	        System.out.println("Links after I removed duplicates");
	        outputStreamWriter.write("LINKS WITHOUT DUPLICATES" + lineSeparator);
	        for (int i = 0; i < links.size(); i++) {
	            System.out.println("Link v2 " + links.get(i));
	            outputStreamWriter.write("Link v2 " + links.get(i) + lineSeparator);
	        }
	        outputStreamWriter.flush();
        }
    }
}

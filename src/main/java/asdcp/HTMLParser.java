package asdcp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.htmlcleaner.*;

public class HTMLParser extends FileParser<Document> {
    private String title;
    private List<String> links = new ArrayList<>();
    private List<String> linksText = new ArrayList<>();
    private boolean isCleaner = false;
    private boolean isJsoup = false;

	public List<String> getLinks() {
		return links;
	}

	public boolean getIsCleaner() {
		return isCleaner;
	}

	public void setCleaner(boolean cleaner) {
		isCleaner = cleaner;
		if(isCleaner) isJsoup = false;
	}

	public boolean isJsoup() {
		return isJsoup;
	}

	public void setJsoup(boolean jsoup) {
		isJsoup = jsoup;
		if(isJsoup) isCleaner = false;
	}

	@Override
    public void readDocument(String fileName) throws IOException {

		File readedFile = new File(fileName);

		if(isJsoup){
			Document htmlFile = Jsoup.parse(readedFile, "UTF-8");
			readWords(htmlFile);
		}
		if (isCleaner){
			readFileUsingHtmlCleaner(readedFile);
		}

    }
    
    @Override
    public void readWords(Document htmlFile) throws IOException {
        String lineSeparator = System.getProperty("line.separator");
        
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("log.txt"))) {
	            
	        // get title
	        title = htmlFile.title();
	        //System.out.println("Title " + title);
	        outputStreamWriter.write("Title " + title + lineSeparator);
	
	        // get text from body
	        Element body = htmlFile.body();
	        Elements divElements = body.getElementsByTag("div");
	        
	        StringBuilder sb = new StringBuilder();
	
	        for (int i = 0; i < divElements.size(); i++) {
	            Element divElement = divElements.get(i);
	            String divText = divElement.text().trim();
	            
	            sb.append(divText);
	            
	            //System.out.println("DivText: " + divText);
	            outputStreamWriter.write("DivText: " + divText + lineSeparator);
	        }
	        
	        text = sb.toString();
	
	        // We can get all text in spbu.ru from first div wrapper, because he includes all others
	
	        // get links
	        Elements aElements = body.getElementsByTag("a");
	
	        for (int i = 0; i < aElements.size(); i++) {
	            Element aElement = aElements.get(i);
	            String link = aElement.attr("href");
	            links.add(link);
	            String linkText = aElement.text();
	            linksText.add(linkText);
	            //System.out.println(link);
	            outputStreamWriter.write("Link: " + link + lineSeparator);
	            //System.out.println("Link text: " + linkText);
	            outputStreamWriter.write("Link text: " + linkText + lineSeparator);
	        }
	
	        // remove duplicates, order isn't saved
	        Set<String> tmp = new HashSet<>(links);
	        links = new ArrayList<>(tmp);
	
	        //System.out.println("Links after I removed duplicates");
	        outputStreamWriter.write("LINKS WITHOUT DUPLICATES" + lineSeparator);
	        for (int i = 0; i < links.size(); i++) {
	            //System.out.println("Link v2 " + links.get(i));
	            outputStreamWriter.write("Link v2 " + links.get(i) + lineSeparator);
	        }
	        outputStreamWriter.flush();
        }
    }

    private void readFileUsingHtmlCleaner(File file){

		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode rootNode = null;

		try {
			rootNode = cleaner.clean(file, "UTF-8");
			TagNode[] divNodes = rootNode.getElementsByName("div", true); // to find inner elements
			TagNode[] aNodes = rootNode.getElementsByName("a", true);

			StringBuilder sb = new StringBuilder();
			
			// Get text
			for (int i = 0; i < divNodes.length; i++) {
				sb.append(divNodes[i].getText().toString().trim());
			}

			text = sb.toString();
			
			// Get links
			for (int i = 0; i < aNodes.length; i++) {
				String link = aNodes[i].getAttributeByName("href");
				links.add(link);
				String linkText = aNodes[i].getText().toString();
				linksText.add(linkText);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

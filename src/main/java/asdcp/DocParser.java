package asdcp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class DocParser implements FileParser {
	
    public void readDocument (String fileName) throws IOException {
        POIFSFileSystem fs = null;

        fs = new POIFSFileSystem(new FileInputStream(fileName));
        HWPFDocument doc = new HWPFDocument(fs);

        readWords(doc);

    }  

    public void readWords(HWPFDocument doc) throws IOException {
    	try(WordExtractor we = new WordExtractor(doc)) {
    		
            String[] paragraphs = we.getParagraphText();

            for (int i = 0; i < paragraphs.length; ++i) {
            	words.addAll(Arrays.asList(paragraphs[i].split("\\s+")));
            }
            
            for (String word : words) {
            	System.out.println(word);
            }    		
            
    	}
    }
	
}

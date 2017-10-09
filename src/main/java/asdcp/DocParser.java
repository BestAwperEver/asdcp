package asdcp;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class DocParser extends FileParser<HWPFDocument> {
	
    public void readDocument (String fileName) throws IOException {
        POIFSFileSystem fs = null;

        fs = new POIFSFileSystem(new FileInputStream(fileName));
        HWPFDocument doc = new HWPFDocument(fs);

        readWords(doc);

    }  

    public void readWords(HWPFDocument doc) throws IOException {
    	try(WordExtractor we = new WordExtractor(doc)) {
    		
            String[] paragraphs = we.getParagraphText();

            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < paragraphs.length; ++i) {
            	sb.append(paragraphs[i]);
            }
            
            text = sb.toString();
            
    	}
    }
	
}

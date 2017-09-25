package asdcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class DocxParser implements FileParser {
	
    public void readDocument(String fileName) throws IOException {
    	
    	File file = new File(fileName);
    	
        try(FileInputStream fis = new FileInputStream(file.getAbsolutePath())) {
        	
            XWPFDocument doc = new XWPFDocument(fis);

            readWords(doc);
        }
    }  

    public void readWords(XWPFDocument doc) throws IOException {
     
        List<XWPFParagraph> paragraphs = doc.getParagraphs();

        for (XWPFParagraph para : paragraphs) {
        	words.addAll(Arrays.asList(para.getText().split("\\s+")));
        }        
        
        for (String word : words) {
        	System.out.println(word);
        }
        
    }

}

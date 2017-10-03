package asdcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;

public class DocxParser extends FileParser<XWPFDocument> {
	
	private boolean m_bUseDocx4j = false;
	
	public boolean getUseDocx4j() {
		return m_bUseDocx4j;
	}

	public void setUseDocx4j(boolean useDocx4j) {
		this.m_bUseDocx4j = useDocx4j;
	}
	
	protected static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
	    List<Object> result = new ArrayList<Object>();
	    if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();

	    if (obj.getClass().equals(toSearch))
	        result.add(obj);
	    else if (obj instanceof ContentAccessor) {
	        List<?> children = ((ContentAccessor) obj).getContent();
	        for (Object child : children) {
	            result.addAll(getAllElementFromObject(child, toSearch));
	        }
	    }
	    return result;
	}
	
    public void readDocument(String fileName) throws IOException {
    	
    	File file = new File(fileName);
    	
    	if (m_bUseDocx4j) {

			WordprocessingMLPackage wordMLPackage;

			try {
				wordMLPackage = WordprocessingMLPackage.load(file);
				
				readWords(wordMLPackage);
				
			} catch (Docx4JException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return;			
    	}
    	
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
        
    }
    
    public void readWords(WordprocessingMLPackage wordMLPackage) throws IOException {
     
    	List<Object> texts = getAllElementFromObject(wordMLPackage.getMainDocumentPart(), org.docx4j.wml.Text.class);
		for (Object t : texts) {
			org.docx4j.wml.Text content = (org.docx4j.wml.Text) t;
			words.addAll(Arrays.asList(content.getValue().split("\\s+")));
		}
        
    }

}

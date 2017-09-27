package asdcp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class PdfParser extends FileParser<PDDocument> {

	public void readDocument(String fileName) throws IOException {
		
		try (PDDocument doc = PDDocument.load(new File(fileName))) {

			readWords(doc);

        }
	}
	
	public void readWords(PDDocument doc) throws IOException {
        doc.getClass();

        if (!doc.isEncrypted()) {

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);

            PDFTextStripper tStripper = new PDFTextStripper();

            String pdfFileInText = tStripper.getText(doc);
            
            words.addAll(Arrays.asList(pdfFileInText.split("\\s+")));

        } else {
        	System.out.println("The file is encrypted and can't be parsed.");
        }
	}

}

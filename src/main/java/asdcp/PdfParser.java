package asdcp;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class PdfParser extends FileParser<PDDocument> {

	private boolean m_bUseIText = false;
	private boolean m_bUseJPod = false;
	
	public boolean getUseIText() {
		return m_bUseIText;
	}

	public void setUseIText(boolean useIText) {
		this.m_bUseIText = useIText;
		if (useIText) m_bUseJPod = false;
	}

	public boolean getUseJPod() {
		return m_bUseJPod;
	}

	public void setUseJPod(boolean useJPod) {
		this.m_bUseJPod = useJPod;
		if (useJPod) m_bUseIText = false;
	}

	public void readDocument(String fileName) throws IOException {

		if (m_bUseJPod) {
			JpodPdfParser client = new JpodPdfParser();
			text = client.run(fileName);
			return;
		}

		if (m_bUseIText) {
			readIText(fileName);
			return;
		}

		try (PDDocument doc = PDDocument.load(new File(fileName))) {

			readWords(doc);

        }
	}
	
	public void readIText(String fileName) throws IOException {
		PdfReader reader = null;
		try {
			reader = new PdfReader(fileName);
			readWords(reader);	        
		} 
		finally {
			if (reader != null) reader.close();
		}
	}
	
	public void readWords(PdfReader doc) throws IOException {
		int N = doc.getNumberOfPages();
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 1; i <= N; ++i) {
			String textFromPage = PdfTextExtractor.getTextFromPage(doc, i);
			sb.append(textFromPage);
		}
		
		text = sb.toString();
	}
	
	public void readWords(PDDocument doc) throws IOException {
        doc.getClass();

        if (!doc.isEncrypted()) {

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);

            PDFTextStripper tStripper = new PDFTextStripper();

            text = tStripper.getText(doc);

        } else {
        	System.out.println("The file is encrypted and can't be parsed.");
        }
	}

}

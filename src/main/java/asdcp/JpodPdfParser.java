package asdcp;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import de.intarsys.pdf.content.CSDeviceBasedInterpreter;
import de.intarsys.pdf.content.CSException;
import de.intarsys.pdf.content.text.CSTextExtractor;
import de.intarsys.pdf.cos.COSVisitorException;
import de.intarsys.pdf.example.common.CommonJPodExample;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageNode;
import de.intarsys.pdf.pd.PDPageTree;
import de.intarsys.pdf.tools.kernel.PDFGeometryTools;
import de.intarsys.tools.stream.StreamTools;

public class JpodPdfParser extends CommonJPodExample {

	public static int SCALE = 2;

	@SuppressWarnings("rawtypes")
	protected void extractText(PDPageTree pageTree, StringBuilder sb) {
		for (Iterator it = pageTree.getKids().iterator(); it.hasNext();) {
			PDPageNode node = (PDPageNode) it.next();
			if (node.isPage()) {
				try {
					CSTextExtractor extractor = new CSTextExtractor();
					PDPage page = (PDPage) node;
					AffineTransform pageTx = new AffineTransform();
					PDFGeometryTools.adjustTransform(pageTx, page);
					extractor.setDeviceTransform(pageTx);
					CSDeviceBasedInterpreter interpreter = new CSDeviceBasedInterpreter(
							null, extractor);
					interpreter.process(page.getContentStream(), page
							.getResources());
					sb.append(extractor.getContent());
				} catch (CSException e) {
					e.printStackTrace();
				}
			} else {
				extractText((PDPageTree) node, sb);
			}
		}
	}

	protected String extractText(String filename) throws COSVisitorException,
			IOException {
		PDDocument doc = getDoc();
		StringBuilder sb = new StringBuilder();
		extractText(doc.getPageTree(), sb);
		File outputFile = new File(filename + ".txt");
		FileWriter w = new FileWriter(outputFile);
		try {
			w.write(sb.toString());
			return sb.toString();
		} finally {
			StreamTools.close(w);
		}
	}

	public String run(String inputFileName) throws IOException {
//		if (args.length < 1) {
//			usage();
//			return;
//		}
		String text = null;
		try {
//			String inputFileName = args[0];
			open(inputFileName);
			text = extractText(inputFileName);
		} catch (COSLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (COSVisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			close();
		}
		return text;
	}

//	/**
//	 * Help the user.
//	 */
//	public void usage() {
//		System.out.println("usage: java.exe " + getClass().getName() //$NON-NLS-1$
//				+ " <input-pdf>"); //$NON-NLS-1$
//	}
}

package asdcp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class FileParser<T> {
	
	//protected List<String> words = new LinkedList<String>();
	
	protected String text = new String();
	
	public abstract void readDocument(String fileName) throws IOException;
	
	public abstract void readWords(T document) throws IOException;
	
	public String getText() {
		return text;
	}
	
	public List<String> getWords() {
		return Arrays.asList(text.trim().split("\\s+"));
	}
	
}

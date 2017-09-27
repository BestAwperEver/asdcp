package asdcp;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class FileParser<T> {
	
	protected List<String> words = new LinkedList<String>();
	
	public abstract void readDocument(String fileName) throws IOException;
	
	public abstract void readWords(T document) throws IOException;
	
	public List<String> getWords() {
		return words;
	}
	
}

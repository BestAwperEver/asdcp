package asdcp;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public interface FileParser {
	public List<String> words = new LinkedList<String>();
	
	public void readDocument (String fileName) throws IOException;
}

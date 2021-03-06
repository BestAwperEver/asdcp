package asdcp.crawler;

import java.util.List;
import java.util.Map;

import java.util.Set;

public interface CrawlerTestMethods {
	
    public Map<String, String> getTexts();
	
    public List<String> getLinks();
    public List<String> getInternalLinks();
    public List<String> getSubdomenLinks();
    public List<String> getExternalLinks();
    public List<String> getUnreachableLinks();
    
    public Set<String> getUniqueLinks();
    public Set<String> getUniqueInternalLinks();
    public Set<String> getUniqueExternalLinks();
    public Set<String> getUniqueSubdomenLinks();
    public Set<String> getUniqueUnreachableLinks();

}

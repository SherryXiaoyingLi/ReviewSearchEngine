package runners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import edu.virginia.cs.index.Indexer;
import edu.virginia.cs.index.ResultDoc;
import edu.virginia.cs.index.Searcher;
import edu.virginia.cs.index.SearchResult;

public class Main {

	//The main entrance to test various functions 
	public static void main(String[] args) {
		try {
			long currentTime = System.currentTimeMillis();
			Indexer.index("data/indices", "data/datas");
			long timeElapsed = System.currentTimeMillis() - currentTime;
			System.out.format("Finished in %.3f seconds\n", timeElapsed/1000.0);
			String query = "Good for baby products.";
			
			Searcher indexSearcher = new Searcher("data/indices");
			// using the search that calls search(StringQuery) and runSearch
			SearchResult result = indexSearcher.search(query);
			ArrayList<ResultDoc> resultDocs = result.getDocs();
			for (ResultDoc doc: resultDocs) {
				System.out.println(doc.getReviewText());
				System.out.println(doc.getAsin());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/**
	public static class ValueComparator implements Comparator<String> {
	    HashMap<String, Integer> base;
	    public ValueComparator(HashMap<String, Integer> base) {
	        this.base = base;
	    }
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        }
	    }
	}
	public static class ValueComparator2 implements Comparator<String> {
	    HashMap<String, Long> base;
	    public ValueComparator2(HashMap<String, Long> base) {
	        this.base = base;
	    }
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        }
	    }
	}
**/
}

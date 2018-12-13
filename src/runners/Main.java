package runners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import aspectSegmenter.AnalyzerLara;
import aspectSegmenter.Product;
import aspectSegmenter.Review;
import edu.virginia.cs.index.Indexer;
import edu.virginia.cs.index.ResultDoc;
import edu.virginia.cs.index.Searcher;
import json.JSONException;
import lara.LRR;
import edu.virginia.cs.index.SearchResult;

public class Main {

	public static void main(String[] args) throws IOException, JSONException {
		
		
		/**
		// generate bootstrapping product keywords here
		AnalyzerLara analyzer = new AnalyzerLara("data/Seeds/baby_for_bootstrapping.dat", "data/Seeds/stopwords.dat", 
				"data/Model/NLP/en-sent.zip", "data/Model/NLP/en-token.zip", "data/Model/NLP/en-pos-maxent.bin");
		HashMap<String, Product> pdctList = new HashMap<>();
		analyzer.LoadDirectory("data/Reviews/amazon", ".json", pdctList);
		analyzer.BootStrapping("Data/Seeds/baby_bootstrapping_res.dat");
		
		
		
		// generate weights and aspRat and build indices here
		AnalyzerLara analyzer = new AnalyzerLara("data/Seeds/baby_bootstrapping_res.dat", "data/Seeds/stopwords.dat", 
				"data/Model/NLP/en-sent.zip", "data/Model/NLP/en-token.zip", "data/Model/NLP/en-pos-maxent.bin");
		HashMap<String, Product> pdctList = new HashMap<>();
		analyzer.LoadDirectory("data/Reviews/amazon", ".json", pdctList);
		analyzer.Save2Vectors("data/Vectors/vector_baby.dat");	
		
		
		LRR model = new LRR(500, 1e-2, 5000, 1e-2, 2.0);
		//model.LoadVectors("data/Vectors/Vector.dat");
		model.EM_est("data/Vectors/vector_baby.dat", 10, 1e-4);
		model.SaveModel("data/Model/model_baby.dat");
		model.SavePrediction("data/Results/pred_baby.dat"); 
		
		System.out.println("good ");
		
		long currentTime = System.currentTimeMillis();
		Indexer.index("data/indices/indices_baby", "data/Reviews/amazon", "data/Results/pred_baby.dat", 3);
		long timeElapsed = System.currentTimeMillis() - currentTime;
		System.out.format("Finished in %.3f seconds\n", timeElapsed/1000.0);
		**/
		
		
		// input query and weights here, 
		// change weight_i allowable range in Searcher, defaultNumResult in SearchQuery
		String query = "Easy to use for baby.";
		double []weights = {0.3, 0.5, 0.2};
		
		Searcher indexSearcher = new Searcher("data/indices/indices_baby");
		indexSearcher.set_alpha(0.2);
		SearchResult result = indexSearcher.search(query, weights);
		ArrayList<ResultDoc> resultDocs = result.getDocs();
		int rank = 1;
		if (resultDocs.size() == 0)
		    System.out.println("No results found!");
		for (ResultDoc rdoc : resultDocs) {
		    //System.out.println("\n------------------------------------------------------");
		    System.out.println(rank + ". " + " "+ rdoc.getProductID() + " "+rdoc.getOverall());
		    System.out.println(rdoc.getWeights());
		    //System.out.println(rdoc.getContent());
		    //System.out.println("------------------------------------------------------");
		    //System.out.println(result.getSnippet(rdoc).replaceAll("\n", " "));
		    ++rank;
		}
		System.out.print("> ");
		
	
		
	}
}

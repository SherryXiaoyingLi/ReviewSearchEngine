package edu.virginia.cs.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher
{
    private IndexSearcher indexSearcher;
    private SpecialAnalyzer analyzer;
    private static SimpleHTMLFormatter formatter;
    private static final int numFragments = 4;
    private static final ArrayList<String> defaultField = new ArrayList<>(Arrays.asList("content"));

    /**
     * Sets up the Lucene index Searcher with the specified index.
     *
     * @param indexPath
     *            The path to the desired Lucene index.
     */
    public Searcher(String indexPath)
    {
        try
        {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
            indexSearcher = new IndexSearcher(reader);
            analyzer = new SpecialAnalyzer();
            formatter = new SimpleHTMLFormatter("****", "****");           
            
            indexSearcher.setSimilarity(new BM25Similarity());//using default BM25 formula
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public void setSimilarity(Similarity sim)
    {
        indexSearcher.setSimilarity(sim);
    }

    /**
     * The main search function.
     * @param searchQuery Set this object's attributes as needed.
     * @return
     */
    
    public SearchResult search(SearchQuery searchQuery)
    {
        BooleanQuery combinedQuery = new BooleanQuery();
        for(String field: searchQuery.fields())
        {
        		if (!field.contains("weight_") && !field.contains("aspRat_")) {
            QueryParser parser = new QueryParser(Version.LUCENE_46, field, analyzer);
            parser.setDefaultOperator(QueryParser.Operator.AND);//all query terms need to present in a matched document
            try
            {
                Query textQuery = parser.parse(searchQuery.queryText());
                combinedQuery.add(textQuery, BooleanClause.Occur.MUST);
            }
            catch(ParseException exception)
            {
                exception.printStackTrace();
            }
        		}
        		else if (field.contains("weight_")){
        			double w_i = searchQuery.queryWeight()[Integer.parseInt(field.substring(7))-1];
            		Query weightQuery = NumericRangeQuery.newDoubleRange(field, w_i-0.1, w_i+0.1, true, true);
            		combinedQuery.add(weightQuery, BooleanClause.Occur.MUST);
        		}
        }

        return runSearch(combinedQuery, searchQuery);
    }
	
    
    /**
     * The simplest search function. Searches the content field and returns a
     * the default number of results.
     *
     * @param queryText
     *            The text to search
     * @return the SearchResult
     */
    public SearchResult search(String queryText, double[] weights)
    {
    	long currentTime = System.currentTimeMillis();
    for (int i = 1; i <=weights.length; i ++ ) {
    		defaultField.add("weight_"+i);
    }
    	SearchResult results = search(new SearchQuery(queryText, weights, defaultField));
    	long timeElapsed = System.currentTimeMillis() - currentTime;
		
		System.out.format("[Info]%d documents returned for query [%s] in %.3f seconds\n", results.numHits(), queryText, timeElapsed/1000.0);
        return results;
    }

    /**
     * Performs the actual Lucene search.
     *
     * @param luceneQuery
     * @param numResults
     * @return the SearchResult
     */
    
    private SearchResult runSearch(Query luceneQuery, SearchQuery searchQuery)
    {
        try
        {
            System.out.println("\nScoring documents with " + indexSearcher.getSimilarity().toString());

            TopDocs docs = indexSearcher.search(luceneQuery, searchQuery.fromDoc() + searchQuery.numResults());
            ScoreDoc[] hits = docs.scoreDocs;
            String field = searchQuery.fields().get(0);
            
            SearchResult searchResult = new SearchResult(searchQuery, docs.totalHits);
            for(ScoreDoc hit : hits) // hit the ResultDoc id
            {
                Document doc = indexSearcher.doc(hit.doc);
                ResultDoc rdoc = new ResultDoc(hit.doc);
                //String highlighted = null;Highlighter highlighter = new Highlighter(formatter, new QueryScorer(luceneQuery));
                String content = format(doc.getField(field).toString());
                rdoc.setContent(content);
                rdoc.setProductID(format(doc.getField("productID").toString()));
                ArrayList<Double> weights = new ArrayList<>();
                for (String str: searchQuery.fields()) {
                	if (str.startsWith("weight_")) {
                		int index = Integer.parseInt(str.substring(7));
                		weights.add(index-1, Double.parseDouble(format(doc.getField(str).toString())));
                	}
                }
                rdoc.setWeights(weights);
                //String[] snippets = highlighter.getBestFragments(analyzer, field, content, numFragments);
                //highlighted = createOneSnippet(snippets);
                
                searchResult.addResult(rdoc);
               
                //searchResult.setSnippet(rdoc, highlighted);
            }

            searchResult.trimResults(searchQuery.fromDoc());
            return searchResult;
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
        return new SearchResult(searchQuery);
    }
    
    public String format(String raw) {
    		String res = raw;
    		res = res.substring(res.indexOf("<")+1, res.lastIndexOf(">"));
    		return res.substring(res.indexOf(":")+1);
    }

    /**
     * Create one string of all the extracted snippets from the highlighter
     * @param snippets
     * @return
     */
    private String createOneSnippet(String[] snippets)
    {
        String result = " ... ";
        for(String s: snippets)
            result += s + " ... ";
        return result;
    }
}

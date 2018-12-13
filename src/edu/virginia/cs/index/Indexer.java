package edu.virginia.cs.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import aspectSegmenter.AnalyzerLara;
import aspectSegmenter.Product;
import aspectSegmenter.Review;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
//import structures.Corpus;
//import structures.ReviewDoc;
import lara.LRR;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;


public class Indexer {

	public static int numAspect;
	public static Vector<Product> m_hotelList = new Vector<>();
	/**
	 * Creates the initial index files on disk
	 *
	 * @param indexPath
	 * @return
	 * @throws IOException
	 */
	private static IndexWriter setupIndex(String indexPath) throws IOException {
		File path = new File(indexPath);
		if (path.exists()) {
			System.err.println("[Error]You need to first delete this folder!");
			return null;
		}

		Analyzer analyzer = new SpecialAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
		config.setOpenMode(OpenMode.CREATE);
		config.setRAMBufferSizeMB(2048.0);

		FSDirectory dir;
		IndexWriter writer = null;
		dir = FSDirectory.open(new File(indexPath));
		writer = new IndexWriter(dir, config);

		return writer;
	}
	
	

	

	/**
	 * @param indexPath
	 *            Where to create the index
	 * @param prefix
	 *            The prefix of all the paths in the fileList
	 * @param fileList
	 *            Each line is a path to a document
	 * @throws IOException
	 * @throws JSONException 
	 */
	public static void index(String indexPath, String jsonPath, String predPath, int numLatentAspect) throws IOException, JSONException {
		System.out.println("Creating Lucene index...");
		numAspect = numLatentAspect;
		HashMap<String, String[]> prediction = LoadPrediction(predPath);
		ArrayList<JSONObject> jsons = LoadDirectory(jsonPath, ".json");
		
		
		FieldType _contentFieldType = new FieldType();

		_contentFieldType.setIndexed(true);
		_contentFieldType.setStored(true);

		IndexWriter writer = setupIndex(indexPath);
		int counter = 0;
		for (JSONObject json: jsons) {
		JSONArray jarray = json.getJSONArray("Reviews"); //all reviews in one file for multiple products
		String prev = null;
		String title = "", content = null;
		String asin, fname = null;
		String scontent = "";
		double avgrat = 0;
		
		for (int j = 0; j < jarray.length(); j++) {
			try {
				JSONObject obj = jarray.getJSONObject(j);
				title = obj.getString("summary");
				content = obj.getString("reviewText");
				asin = obj.getString("asin");
				fname = asin;
				if (!fname.equals(prev) && prev!=null || j == jarray.length()-1 && fname.equals(prev)) {
					String []pred = prediction.get(prev);
					if (pred!=null) {
					Document doc = new Document();
					doc.add(new TextField("productID", prev, Field.Store.YES));
					doc.add(new TextField("content", scontent.toString(), Field.Store.YES));
					avgrat /= counter;
					doc.add(new DoubleField("overall", avgrat, Field.Store.YES));
					scontent = "";
					avgrat= 0;
					counter = 0;
					
					int k = 0;
					for (k = 2; k < numAspect+2; k ++ ) {
						doc.add(new DoubleField("aspRat_"+(k-1), Double.parseDouble(pred[k]), Field.Store.YES));
						doc.add(new DoubleField("weight_"+(k-1), Double.parseDouble(pred[k+numAspect+1]) , Field.Store.YES));
					}
					writer.addDocument(doc);
					}
				}

				avgrat += obj.getDouble("overall");
				scontent+=(title+"\n");
				scontent+=(content+"\n");
				prev = fname;
				counter ++;
		
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
		
		}
		writer.close();
	}

	
	public static HashMap<String, String[]> LoadPrediction(String filename) throws IOException {
		HashMap<String, String[]> prediction = new HashMap<>();
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = reader.readLine();
		while (line != null) {
			prediction.put(line.substring(0, line.indexOf("\t")), line.split("\t"));
			line = reader.readLine();
		}
		return prediction;
	}

	
	public static ArrayList<JSONObject> LoadDirectory(String folder, String suffix) throws IOException {
		File dir = new File(folder);
		ArrayList<JSONObject> jsons = new ArrayList<JSONObject>();
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)) {
				jsons.add(LoadJson(f.getAbsolutePath()));
			} else if (f.isDirectory()) {
				jsons.addAll(LoadDirectory(f.getAbsolutePath(), suffix));
			}
		}	
		System.out.format("json size:%d",jsons.size());
		return jsons;
	}
	
	// sample code for demonstrating how to read a file from disk in Java
	static JSONObject LoadJson(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			StringBuffer buffer = new StringBuffer(1024);
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			reader.close();
			return new JSONObject(buffer.toString());
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!", filename);
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			System.err.format("[Error]Failed to parse json file %s!", filename);
			e.printStackTrace();
			return null;
		}
	}

}

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
import java.util.HashMap;
import java.util.Map;
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
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
//import structures.Corpus;
//import structures.ReviewDoc;
import lara.LRR;


public class Indexer {

	public static int numAspect;
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
	 */
	public static void index(String indexPath, String jsonPath, String predPath, int numLatentAspect) throws IOException {
		System.out.println("Creating Lucene index...");
		numAspect = numLatentAspect;
		HashMap<String, String[]> prediction = LoadPrediction(predPath);
		HashMap<String, JSONObject> jsons = LoadDirectory(jsonPath, ".json");
		
		FieldType _contentFieldType = new FieldType();

		_contentFieldType.setIndexed(true);
		_contentFieldType.setStored(true);

		IndexWriter writer = setupIndex(indexPath);
		int counter = 0;
				
		try {
			for (String productID: jsons.keySet() ) {
				if (prediction.containsKey(productID)) {
				JSONArray jarray = jsons.get(productID).getJSONArray("Reviews"); // one jarray for all reviews of one hotel, create one doc for it
				Document doc = new Document();
				StringBuilder content = new StringBuilder();
				
				for (int i = 0; i < jarray.length(); i++) {
					try {
						JSONObject review = jarray.getJSONObject(i);
						content.append(review.getString("Title"));
						content.append(review.getString("Content"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				doc.add(new TextField("productID", productID, Field.Store.YES));
				doc.add(new TextField("content", content.toString(), Field.Store.YES));
				
				
				String []pred = prediction.get(productID);
				
				int k = 0;
				for (k = 2; k < numAspect+2; k ++ ) {
					doc.add(new DoubleField("aspRat_"+(k-1), Double.parseDouble(pred[k]), Field.Store.YES));
					doc.add(new DoubleField("weight_"+(k-1), Double.parseDouble(pred[k+numAspect+1]) , Field.Store.YES));
				}
				
				writer.addDocument(doc);
				counter ++ ;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			System.out.println(" -> indexed " + counter + " total docs.");
			writer.close();
		}

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
	
	public static HashMap<String, JSONObject> LoadDirectory(String folder, String suffix) {
		File dir = new File(folder);
		HashMap<String, JSONObject> jsons = new HashMap<>();
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)) {
				String productID = f.getName().substring(0, f.getName().indexOf(".json"));
				jsons.put(productID,LoadJson(f.getAbsolutePath()));
			} else if (f.isDirectory()) {
				jsons.putAll((LoadDirectory(f.getAbsolutePath(), suffix)));
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

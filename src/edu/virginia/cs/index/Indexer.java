package edu.virginia.cs.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
//import structures.Corpus;
//import structures.ReviewDoc;

public class Indexer {

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
	public static void index(String indexPath, String jsonPath) throws IOException {
		System.out.println("Creating Lucene index...");
		ArrayList<JSONObject> jsons = LoadDirectory(jsonPath, ".json");
		FieldType _contentFieldType = new FieldType();

		_contentFieldType.setIndexed(true);
		_contentFieldType.setStored(true);

		IndexWriter writer = setupIndex(indexPath);
		int i = 0, counter = 0;

		try {
			for (JSONObject json : jsons) {
				JSONArray jarray = json.getJSONArray("Reviews");
				for (i = 0; i < jarray.length(); i++) {
					try {
						JSONObject review = jarray.getJSONObject(i);
						Document doc = new Document();
						/** add all fields here **/
						
						doc.add(new TextField("reviewText", review.getString("reviewText"), Field.Store.YES));
						doc.add(new TextField("reviewerID", review.getString("reviewerID"), Field.Store.YES));
						doc.add(new TextField("asin", review.getString("asin"), Field.Store.YES));
						doc.add(new TextField("reviewerName", review.getString("reviewerName"), Field.Store.YES));
						doc.add(new TextField("helpful", review.getStringFromArray("helpful"), Field.Store.YES));
						doc.add(new TextField("overall", review.doubleToString(review.getDouble("overall")), Field.Store.YES));
						doc.add(new TextField("summary", review.getString("summary"), Field.Store.YES));
						doc.add(new TextField("unixReviewTime", Long.toString(review.getLong("unixReviewTime")), Field.Store.YES));
						doc.add(new TextField("reviewTime", review.getString("reviewTime"), Field.Store.YES));
						writer.addDocument(doc);
						counter += 1;
						if (counter % 1000 == 0)
							System.out.println(" -> indexed " + counter + " docs...");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			System.out.println(" -> indexed " + counter + " total docs.");
			writer.close();
		}

	}

	public static ArrayList<JSONObject> LoadDirectory(String folder, String suffix) {
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

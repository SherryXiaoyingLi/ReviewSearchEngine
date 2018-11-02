package edu.virginia.cs.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.simple.parser.JSONParser;

import json.JSONArray;
import json.JSONException;
import org.json.simple.JSONObject;
import opennlp.tools.util.InvalidFormatException;
import structures.Corpus;
import structures.ReviewDoc;

public class DocAnalyzer extends TextAnalyzer {
	
	Corpus m_corpus;
	SimpleDateFormat m_dateParser; // to parse Yelp date format: 2014-05-22
	
	public DocAnalyzer(String tokenizerModel) throws InvalidFormatException, FileNotFoundException, IOException {
		super(tokenizerModel);
		m_corpus = new Corpus();
		m_dateParser = new SimpleDateFormat("yyyy-MM-dd");
	}
	
	public Corpus getCorpus() {
		return m_corpus;
	}
	
	// sample code for demonstrating how to recursively load files in a directory 
	public void LoadDirectory(String folder, String suffix) throws org.json.simple.parser.ParseException {
		File dir = new File(folder);
		int size = m_corpus.getCorpusSize();
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)){
				analyzeDocument(LoadJson(f.getAbsolutePath()));
			}
			else if (f.isDirectory())
				LoadDirectory(f.getAbsolutePath(), suffix);
		}
		size = m_corpus.getCorpusSize() - size;
		System.out.println("Loading " + size + " review documents from " + folder);
	}
	
	// sample code for demonstrating how to read a file from disk in Java
	ArrayList<JSONObject> LoadJson(String filename) throws org.json.simple.parser.ParseException {
		JSONObject obj;
		ArrayList<JSONObject> json = new ArrayList<>();
		JSONParser parser = new JSONParser();
		try {
			String line = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			while((line = reader.readLine()) != null) {
				//System.out.println(line);
	            obj = (JSONObject) parser.parse(line);
	            System.out.println(obj);
	            json.add(obj);
	        }
	        // Always close files.
	        reader.close();    
	        
			return json;
		} catch (IOException e) {
			System.err.format("[Error]Failed to open file %s!", filename);
			e.printStackTrace();
			return null;
		}
	}
	
	void analyzeDocument(ArrayList<JSONObject> json) {		
		//try {
	        //JSONArray jarray = json.toJSONArray(jarray)
			for(int i=0; i<json.size(); i++) {
				//try {
					JSONObject review = json.get(i);
					System.out.println(review);
					
					/**
					ReviewDoc doc = new ReviewDoc(review.getString("ReviewID"), review.getString("Author"));
					
					doc.setRating(review.getDouble("Overall"));
					doc.setDate(m_dateParser.parse(review.getString("Date")));
					
					String content = review.getString("Content");
					doc.setContent(content);					
					doc.setBoW(tokenize(content));
					
					m_corpus.addDoc(doc);
					**/
					/**
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				**/
				
			}
			/**
		} catch (JSONException e) {
			e.printStackTrace();
		}
		**/
	}
}

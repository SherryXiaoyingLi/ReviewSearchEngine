package sum;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import sum.Sentence;
import sum.Word;
import sum.Parser;
import sum.TextRankKeywords;
import sum.TextRankSummary;

public class main {
    private static Algorithm algorithm = Algorithm.TEXTRANK;
    private static Sentence[] parsedSentences;
    private static Word[] taggedWords;

    private static Parser p;
    private static Sentence[] summary;
    private static String[] keywords;

    private static int numKeywords = 3;
    private static int summaryLength = 2;

    private enum Algorithm {
	TEXTRANK
    }

    public static void main(String[] args) throws IOException {
    	HashMap<String, JSONObject> jsons = LoadDirectory("data/Reviews", ".json");
    	
    	try {
			for (String productID: jsons.keySet() ) {
				//String productID = "72572";
				JSONArray jarray = jsons.get(productID).getJSONArray("Reviews"); // one jarray for all reviews of one hotel, create one doc for it
				String name = "data/Reviews/"+productID+".txt";
				BufferedWriter main_writer = new BufferedWriter(new FileWriter(name+".sum"));
				for (int i = 0; i < jarray.length(); i++) {
					try {
						String filename = name+i;
						BufferedWriter writer = new BufferedWriter(new FileWriter(filename)) ;
						
						JSONObject review = jarray.getJSONObject(i);
						String content = review.getString("Content");
						writer.write(content);
						writer.close();
						
						getSummary(filename, main_writer);
						
						Path path = Paths.get(filename);
						Files.delete(path);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				main_writer.close();
				BufferedWriter sum_writer = new BufferedWriter(new FileWriter(name+".productSum") );
				getSummary(name+".sum", sum_writer);
				sum_writer.close();
				Files.delete(Paths.get(name+".sum"));
			} 
			 	
	} catch (JSONException e) {
			e.printStackTrace();
	}
    	
    }
    
    public static void getSummary(String filename, BufferedWriter writer) throws IOException {
    	try {
			p = new Parser(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		parsedSentences = p.getParsedSentences();
		taggedWords = p.getTaggedWords();
		keywords = new TextRankKeywords(taggedWords, numKeywords).getKeywords();

		switch (algorithm) {
		case TEXTRANK:
		    summary = new TextRankSummary(parsedSentences, summaryLength)
			    .getSummary();
		    break;
		}
		/**
		System.out.printf("\n*** KEYWORDS (%d) ***\n", numKeywords);
		for (String s : keywords) {
		    System.out.println("  - " + s);
		}
		**/
		for (Sentence s : summary) {
			writer.write(s.toString()+"\n");
		}
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

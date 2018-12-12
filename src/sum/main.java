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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

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
    private static int summaryLength = 1;

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
				BufferedWriter sum_writer = new BufferedWriter(new FileWriter("data/Reviews/sum/"+productID+".productSum") );
				getSummary(name+".sum", sum_writer);
				sum_writer.close();
				Files.delete(Paths.get(name+".sum"));
			} 
			 	
	} catch (JSONException e) {
			e.printStackTrace();
	}	
    }
    
    
    /**
    //for evaluation here
    public static void main(String[] args) throws IOException {
    	HashMap<String, ArrayList<Sentence[]>> sum_gold = new HashMap<>();
    	File dir = new File("data/Reviews/train/summaries-gold");
    	if (dir.isDirectory()) {
    		for (File dir2:dir.listFiles()) {
    			if (dir2.isDirectory()) {
    				String topicName = dir2.getName();
    				ArrayList<Sentence[]> gold = new ArrayList<>();
    				for (File f: dir2.listFiles()) {
    				 	try {
    						p = new Parser(f.getAbsolutePath());
    					} catch (IOException e) {
    						e.printStackTrace();
    					}
    					gold.add(p.getParsedSentences());
    				}
    				sum_gold.put(topicName, gold);
    			}
    		}
    	}
    			double avgrate = 0;
			for (String topic_name: sum_gold.keySet() ) {
				BufferedWriter sum_writer = new BufferedWriter(new FileWriter("data/Reviews/train/"+topic_name+".productSum") );
				Sentence[] summary = getSummary("data/Reviews/train/topics/"+topic_name+".txt.data", sum_writer);
				sum_writer.close();
				//compare result to summaries-gold
				double topicrate = 0;
				for (Sentence[] parsedSum: sum_gold.get(topic_name)) {
					double filerate = 0;
					for (Sentence sum: summary) {
						double senrate = 0;
						List<String> thisWords = sum.getWords();
						for (Sentence gold_sen: parsedSum) {
							List<String> otherWords = gold_sen.getWords();
							
							//HashSet<String> set = new HashSet<>();
							//set.addAll(otherWords);
							//double cnt = 0;
							//for (String w: set) {
							//	if (thisWords.contains(w)) {
							//		cnt ++;
							//	}
							//}
							//senrate += cnt / set.size();
							//2 added here to keep the result within 1 
							float numCommonWords = thisWords.stream().filter(w -> otherWords.contains(w)).count();
							senrate += numCommonWords / 2/Math.log(thisWords.size() * otherWords.size());
							
							//senrate += numCommonWords / otherWords.size();
						}
						senrate /= parsedSum.length;
					    filerate += senrate;
					}
					filerate /= summary.length;
					topicrate += filerate;
				}
				topicrate /= 5;
				
				System.out.println(topicrate);
				avgrate += topicrate;
			}
			avgrate /= sum_gold.size();
			System.out.println("avgrate: "+avgrate);
    }
	
	**/
    
    
    public static Sentence[] getSummary(String filename, BufferedWriter writer) throws IOException {
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
		return summary;
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

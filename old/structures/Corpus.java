package structures;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

public class Corpus {
	LinkedList<ReviewDoc> m_collection; // a list of review documents
	
	HashMap<String, Integer> m_dictionary; // dictionary of observed words in this corpus, word -> frequency
										   // you can use this structure to prepare the curve of Zipf's law
	
	public Corpus() {
		m_collection = new LinkedList<ReviewDoc>();
		m_dictionary = new HashMap<String, Integer>();
	}
	
	// **added method for question one
	public HashMap getDictionary() {
		return m_dictionary;
	}
	
	public int getCorpusSize() {
		return m_collection.size();
	}
	
	public int getDictionarySize() {
		return m_dictionary.size();	
	}
	
	public void addDoc(ReviewDoc doc) {
		m_collection.add(doc);
		
		/**
		 * INSTRUCTOR'S NOTE: based on the BoW representation of this document, you can update the m_dictionary content
		 * to maintain some global statistics here 
		 */
		// for TTF
		
		for (Entry<String, Integer> entry: doc.m_BoW.entrySet()) {
			String key = entry.getKey();
			int value = entry.getValue();
			if (!m_dictionary.containsKey(key)) {
				m_dictionary.put(key, value);
				// for DF
				//m_dictionary.put(key, 1);
			}
			else {
				m_dictionary.put(key, m_dictionary.get(key)+value);
				//m_dictionary.put(key, m_dictionary.get(key)+1);
			}
		}
		
	}
	
	public ReviewDoc getDoc(int index) {
		if (index < getCorpusSize())
			return m_collection.get(index);
		else
			return null;
	}
	
	public int getWordCount(String term) {
		if (m_dictionary.containsKey(term))
			return m_dictionary.get(term);
		else
			return 0;
	}
	
	void setWordCount(String term, int count) {
		m_dictionary.put(term, count);
	}
}

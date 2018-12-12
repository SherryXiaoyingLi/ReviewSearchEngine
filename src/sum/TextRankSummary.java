package sum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sum.Graph;
import sum.Sentence;
import sum.SummaryExtractor;

public class TextRankSummary extends TextRank implements SummaryExtractor {

    private final Sentence[] sentences;
    private final Graph<Sentence> sentenceGraph;
    private final int summaryLength;
    private HashMap<String, Float> bm25stats;

    public TextRankSummary(Sentence[] s, int l) {
        sentences = s;
        sentenceGraph = new Graph<Sentence>();
        summaryLength = l;
        bm25stats = new HashMap<>();
        bm25stats.put("N", (float)s.length);
        float sum = 0;
        for (Sentence sen: s) {
        sum +=sen.getWords().size();
        }
        bm25stats.put("navg", sum/s.length);
    }

    @Override
    public Sentence[] getSummary() {
        for (Sentence sentence : sentences) {
            sentenceGraph.add(sentence, new SentenceNode(sentence));
        }

        calculateRanks(sentenceGraph);

        return sentenceGraph.getRankedNodes().limit(summaryLength).map(n -> n.getContent())
                .toArray(Sentence[]::new);
    }

    private class SentenceNode extends TextRankNode<Sentence> {

        public SentenceNode(Sentence s) {
            super(sentenceGraph, s);
        }

        // similarity measure calculated here
        @Override
        public double calculateRelationScore(Graph<Sentence>.Node other) {
        		//return BM25(other);
        		//return TFIDF(other);
        		return PivotedLength(other);
        		
        		//List<String> thisWords = getContent().getWords();
            //List<String> otherWords = other.getContent().getWords();
        		//float numCommonWords = thisWords.stream().filter(w -> otherWords.contains(w)).count();
            //return numCommonWords / Math.log(thisWords.size() * otherWords.size());
        }
        
        public double BM25(Graph<Sentence>.Node other) {
    		//BM25
    		Set<String> thisWords = new HashSet<>();
    		Map<String, Integer> thisWordsMap = new HashMap<>();
    		Map<String, Integer> otherWords = new HashMap<>();
    		thisWords.addAll(getContent().getWords());
    		
    		for (String ow:other.getContent().getWords()) {
    			int cnt = 1;
    			if (otherWords.containsKey(ow)) {
    				otherWords.put(ow, otherWords.get(ow)+cnt);
    			}
    			else {
    				otherWords.put(ow, cnt);
    			}
    		}
    		for (String ow:getContent().getWords()) {
    			int cnt = 1;
    			if (thisWordsMap.containsKey(ow)) {
    				thisWordsMap.put(ow, thisWordsMap.get(ow)+cnt);
    			}
    			else {
    				thisWordsMap.put(ow, cnt);
    			}
    		}
    		float N = bm25stats.get("N");
    		float docLength = other.getContent().getWords().size();
    		// this(q) other(d)
    		float df = 0;
    		for (Sentence sen: sentences) {
    			boolean check = true;
    			for (String w: thisWords) {
    				if (!sen.getWords().contains(w)) {
    					check = false;
    					break;
    				}
    			}
    			if (check) {
    				df ++;
    			}
    		}
    		
    		float navg = bm25stats.get("navg"); 
    		float k1 = (float) 1.2;  
    		float k2 = 750;
    		float b = (float) 0.75;
    		float score = 0;
    		for (String w: thisWords) {
    			if (otherWords.containsKey(w)) {
    				float termFreq = otherWords.get(w);
    				float queryTermFreq = thisWordsMap.get(w);
    				score += (float) (Math.log( (N - df  + 0.5) / (df + 0.5) ) 
    	    				* ( (k1+1) * termFreq) / (k1 * (1-b+b*docLength/navg) + termFreq) ) 
    	    				* ( (k2+1)*queryTermFreq/ (k2+queryTermFreq) ); 
    			}
    		}
    		//System.out.println(score);
    		return score;
        }
        
        public double TFIDF(Graph<Sentence>.Node other) {
    		Set<String> thisWords = new HashSet<>();
    		Map<String, Integer> otherWords = new HashMap<>();
    		thisWords.addAll(getContent().getWords());
    		
    		for (String ow:other.getContent().getWords()) {
    			int cnt = 1;
    			if (otherWords.containsKey(ow)) {
    				otherWords.put(ow, otherWords.get(ow)+cnt);
    			}
    			else {
    				otherWords.put(ow, cnt);
    			}
    		}
    		float N = bm25stats.get("N");
    		float docLength = other.getContent().getWords().size();
    		// this(q) other(d)
    		float df = 0;
    		for (Sentence sen: sentences) {
    			boolean check = true;
    			for (String w: thisWords) {
    				if (!sen.getWords().contains(w)) {
    					check = false;
    					break;
    				}
    			}
    			if (check) {
    				df ++;
    			}
    		}
    		float navg = bm25stats.get("navg"); 
    		float score = 0;
    		for (String w: thisWords) {
    			if (otherWords.containsKey(w)) {
    				float termFreq = otherWords.get(w);
    				score += (float) ( (1 + Math.log(termFreq) ) * Math.log( (N + 1) / df ) );
    			}
    		}
    		//System.out.println(score);
    		return score;
        }
        
        public double PivotedLength(Graph<Sentence>.Node other) {Set<String> thisWords = new HashSet<>();
        float s = (float) 0.75;
        Map<String, Integer> thisWordsMap = new HashMap<>();
    		Map<String, Integer> otherWords = new HashMap<>();
    		thisWords.addAll(getContent().getWords());
    		
    		for (String ow:other.getContent().getWords()) {
    			int cnt = 1;
    			if (otherWords.containsKey(ow)) {
    				otherWords.put(ow, otherWords.get(ow)+cnt);
    			}
    			else {
    				otherWords.put(ow, cnt);
    			}
    		}
    		for (String ow:getContent().getWords()) {
    			int cnt = 1;
    			if (thisWordsMap.containsKey(ow)) {
    				thisWordsMap.put(ow, thisWordsMap.get(ow)+cnt);
    			}
    			else {
    				thisWordsMap.put(ow, cnt);
    			}
    		}
    		float N = bm25stats.get("N");
    		float docLength = other.getContent().getWords().size();
    		// this(q) other(d)
    		float df = 0;
    		for (Sentence sen: sentences) {
    			boolean check = true;
    			for (String w: thisWords) {
    				if (!sen.getWords().contains(w)) {
    					check = false;
    					break;
    				}
    			}
    			if (check) {
    				df ++;
    			}
    		}
    		float navg = bm25stats.get("navg");
    		float score = 0;
    		for (String w: thisWords) {
    			if (otherWords.containsKey(w)) {
    				float termFreq = otherWords.get(w);
    				float queryTermFreq = thisWordsMap.get(w);
    				score += (float) ((1+Math.log(1+Math.log(termFreq) ) ) / (1-s+s*docLength/navg) 
    	    				* queryTermFreq * Math.log( (N+1) / df));
    			}
    		}
    		//System.out.println(score);
    		return score;
        }
       
        
    }
}

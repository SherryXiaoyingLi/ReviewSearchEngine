package sum;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import sum.Graph;
import sum.NGram;
import sum.Word;
import sum.KeywordExtractor;

public class TextRankKeywords extends TextRank implements KeywordExtractor {

    private final int MAX_NGRAM_LENGTH = 3;

    private final Word[] words;
    private final Graph<Word> wordGraph;
    private final int numKeywords;

    public TextRankKeywords(Word[] w, int n) {
        words = w;
        wordGraph = new Graph<Word>();
        numKeywords = n;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String[] getKeywords() {
        for (int i = 0; i < words.length; i++) {
            Word w = words[i];
            if (isCandidate(w)) {
                PositionNode<Word> n = (PositionNode<Word>) wordGraph.get(w);
                if (n == null) {
                    wordGraph.add(w, new PositionNode<Word>(wordGraph, w, i));
                } else {
                    n.addIndex(i);
                }
            }
        }

        calculateRanks(wordGraph);

        return getKeyNGrams();
    }

    private String[] getKeyNGrams() {
        Word[] keywords = wordGraph.getRankedNodes().limit(numKeywords).map(n -> n.getContent())
                .toArray(Word[]::new);
        double threshold = wordGraph.getThreshold(numKeywords * 2);
        Set<NGram> keyNGrams = new HashSet<NGram>();
        List<Word> nGramWords = new LinkedList<>();
        boolean containsKeyword = false;
        for (Word word : words) {
            Graph<Word>.Node n = wordGraph.get(word);
            if (n != null && nGramWords.size() < MAX_NGRAM_LENGTH && n.getRank() >= threshold) {
                nGramWords.add(word);
                if (!containsKeyword) {
                    for (Word keyword : keywords) {
                        if (word.equals(keyword)) {
                            containsKeyword = true;
                            break;
                        }
                    }
                }
            } else if (!nGramWords.isEmpty()) {
                if (containsKeyword) {
                    addNGram(keyNGrams, nGramWords);
                }
                nGramWords.clear();
                containsKeyword = false;
            }
        }
        if (!nGramWords.isEmpty() && containsKeyword) {
            addNGram(keyNGrams, nGramWords);
        }
        return keyNGrams.stream().map(n -> n.toString()).limit(numKeywords).toArray(String[]::new);
    }

    private void addNGram(Set<NGram> s, List<Word> words) {
        NGram nGram = new NGram(words.toArray(new Word[words.size()]));
        if (!s.contains(nGram)) {
            Iterator<NGram> i = s.iterator();
            while (i.hasNext()) {
                NGram n = i.next();
                if (n.contains(nGram)) {
                    return;
                } else if (nGram.contains(n)) {
                    i.remove();
                    s.add(nGram);
                    return;
                }
            }
            s.add(nGram);
        }
    }

    private boolean isCandidate(Word w) {
        return w.getTag().equals("JJ") || w.getTag().charAt(0) == 'N';
    }

    /*
     * private void calculateNGramGraph(double threshold) { int pos = 0; List<Word> nGramWords = new
     * LinkedList<>(); for (Word word : words) { Graph<Word>.Node n = wordGraph.get(word); if (n !=
     * null && nGramWords.size() < MAX_NGRAM_LENGTH && n.getRank() >= threshold) {
     * nGramWords.add(word); } else { if (!nGramWords.isEmpty()) { Word[] words =
     * nGramWords.toArray(new Word[nGramWords.size()]); addNGram(pos++, words); nGramWords.clear();
     * } if (isCandidate(word)) { addNGram(pos++, word); } } } if (!nGramWords.isEmpty()) { Word[]
     * words = nGramWords.toArray(new Word[nGramWords.size()]); addNGram(pos++, words);
     * nGramWords.clear(); } }
     *
     * @SuppressWarnings("unchecked") private void addNGram(int pos, Word... words) { NGram nGram =
     * new NGram(words); System.out.println("NGram: " + nGram); PositionNode<NGram> nGramNode =
     * (PositionNode<NGram>) nGramGraph.get(nGram); if (nGramNode == null) { nGramGraph.add(nGram,
     * new PositionNode<NGram>(nGramGraph, new NGram(words), pos++)); } else {
     * nGramNode.addIndex(pos++); } }
     */

    /*
     * private String[] combineNodes(Stream<Graph<Word>.Node> words) { PositionNode<Word>[]
     * wordNodes = words.map(n -> (PositionNode<Word>) n) .toArray(PositionNode<Word>[]::new);
     *
     * List<String> combinedWordNodes = new ArrayList<>();
     *
     * for (int i = 0; i < wordNodes.length; i++) { WordNode n1 = wordNodes[i]; if (n1 != null) {
     * for (int j = i + 1; j < wordNodes.length; j++) { WordNode n2 = wordNodes[j]; if (n2 != null)
     * { int adjacency = n1.adjacency(n2);
     *
     * if (adjacency != 0) { String combined = (adjacency < 0) ? n1.getContent() .getWord() + " " +
     * n2.getContent().getWord() : n2 .getContent().getWord() + " " + n1.getContent().getWord();
     *
     * wordNodes[i] = null; wordNodes[j] = null;
     *
     * combinedWordNodes.add(combined); }
     *
     * } } if (wordNodes[i] != null) combinedWordNodes.add(wordNodes[i].getContent().getWord()); } }
     *
     * return combinedWordNodes.toArray(new String[combinedWordNodes.size()]); }
     */
}

package sum;

import java.util.List;

import sum.Graph;
import sum.Sentence;
import sum.SummaryExtractor;

public class TextRankSummary extends TextRank implements SummaryExtractor {

    private final Sentence[] sentences;
    private final Graph<Sentence> sentenceGraph;
    private final int summaryLength;

    public TextRankSummary(Sentence[] s, int l) {
        sentences = s;
        sentenceGraph = new Graph<Sentence>();
        summaryLength = l;
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

        @Override
        public double calculateRelationScore(Graph<Sentence>.Node other) {
            List<String> thisWords = getContent().getWords();
            List<String> otherWords = other.getContent().getWords();

            long numCommonWords = thisWords.stream().filter(w -> otherWords.contains(w)).count();

            return numCommonWords / Math.log(thisWords.size() * otherWords.size());
        }
    }
}

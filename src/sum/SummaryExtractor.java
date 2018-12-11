package sum;

import sum.Sentence;

public interface SummaryExtractor {

    /**
     * @return Sentence[] the summary
     * @see Sentence
     */
    public Sentence[] getSummary();
}

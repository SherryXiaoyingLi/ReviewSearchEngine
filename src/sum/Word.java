package sum;

public class Word {
    private final String word;
    private final String tag;

    public Word(String w, String t) {
	word = w;
	tag = t;
    }

    /**
     * @return the word represented by this Word
     */
    public String getWord() {
	return word;
    }

    /**
     * @return the part of speech tag for this Word
     */
    public String getTag() {
	return tag;
    }

    @Override
    public boolean equals(Object o) {
	if (o == this) {
	    return true;
	} else if (o instanceof Word) {
	    Word w = (Word) o;
	    return word.equals(w.word);
	}
	return false;
    }

    @Override
    public int hashCode() {
	return word.hashCode();
    }

    @Override
    public String toString() {
	return word + " (" + tag + ")";
    }
}

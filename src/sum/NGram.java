package sum;

import java.util.Arrays;

public class NGram {
    private final Word[] words;
    private final String phrase;

    public NGram(Word... w) {
        words = w;

        StringBuffer s = new StringBuffer();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                s.append(" ");
            }
            s.append(words[i].getWord());
        }
        phrase = s.toString();
    }

    public Word[] getWords() {
        return words;
    }

    public boolean contains(NGram n) {
        return phrase.contains(n.phrase);
    }

    @Override
    public String toString() {
        return phrase;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof NGram) {
            NGram nGram = (NGram) o;
            return Arrays.deepEquals(words, nGram.words);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(words);
    }
}

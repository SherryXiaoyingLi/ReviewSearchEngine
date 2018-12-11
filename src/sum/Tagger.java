package sum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sum.Word;

// based off of http://phpir.com/part-of-speech-tagging

public class Tagger {
    private final int LEXICON_SIZE = 92661;

    private final Map<String, List<String>> dict;

    /**
     * Constructs a new Tagger by loading in the lexicon data
     */
    public Tagger() {
        dict = new HashMap<>(LEXICON_SIZE);

        try (BufferedReader r = new BufferedReader(new InputStreamReader(getClass()
                .getResourceAsStream("lexicon.txt")))) {

            String line = r.readLine();
            while (line != null) {
                List<String> lineTokens = Arrays.asList(line.split(" "));

                String word = lineTokens.get(0);
                List<String> tags = lineTokens.subList(1, lineTokens.size());

                if (word != null && !lineTokens.isEmpty()) {
                    dict.put(word, tags);
                }

                line = r.readLine();
            }

            r.close();
        } catch (IOException e) {
            System.err.println("Unable to initilize tagger.");
            System.exit(10);
        }
    }

    /**
     * @param words
     *            Array of Strings to convert to Words
     * @return array of words to Words (tagged with part of speech)
     */
    public Word[] tag(String[] words) {
        List<Word> taggedWords = new ArrayList<Word>(words.length);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            String tag;
            if (dict.containsKey(word)) {
                tag = dict.get(word).get(0);
            } else {
                tag = "NN";
            }

            // verbs after "the" -> nouns
            if (i > 0 && taggedWords.get(i - 1).getTag().equals("DT") && tag.matches("VBD|VBP|VB")) {
                tag = "NN";
            }

            // if "." && noun -> number
            if (tag.charAt(0) == 'N' && word.contains(".")) {
                tag = "CD";
            }

            // if ends with "ed" && noun -> past particle
            if (tag.charAt(0) == 'N' && word.length() > 2
                    && word.substring(word.length() - 2).equals("ed")) {
                tag = "VBN";
            }

            // ends with "ly" -> adverb
            if (word.length() > 2 && word.substring(word.length() - 2).equals("ly")) {
                tag = "RB";
            }

            if (tag.charAt(0) == 'N' && word.length() > 2
                    && word.substring(word.length() - 2).equals("al")) {
                tag = "JJ";
            }

            if (i > 0 && tag.equals("NN") && taggedWords.get(i - 1).getWord().equals("would")) {
                tag = "VB";
            }

            if (tag.equals("NN") && word.charAt(word.length() - 1) == 's') {
                tag = "NNS";
            }

            if (tag.charAt(0) == 'N' && word.length() > 3
                    && word.substring(word.length() - 3).equals("ing")) {
                tag = "VBG";
            }

            if (i > 0 && taggedWords.get(i - 1).getTag().charAt(0) == 'N' && tag.charAt(0) == 'N') {
                List<String> tagOptions = dict.get(word);
                if (tagOptions != null) {
                    if (tagOptions.contains("VBN")) {
                        tag = "VBN";
                    } else if (tagOptions.contains("VBZ")) {
                        tag = "VBZ";
                    }
                }
            }

            taggedWords.add(new Word(word, tag));
        }

        return taggedWords.toArray(new Word[taggedWords.size()]);
    }
}

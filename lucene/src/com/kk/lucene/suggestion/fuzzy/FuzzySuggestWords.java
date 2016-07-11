package com.kk.lucene.suggestion.fuzzy;

import com.kk.lucene.suggestion.chinese.Word;
import com.kk.lucene.suggestion.chinese.WordIterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * suggestion 纠错， 目前仅支持 英文和拼音，内部使用Levenshtein算法
 */
public class FuzzySuggestWords {

    private static Directory directory;
    private static FuzzySuggester suggester;

    private static String[] words = new String[]{"word", "china", "hello", "haha"};
    private static int weights[] = new int[]{5, 4, 3, 2, 1};
    private static List<Word> wordList = new ArrayList<Word>();

    static {
        for (int i = 0; i < words.length; i++) {
            wordList.add(new Word(words[i], weights[i]));
        }
    }

    public static void createIndex() throws IOException {
        directory = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        suggester = new FuzzySuggester(analyzer);

        suggester.build(new WordIterator(wordList));

    }

    private static void lookup(String word) throws IOException {

        HashSet<BytesRef> contexts = new HashSet<BytesRef>();

        int num = 10;

        // 搜索不带 context
        List<Lookup.LookupResult> results = suggester.lookup(word, null, false, num);


        System.out.println("-- \"" + word);
        for (Lookup.LookupResult result : results) {
            System.out.println("key=" + result.key + ",weight=" + result.value);
        }
        System.out.println();
    }

    public static void main(String[] args) throws IOException {

        createIndex();

        lookup("heh");
    }

    public static Set<BytesRef> contexts() {
        Set<BytesRef> regions = new HashSet<BytesRef>();
        return regions;
    }

    public static BytesRef parse(String cont) throws UnsupportedEncodingException {
        return new BytesRef(cont.getBytes("utf-8"));
    }

    public static BytesRef parse(Word word) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(word);
        out.close();
        return new BytesRef(bos.toByteArray());
    }
}

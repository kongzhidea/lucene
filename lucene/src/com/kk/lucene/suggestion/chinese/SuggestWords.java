package com.kk.lucene.suggestion.chinese;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 支持中文，
 * <p/>
 * StandardAnalyzer，将中文  分割成 一个一个的字，
 */
public class SuggestWords {

    private static Directory directory;
    private static AnalyzingInfixSuggester suggester;

    private static String[] words = new String[]{"中华人民共和国", "中华帝国", "美国帝国主义", "美国社会"};
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
        suggester = new AnalyzingInfixSuggester(Version.LUCENE_4_10_2, directory, analyzer);

        suggester.build(new WordIterator(wordList));

        // 新增一个索引-----key, context,weight,paylod
        suggester.add(parse("中华烟不错"), contexts(), 10, null);
        suggester.refresh();

        // 更新索引
        suggester.update(parse("中华人民共和国"), contexts(), 100, null);
        suggester.refresh();
    }

    private static void lookup(String word) throws IOException {

        HashSet<BytesRef> contexts = new HashSet<BytesRef>();

        int num = 10;

        // 搜索不带 context
        List<Lookup.LookupResult> results = suggester.lookup(word, num, true, false);


        System.out.println("-- \"" + word);
        for (Lookup.LookupResult result : results) {
            System.out.println("key=" + result.key + ",weight=" + result.value);

            // 如果lookup 时候设置 doHighlight参数为true，则 result.highlightKey 为词加上高亮效果
//            System.out.println("highlight.key=" + result.highlightKey);
        }
        System.out.println();
    }

    public static void main(String[] args) throws IOException {

        createIndex();

        lookup("中华");
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

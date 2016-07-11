package com.kk.lucene.combat.suggestion;

import com.kk.lucene.combat.model.Book;
import com.kk.lucene.combat.service.BookService;
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

    private static BookService bookService = new BookService();


    public static void main(String[] args) throws IOException {

        createIndex();

        lookup("风云");
    }

    public static void createIndex() throws IOException {
        directory = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        suggester = new AnalyzingInfixSuggester(Version.LUCENE_4_10_2, directory, analyzer);

        List<Book> books = bookService.getBookList();
        List<Word> wordList = new ArrayList<Word>();
        for (Book book : books) {
            wordList.add(new Word(book.getTitle(), 1));
        }
        suggester.build(new WordIterator(wordList));
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

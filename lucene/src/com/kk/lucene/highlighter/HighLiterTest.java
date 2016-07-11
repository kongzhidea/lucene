package com.kk.lucene.highlighter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;

public class HighLiterTest {

    private static int[] ids = new int[]{1, 2, 3, 4};
    private static String[] names = new String[]{"kong", "zhi", "hui", "ma"};
    private static String[] descriptions = new String[]{"like kong", "like zhi 孔智慧", "like hui java 孔智慧", "like ma java book and java"};


    private static Analyzer analyzer = new StandardAnalyzer();
    private static Directory directory = null;

    public static void main(String[] args) throws Exception {

        createIndex();


        DirectoryReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser parser = new QueryParser("desc", new StandardAnalyzer());
        Query query = parser.parse("like java");

        int limit = 10;
        TopDocs topDocs = searcher.search(query, limit);

        ScoreDoc[] scoreDosc = topDocs.scoreDocs;
//        for (ScoreDoc sd : scoreDosc) {
//            Document d = searcher.doc(sd.doc);
//            System.out.println(String.format("id=%s,name=%s,desc=%s", d.get("id"), d.get("name"), d.get("desc")));
//        }
        SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter(
                "<font color='red'>", "</font>");

        Highlighter highlighter = new Highlighter(htmlFormatter,
                new QueryScorer(query));

        for (ScoreDoc sd : scoreDosc) {
            Document doc = searcher.doc(sd.doc);
            String text = doc.get("desc");

            TokenStream tokenStream = TokenSources.getAnyTokenStream(
                    searcher.getIndexReader(), sd.doc, "desc", analyzer);
            int maxNumFragments = 10;
            TextFragment[] frag = highlighter.getBestTextFragments(tokenStream,
                    text, false, maxNumFragments);

            for (int j = 0; j < frag.length; j++) {
                if ((frag[j] != null) && (frag[j].getScore() > 0)) {
                    System.out.println((frag[j].toString()));
                }
            }

            //  或者如下方式
//            String[] conts = highlighter.getBestFragments(tokenStream, text, maxNumFragments);
//            System.out.println(Arrays.asList(conts));
        }
    }

    //  内存索引
    private static void createIndex() throws IOException {
        directory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        int len = ids.length;
        for (int i = 0; i < len; i++) {
            Document document = new Document();
            Field idField = new IntField("id", ids[i], Store.YES);
            Field nameField = new StringField("name", names[i], Store.YES);
            Field descFiled = new TextField("desc", descriptions[i], Store.YES);

            document.add(idField);
            document.add(nameField);
            document.add(descFiled);
            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }
}

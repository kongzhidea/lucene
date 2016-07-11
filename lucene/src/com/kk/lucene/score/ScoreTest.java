package com.kk.lucene.score;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;

public class ScoreTest {

    private static int[] ids = new int[]{1, 2, 3, 4};
    private static String[] names = new String[]{"kong", "zhi", "hui", "ma"};
    private static String[] descriptions = new String[]{"like kong", "like zhi 孔智慧", "like hui java 孔智慧 and java", "like ma java book "};


    private static Analyzer analyzer = new StandardAnalyzer();
    private static Directory directory = null;

    public static void main(String[] args) throws Exception {

        createIndex();


        DirectoryReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        QueryParser parser = new QueryParser("desc", new StandardAnalyzer());
        Query query = parser.parse("java");

        int limit = 10;
        TopDocs topDocs = searcher.search(query, limit);

        ScoreDoc[] scoreDosc = topDocs.scoreDocs;
        for (ScoreDoc sd : scoreDosc) {
            Document d = searcher.doc(sd.doc);
            System.out.println(String.format("id=%s,name=%s,desc=%s", d.get("id"), d.get("name"), d.get("desc")));
            System.out.println("score=" + sd.score);
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
            Field idField = new IntField("id", ids[i], Field.Store.YES);
            Field nameField = new StringField("name", names[i], Field.Store.YES);
            Field descFiled = new TextField("desc", descriptions[i], Field.Store.YES);

            descFiled.setBoost(ids[i]);

            document.add(idField);
            document.add(nameField);
            document.add(descFiled);

            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }
}

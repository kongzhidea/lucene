package com.kk.lucene.score;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class SortTest {


    private static int[] ids = new int[]{1, 2, 3, 4};
    private static String[] names = new String[]{"kong", "zhi", "zhi", "ma"};
    private static String[] descriptions = new String[]{"like kong zhi", "like zhi 孔智慧", "like hui java 孔智慧", "like ma java"};

    private static Directory directory = null;

    public static void main(String[] args) throws Exception {
        createIndex();

        SortTest ss = new SortTest();
        /**sort没有set sort时，默认按照关联性进行排序**/
        Sort sort = new Sort();  //true表示倒序，默认是false表示正序

        //先按照name正序，再按照id正序。
        sort.setSort(new SortField("name", SortField.Type.STRING), new SortField("id", SortField.Type.INT));

        // 按照id倒序
//        sort.setSort(new SortField("id", SortField.Type.INT, true));

//        sort.setSort(SortField.FIELD_SCORE); // 按照评分排序

        ss.searchByScore("like java", sort);
    }

    private static void createIndex() throws IOException {
        directory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, config);

        int len = ids.length;
        for (int i = 0; i < len; i++) {
            Document document = new Document();
            Field idField = new IntField("id", ids[i], Field.Store.YES);
            Field nameField = new StringField("name", names[i], Field.Store.YES);
            Field descFiled = new TextField("desc", descriptions[i], Field.Store.YES);

            document.add(idField);
            document.add(nameField);
            document.add(descFiled);
            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }

    /**
     * 按照评分进行排序
     */
    public void searchByScore(String queryStr, Sort sort) {
        try {
            IndexSearcher search = new IndexSearcher(DirectoryReader.open(directory));

            QueryParser qp = new QueryParser("desc", new StandardAnalyzer());
            Query q = qp.parse(queryStr);
            TopDocs tds = null;
            if (sort != null) {
                tds = search.search(q, 200, sort);
            } else {
                tds = search.search(q, 200);
            }
            for (ScoreDoc sd : tds.scoreDocs) {
                Document doc = search.doc(sd.doc);
                System.out.println(doc.get("id") + "---->" +
                        doc.get("name") + "[" + doc.get("desc") + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
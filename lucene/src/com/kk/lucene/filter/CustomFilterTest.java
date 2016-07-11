package com.kk.lucene.filter;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class CustomFilterTest {

    private static int[] ids = new int[]{1, 2, 3, 4};
    private static String[] names = new String[]{"kong", "zhi", "zhi", "ma"};
    private static String[] descriptions = new String[]{"like kong zhi", "like zhi 孔智慧", "like hui java 孔智慧", "like ma java"};

    private static Directory directory = null;

    public static void main(String[] args) throws Exception {
        createIndex();

        testFilter();

        System.out.println("..............");
        searchByCustomFilter();
    }

    private static void testFilter() throws IOException {

//        Filter filter = TermRangeFilter.newStringRange("name", "a", "n", true, true);
//        Filter filter = NumericRangeFilter.newIntRange("id", 2, 3, true, true);

        Filter filter = new QueryWrapperFilter(NumericRangeQuery.newIntRange("id", 2, 3, true, true));
        try {
            IndexSearcher search = new IndexSearcher(DirectoryReader.open(directory));

            Query q = new MatchAllDocsQuery();

            TopDocs tds = search.search(q, filter, 200);
            for (ScoreDoc sd : tds.scoreDocs) {
                Document doc = search.doc(sd.doc);
                System.out.println(doc.get("id") + "---->" +
                        doc.get("name") + "[" + doc.get("desc") + "]");
            }
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * 用户自定义Filter，通过Filter进行数据过滤，在这里可以通过ioc注入方式或者配置文件的方式针对多种情况添加不通的过滤器
     */
    public static void searchByCustomFilter() {
        try {
            IndexSearcher search = new IndexSearcher(DirectoryReader.open(directory));

            Query q = new TermQuery(new Term("desc", "like"));

            // 目前此自定义fiter 只能针对StringField进行过滤。
            Filter filter = new MyIDFilter(new FilterAccessor() {

                @Override
                public String[] needOperateValues() {
                    //显示ID
                    String[] ids = new String[]{"zhi"};
                    return ids;
                }

                @Override
                public String getField() {
                    String field = "name";
                    return field;
                }

                @Override
                public boolean hasSet() {
                    return true;
                }

            });
//            TopDocs tds = search.search(q, 200);
            TopDocs tds = search.search(q, filter, 200);
            for (ScoreDoc sd : tds.scoreDocs) {
                Document doc = search.doc(sd.doc);
                System.out.println(doc.get("id") + "---->" +
                        doc.get("name") + "[" + doc.get("desc") + "]");
            }
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
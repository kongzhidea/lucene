package com.kk.lucene.zhongwen.mmseg4j;

import com.chenlb.mmseg4j.analysis.MaxWordAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;

public class ZnSearchTest {

    private static int[] ids = new int[]{1, 2, 3, 4};
    private static String[] names = new String[]{"kong", "zhi", "hui", "ma"};
    private static String[] descriptions = new String[]{"中华人民共和国帝国", "中华帝国", "美国资本主义帝国", "资本主义社会"};

    private static Directory directory = null;

    public static void main(String[] args) throws Exception {
        createIndex();

        testQuery();

        System.out.println("...........");

        testQueryHighliter();
    }

    private static void createIndex() throws IOException {
        directory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, new MaxWordAnalyzer());
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

    private static void testQuery() throws IOException, ParseException {
        DirectoryReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        // 将descriptions 中文分词，如果term.value在分词的词组中，则可以找到，否则找不到。
        Query query = new TermQuery(new Term("desc", "帝国"));
        // 将descriptions 中文分词，再讲query 中文分词， 按照匹配度查找。
//        QueryParser parser = new QueryParser("desc", new MaxWordAnalyzer());
//        Query query = parser.parse("中华帝国");

        int limit = 10;
        TopDocs topDocs = searcher.search(query, limit);

        ScoreDoc[] scoreDosc = topDocs.scoreDocs;
        for (ScoreDoc sd : scoreDosc) {
            Document d = searcher.doc(sd.doc);
            System.out.println(String.format("id=%s,name=%s,desc=%s", d.get("id"), d.get("name"), d.get("desc")));
        }
    }

    private static void testQueryHighliter() throws IOException, ParseException, InvalidTokenOffsetsException {
        DirectoryReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);

        // 将descriptions 中文分词，如果term.value在分词的词组中，则可以找到，否则找不到。
//        Query query = new TermQuery(new Term("desc", "帝国"));
        // 将descriptions 中文分词，再讲query 中文分词， 按照匹配度查找。
        QueryParser parser = new QueryParser("desc", new MaxWordAnalyzer());
        Query query = parser.parse("中华帝国");

        int limit = 10;
        TopDocs topDocs = searcher.search(query, limit);

        ScoreDoc[] scoreDosc = topDocs.scoreDocs;

        SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter(
                "<font color='red'>", "</font>");

        Highlighter highlighter = new Highlighter(htmlFormatter,
                new QueryScorer(query));

        for (ScoreDoc sd : scoreDosc) {
            Document doc = searcher.doc(sd.doc);
            String text = doc.get("desc");

            TokenStream tokenStream = TokenSources.getAnyTokenStream(
                    searcher.getIndexReader(), sd.doc, "desc", new MaxWordAnalyzer());
            int maxNumFragments = 10;
            TextFragment[] frag = highlighter.getBestTextFragments(tokenStream,
                    text, false, maxNumFragments);

            for (int j = 0; j < frag.length; j++) {
                if ((frag[j] != null) && (frag[j].getScore() > 0)) {
                    System.out.println("high.." + (frag[j].toString()));
                }
            }
        }
    }
}

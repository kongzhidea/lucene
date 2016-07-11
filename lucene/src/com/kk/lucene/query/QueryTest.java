package com.kk.lucene.query;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.Arrays;

public class QueryTest {

    private static int[] ids = new int[]{1, 2, 3, 4};
    private static String[] names = new String[]{"kong", "zhi", "hui", "ma"};
    private static String[] descriptions = new String[]{"like kong zhi", "like zhi 孔智慧", "like hui java 孔智慧", "like ma java"};

    private static Directory directory = null;

    public static void main(String[] args) throws Exception {
        createIndex();
        testQuery();
    }

    private static void createIndex() throws IOException {
        directory = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, config);

        int len = ids.length;
        for (int i = 0; i < len; i++) {
            Document document = new Document();
            Field idField = new IntField("id", ids[i], Store.YES);
            Field nameField = new StringField("name", names[i], Store.YES);
            Field descFiled = new TextField("desc", descriptions[i], Store.YES);

            document.add(idField);
            document.add(nameField);
            document.add(new StringField("name", StringUtils.reverse(names[i]), Store.YES));
            document.add(descFiled);
            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }

    private static void testQuery() throws IOException, ParseException {
//        directory = FSDirectory.open(new File(INDEX));
        DirectoryReader reader = DirectoryReader.open(directory);

        IndexSearcher searcher = new IndexSearcher(reader);
        //Query query = new TermQuery(new Term("id", "1"));  无法针对 IntField类型来搜索
//        Query query = NumericRangeQuery.newIntRange("id", 1, 1, true, true);
//        Query query = new TermQuery(new Term("desc", "java"));
//        Query query = TermRangeQuery.newStringRange("name", "a", "n", true, true);

        QueryParser parser = new QueryParser("name", new StandardAnalyzer());
        Query query = parser.parse(StringUtils.reverse("hui"));

//        Query query = new TermQuery(new Term("name", StringUtils.reverse("hui")));

//        Query query = NumericRangeQuery.newIntRange("id", 1, 1, true, true);
        System.out.println(query.toString());

//        BooleanQuery query = new BooleanQuery();
//        Query query1 = NumericRangeQuery.newIntRange("id", 1, 3, true, true);
//        Query query2 = new TermQuery(new Term("desc", "java"));
//        query.add(query1, BooleanClause.Occur.MUST);
//        query.add(query2, BooleanClause.Occur.MUST);

//        QueryParser parser = new QueryParser("desc", new StandardAnalyzer());
//        parser.setDefaultOperator(QueryParser.Operator.AND);
//        Query query = parser.parse("java 智");

//        Term term = new Term("desc","like");
//        PrefixQuery query = new PrefixQuery(term);

//        QueryParser parser = new QueryParser("desc", new StandardAnalyzer());
//        Query query = parser.parse("l*");


//        QueryParser parser = new QueryParser("desc", new StandardAnalyzer());
//        Query query = parser.parse("like zhi");

//        PhraseQuery query = new PhraseQuery();
//        query.setSlop(1); //可以设置slop, 最多隔一个词距离
//        query.add(new Term("desc", "like"));
//        query.add(new Term("desc", "java"));

//        Query query = new FuzzyQuery(new Term("name", "kpmg"));

//        Term term = new Term("name", "k*g");
//
//        WildcardQuery query = new WildcardQuery(term);

//        Term t = new Term("desc", "hui");
//        SpanTermQuery spanTermQuery = new SpanTermQuery(t);
//        SpanFirstQuery query = new SpanFirstQuery(spanTermQuery, 2);

//        Term t1 = new Term("desc", "like");
//        Term t2 = new Term("desc", "java");
//
//        SpanTermQuery query1 = new SpanTermQuery(t1);
//        SpanTermQuery query2 = new SpanTermQuery(t2);
//
//        SpanQuery[] queryarray = new SpanQuery[]{query1, query2};
//        SpanNearQuery query = new SpanNearQuery(queryarray, 1, true);

        // 构造布尔查询（可根据你的要求随意组合）
//        BooleanClause.Occur[] flags = new BooleanClause.Occur[]{
//                BooleanClause.Occur.MUST, BooleanClause.Occur.MUST};
//
//        Query query = MultiFieldQueryParser.parse("zhi", new String[]{
//                "name", "desc"}, flags, new StandardAnalyzer());

//        Query query = new MatchAllDocsQuery();

        int limit = 10;
        TopDocs topDocs = searcher.search(query, limit);

        ScoreDoc[] scoreDosc = topDocs.scoreDocs;
        for (ScoreDoc sd : scoreDosc) {
            Document d = searcher.doc(sd.doc);
            System.out.println(String.format("id=%s,name=%s,desc=%s", d.get("id"), d.get("name"), d.get("desc")));

            System.out.println(Arrays.asList(d.getValues("name")));
        }
    }
}

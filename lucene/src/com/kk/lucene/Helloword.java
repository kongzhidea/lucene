package com.kk.lucene;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * 1.把article对象存放在索引库中
 * 2.根据关键词把对象从索引库中取出
 *
 * @author lenovo
 */
public class Helloword {

    private static final String INDEX = "D:\\workspace\\idea\\lucene-data\\helloworld\\index";

    public static void main(String[] args) throws Exception {
//        testCreateIndex();
        testSearchIndex();
    }

    public static void testCreateIndex() throws Exception {
        /**
         * 1.创建一个article对象 并且把信息存放进入
         * 2.调用indexWriter的api把数据存放到索引库中
         * 3.关闭indexWriter；
         */

        //调用indexWriter的api把数据存放在索引库中

        //创建索引库
        Directory directory = FSDirectory.open(new File(INDEX));
        /**
         * 创建IndexWriter
         * 1.索引库
         * 2.分词器
         * 	 索引库中分为 目录库和内容库
         *   MaxFieldLength.LIMITED出入索引库中的最大值
         *   Store  YES：存储到内容库中  NO 不存储
         *   Index  no 不存储到 目录库中   not_analyed 存储 但部分词  analyed  存储 并且分词
         */
        IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, iwConfig);
        //把一个article对象转换成document
        Document document = new Document();
        Field idField = new StringField("id", "1", Store.YES);
        Field titleField = new TextField("title", "lucene可以做搜索引擎", Store.YES);
        Field contentField = new TextField("content", "百度，都是很好的索引", Store.YES);
        document.add(idField);
        document.add(titleField);
        document.add(contentField);
        indexWriter.addDocument(document);


        //关闭indexWriter
        indexWriter.close();
    }

    public static void testSearchIndex() throws Exception {
        /**
         * 1.创建一个IndexSearch对象
         * 2.调用search方法进行检索
         * 3.输出内容
         */
        //创建一个IndexSearch对象
        Directory directory = FSDirectory.open(new File(INDEX));//指定索引库的位置
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher indexSearch = new IndexSearcher(reader);
        QueryParser queryParser = new QueryParser("title", new StandardAnalyzer());//content表示搜索的域或者说字段
        Query query = queryParser.parse("lucene");//关键词


        TopDocs topDocs = indexSearch.search(query, 2);//2 前2条记录
        int count = topDocs.totalHits;//根据关键词查询出来的总的记录数

        System.out.println("totalHits=" + count);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        //每一个scoreDoc就代表 目录库中的一个
        for (ScoreDoc scoreDoc : scoreDocs) {
            float score = scoreDoc.score;//
            int index = scoreDoc.doc;//索引的下标
            Document document = indexSearch.doc(index);
            //把document专场Article
            System.out.println(document.get("id") + ".." + document.get("title") + ".." + document.get("content"));
        }

    }
}

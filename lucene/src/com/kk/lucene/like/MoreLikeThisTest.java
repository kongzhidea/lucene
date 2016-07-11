package com.kk.lucene.like;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class MoreLikeThisTest {
    public static void main(String[] args) throws Exception {
        Directory directory = new RAMDirectory();
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
        IndexWriter writer = new IndexWriter(directory, conf);

        // Field选项，让其存储term vector加快like分析速度，
        // 否则需要在执行mlt.like(0)时动态的生成term vector
        FieldType TYPE_STORED = new FieldType();
        TYPE_STORED.setIndexed(true);
        TYPE_STORED.setTokenized(true);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setStoreTermVectors(true);
        TYPE_STORED.freeze();

        String[] docs = {"JQuery in Action", "Lucene in Action",
                "Sprint in Action", "Thinking in Java", "Thinking in PHP"
                , "Thinking in python", "Sprint in python"};

        String[] titles = {"kong zhi hui", "ma zhi min",
                "kong zhi min", "guo nian teng", "kong you hang"
                , "ma you jia", "guo wen jia"};

        for (int i = 0; i < docs.length; i++) {
            Document d = new Document();
            d.add(new Field("title", titles[i], TYPE_STORED));
            d.add(new Field("content", docs[i], TYPE_STORED));
            writer.addDocument(d);
        }
        writer.close();

        // search

        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        MoreLikeThis mlt = new MoreLikeThis(reader);
        //如果存储中已经提供term vector,可以不用提供分析器啦，
        //分析器的作用也就产生term vector
        mlt.setAnalyzer(new StandardAnalyzer());
        //不设置的话会使用名为"contens"的fieldName
        mlt.setFieldNames(new String[]{"content", "title"});
        //term在源document中出现给定的次数才是一个有效的term
        mlt.setMinTermFreq(1);
        //一个term至少要在给定的document中出现,查看源码，MoreLikeThis.createQueue中，
        //这个值和IndexReader.docFreq(Term term)返回值进行比较的
        mlt.setMinDocFreq(1);

        Query query = mlt.like(3); // docNum， 可以先根据Id查询出docNum，再构建 moreLike
        TopDocs topDocs = searcher.search(query, 10);
        for (ScoreDoc doc : topDocs.scoreDocs) {
            Document dlike = reader.document(doc.doc);
            System.out.println(dlike.getField("title").stringValue() + "," + dlike.getField("content").stringValue());
        }
    }
}

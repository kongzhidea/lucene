package com.kk.lucene.combat.util;

import com.kk.lucene.combat.model.Book;
import org.apache.lucene.document.*;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SearchUtil {

    private static String INDEX = "D:/data/lucene/combat/index";

    private static Directory directory = null;
    private static DirectoryReader reader = null;

    static {
        try {
            directory = FSDirectory.open(new File(INDEX));//指定索引库的位置
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createIndex(List<Book> bookList) throws IOException, ParseException {
        // IKAnalyzer
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, new IKAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, config);

        for (Book book : bookList) {
            Document document = new Document();
            Field idField = new IntField("id", book.getId(), Field.Store.YES);
            Field titleField = new TextField("title", book.getTitle(), Field.Store.YES);
            titleField.setBoost(3); // 默认1
            Field chapterField = new StringField("chapter", book.getChapter(), Field.Store.YES);
            Field urlField = new StringField("url", book.getUrl(), Field.Store.YES);
            Field contentFiled = new TextField("content", book.getContent(), Field.Store.NO); // 不存储

            document.add(idField);
            document.add(titleField);
            document.add(chapterField);
            document.add(urlField);
            document.add(contentFiled);
            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }

    public static IndexSearcher getSearcher() {
        try {
            if (reader == null) {
                reader = DirectoryReader.open(directory);
            } else {
                DirectoryReader tr = DirectoryReader.openIfChanged(reader);
                if (tr != null) {
                    reader.close();
                    reader = tr;
                }
            }
            return new IndexSearcher(reader);
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

package com.kk.lucene.combat.init;

import com.kk.lucene.combat.model.Book;
import com.kk.lucene.combat.service.BookService;
import com.kk.lucene.combat.util.SearchUtil;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

/**
 * 生成索引
 */
public class SearchBootstrap {
    private static BookService bookService = new BookService();

    public static void main(String[] args) throws IOException, ParseException {
        List<Book> list = bookService.getBookList();
        SearchUtil.createIndex(list);
    }
}

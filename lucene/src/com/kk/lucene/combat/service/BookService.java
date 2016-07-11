package com.kk.lucene.combat.service;

import com.alibaba.fastjson.JSON;
import com.kk.lucene.combat.model.Book;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 目前是存储到文件
public class BookService {
    private String DATA = "D:/data/lucene/combat/data";

    public void addBook(Book book) {
        File file = new File(DATA + "/" + book.getId() + ".txt");
        String json = JSON.toJSONString(book);
        try {
            FileUtils.write(file, json, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Book> getBookList() {
        List<Book> list = new ArrayList<Book>();
        File dir = new File(DATA);
        File[] files = dir.listFiles();
        for (File file : files) {
            Book book = getBook(Integer.valueOf(file.getName().replace(".txt", "")));
            if (book != null) {
                list.add(book);
            }
        }
        return list;
    }

    public Book getBook(int id) {
        File file = new File(DATA + "/" + id + ".txt");
        if (!file.exists()) {
            return null;
        }
        try {
            String c = FileUtils.readFileToString(file, "utf-8");

            return JSON.parseObject(c, Book.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

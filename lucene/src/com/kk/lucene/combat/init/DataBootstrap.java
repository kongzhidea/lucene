package com.kk.lucene.combat.init;

import com.kk.lucene.combat.model.Book;
import com.kk.lucene.combat.service.BookService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * 抓取文件
 */
public class DataBootstrap {

    private static String book_url = "http://www.yaerwen.com/files/article/html/0/594/index.html";
    private static String book_url_prefix = "http://www.yaerwen.com/files/article/html/0/594/";

    private static BookService bookService = new BookService();

    public static void main(String[] args) throws IOException {
//        System.out.println(getChapter("第七十二章冲进去")); // 第七十二章
//        System.out.println(getTitle("第七十二章冲进去")); // 冲进去
//        System.out.println(getId("23423.html")); // 23423

        if (true) { //
            return;
        }

        Document doc = Jsoup.connect(book_url).get();

        Elements pawinfos = doc.getElementsByClass("shumeng_pawinfo");

        Elements lis = pawinfos.get(0).getElementsByTag("li");

        for (int i = 0; i < lis.size(); i++) {
            Element li = lis.get(i);
            if (li.getElementsByTag("a").size() == 0) {
                continue;
            }
            try {
                String link = li.getElementsByTag("a").get(0).attr("href");
                String name = li.text();
                process(link, name);
            } catch (Exception e) {
                System.err.println("error.line=" + li);
                e.printStackTrace();
            }
        }
    }

    private static void process(String link, String name) throws IOException {
        int id = getId(link);
        Book book = new Book();
        book.setId(id);
        book.setChapter(getChapter(name));
        book.setTitle(getTitle(name));
        book.setUrl(book_url_prefix + link);

        System.out.println(book);
//        Document doc = Jsoup.connect(book.getUrl()).get();
//        String content = doc.getElementById("content").html();
//        book.setContent(content);
//
//        bookService.addBook(book);

    }

    public static String getChapter(String name) {
        int idx = name.indexOf("章");
        if (idx < 0) {
            return "";
        }
        return name.substring(0, idx + 1);
    }

    public static String getTitle(String name) {
        int idx = name.indexOf("章");
        if (idx < 0) {
            return "";
        }
        return name.substring(idx + 1).trim();
    }

    public static int getId(String attr) {
        int idx = attr.indexOf(".");
        if (idx < 0) {
            return 0;
        }
        return Integer.valueOf(attr.substring(0, idx));
    }
}

package com.kk.lucene.combat.query;

import com.kk.lucene.combat.model.Book;
import com.kk.lucene.combat.service.BookService;
import com.kk.lucene.combat.util.SearchUtil;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.util.List;

public class QueryTest {
    private static BookService bookService = new BookService();

    public static void main(String[] args) throws IOException, ParseException, InvalidTokenOffsetsException {
        List<Book> list = bookService.getBookList();
        for (Book book : list) {
            System.out.println(book.getTitle());
        }

        System.out.println("..................................");


//        Query query = new TermQuery(new Term("title", "疯狂"));

//        QueryParser parser = new QueryParser("title", new IKAnalyzer());
//        parser.setDefaultOperator(QueryParser.Operator.AND);
//        Query query = parser.parse("突破");
//        search(query, 100);

        String word = "双瞳中凝结出一股森然冷意";
        BooleanQuery query = new BooleanQuery();


        QueryParser parser1 = new QueryParser("title", new IKAnalyzer());
        Query query1 = parser1.parse(word);
        QueryParser parse2 = new QueryParser("content", new IKAnalyzer());
        Query query2 = parse2.parse(word);

        query.add(query1, BooleanClause.Occur.SHOULD);
        query.add(query2, BooleanClause.Occur.SHOULD);
//        search(query, 100);
        searchHighliter(query, 100);
    }

    private static void search(Query query, int limit) throws IOException {
        IndexSearcher searcher = SearchUtil.getSearcher();
        TopDocs topDocs = searcher.search(query, limit);

        ScoreDoc[] scoreDosc = topDocs.scoreDocs;
        System.out.println("length=" + scoreDosc.length);

        for (ScoreDoc sd : scoreDosc) {
            Document d = searcher.doc(sd.doc);
            System.out.println(String.format("id=%s,chapter=%s,title=%s,content=%s",
                    d.get("id"), d.get("chapter"), d.get("title"), bookService.getBook(Integer.valueOf(d.get("id"))).getContent()));
        }
    }

    private static void searchHighliter(Query query, int limit) throws IOException, InvalidTokenOffsetsException {
        IndexSearcher searcher = SearchUtil.getSearcher();
        TopDocs topDocs = searcher.search(query, limit);

        SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter(
                "<font color='red'>", "</font>");

        Highlighter highlighter = new Highlighter(htmlFormatter,
                new QueryScorer(query));

        ScoreDoc[] scoreDosc = topDocs.scoreDocs;
        System.out.println("length=" + scoreDosc.length);

        for (ScoreDoc sd : scoreDosc) {
            Document d = searcher.doc(sd.doc);
            System.out.println(String.format("id=%s,chapter=%s,title=%s",
                    d.get("id"), d.get("chapter"), d.get("title")));

            String content = bookService.getBook(Integer.valueOf(d.get("id"))).getContent();
//            TokenStream tokenStream = TokenSources.getAnyTokenStream(
//                    searcher.getIndexReader(), sd.doc, "content", new IKAnalyzer());
            TokenStream tokenStream = TokenSources.getTokenStream(
                    "content", content, new IKAnalyzer());
            int maxNumFragments = 10;
            TextFragment[] frag = highlighter.getBestTextFragments(tokenStream,
                    content, false, maxNumFragments);

            for (int j = 0; j < frag.length; j++) {
                if ((frag[j] != null) && (frag[j].getScore() > 0)) {
                    System.out.println((frag[j].toString()));
                }
            }
            System.out.println("\n\n");
        }
    }
}

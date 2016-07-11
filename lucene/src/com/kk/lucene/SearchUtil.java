package com.kk.lucene;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * 对于中文来说，Lucene提供的search基本上不能使用，使用中文分词器替换即可
 *
 * 对于检索来说，Lucene4.10默认提供了很多检索模式，包括模糊查询、正则匹配、通配符匹配等有用的匹配模式，但是在实际使用时需要考虑Lucene
 * 匹配的效率和系统的需求然后选择相应的匹配模式。
 * Lucene也提供了分页的查询方式。可以在scoredocs中进行分页，适合数据量比较小的情况，数据量太大有可能导致内存溢出；使用SearchAfter分页，
 * 每页都从索引中查询数据，查询速度较上一种慢，但是不会有内存溢出的情况出现，这也是推荐的用法。
 *
 * 注意：在Lucene中是不存在date类型的，对于日期类型来说，如果需要进行索引查询，需要将日期转换为long类型进行存储和比较。
 */
public class SearchUtil {
    private static final String INDEX = "D:\\workspace\\idea\\lucene-data\\searchutil\\index";

    private Version Lucene_Version = Version.LUCENE_4_10_2;
    private Directory directory;
    private DirectoryReader reader = null;
    private String[] ids = {"1", "2", "3", "4", "5", "6"};
    private String[] emails = {"aa@itat.org", "bb@itat.org", "cc@cc.org", "dd@sina.org", "ee@zttc.edu", "ff@itat.org"};
    private String[] contents = {
            "welcome to visited the space,I like book java",
            "hello boy, I like pingpeng ball",
            "my name is cc I like game java",
            "I like football",
            "I like football and I like basketball too",
            "I like movie and swim java"
    };
    private Date[] dates = null;
    private int[] attachs = {2, 3, 1, 4, 5, 5};
    private String[] names = {"zhangsan", "lisi", "john", "jetty", "mike", "jake"};

    public SearchUtil() {
//        directory = new RAMDirectory();
        try {
            directory = FSDirectory.open(new File(INDEX));
            setDates();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dates = new Date[ids.length];
            dates[0] = sdf.parse("2010-02-19");
            dates[1] = sdf.parse("2012-01-11");
            dates[2] = sdf.parse("2011-09-19");
            dates[3] = sdf.parse("2010-12-22");
            dates[4] = sdf.parse("2012-01-01");
            dates[5] = sdf.parse("2011-05-19");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public void index() {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(directory, new IndexWriterConfig(Lucene_Version, new StandardAnalyzer()));
            //writer.deleteAll();
            Document doc = null;
            for (int i = 0; i < ids.length; i++) {
                doc = new Document();
                doc.add(new StringField("id", ids[i], Store.YES));
                doc.add(new StringField("email", emails[i], Store.YES));
                doc.add(new TextField("content", contents[i], Store.NO));
                doc.add(new StringField("name", names[i], Store.YES));
                //存储数字
                doc.add(new IntField("attach", attachs[i], Store.YES));
                //存储日期
                doc.add(new LongField("date", dates[i].getTime(), Store.YES));

//                String et = emails[i].substring(emails[i].lastIndexOf("@")+1);
//                System.out.println(et);
                /**
                 * 在Lucene4.x中，只能给域加权，部门给文档加权，如果要提高文档的加权，需要给
                 * 文档的每个域进行加权
                 * **/

                writer.addDocument(doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public IndexSearcher getSearcher() {
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

    /**
     * 指定field进行查询，termquery不能进行数字和日期的查询
     * 日期的查询需要转成数字进行查询，
     * 数字查询使用NumbericRangeQuery
     *
     * @param field
     * @param name
     * @param num
     */
    public void searchByTerm(String field, String name, int num) {
        try {
            IndexSearcher searcher = getSearcher();
            Query query = new TermQuery(new Term(field, name));
            TopDocs tds = searcher.search(query, num);
            System.out.println("一共查询了:" + tds.totalHits);
            for (ScoreDoc sd : tds.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                System.out.println(doc.get("id") + "---->" +
                        doc.get("name") + "[" + doc.get("email") + "]-->" + doc.get("id") + "," +
                        doc.get("attach") + "," + doc.get("date"));
            }
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void searchByTermRange(String field, String start, String end, int num) {
        try {
            IndexSearcher searcher = getSearcher();
            Query query = TermRangeQuery.newStringRange(field, start, end, true, true);
            TopDocs tds = searcher.search(query, num);
            System.out.println("一共查询了:" + tds.totalHits);
            for (ScoreDoc sd : tds.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                System.out.println(doc.get("id") + "---->" +
                        doc.get("name") + "[" + doc.get("email") + "]-->" + doc.get("id") + "," +
                        doc.get("attach") + "," + doc.get("date"));
            }
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void searchByQueryParse(Query query, int num) {
        try {
            IndexSearcher searcher = getSearcher();
            TopDocs tds = searcher.search(query, num);
            System.out.println("一共查询了:" + tds.totalHits);
            for (ScoreDoc sd : tds.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                System.out.println(doc.get("id") + "---->" +
                        doc.get("name") + "[" + doc.get("email") + "]-->" + doc.get("id") + "," +
                        doc.get("attach") + "," + doc.get("date") + "==" + sd.score);
            }
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * 如果想要获取为存储到索引中得值，可以根据ID去源文件中进行查找并返回
     **/
    public void searchPage(String query, int page, int limit) {
        try {
            IndexSearcher searcher = getSearcher();
            QueryParser parser = new QueryParser("content", new StandardAnalyzer());
            Query q = null;
            try {
                q = parser.parse(query);
            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                e.printStackTrace();
            }
            TopDocs tds = searcher.search(q, 500);
            ScoreDoc[] sds = tds.scoreDocs;
            int start = (page - 1) * limit;
            int end = page * limit;
            if (end >= sds.length) end = sds.length;
            for (int i = start; i < end; i++) {
                Document doc = searcher.doc(sds[i].doc);
                String id = doc.get("id");
                int arrInt = -1;
                for (int j = 0; j < ids.length; j++) {
                    if (id.equals(ids[j])) {
                        arrInt = j;
                        break;
                    }
                }

                System.out.println(sds[i].doc + ":" + doc.get("name") + "-->" + contents[arrInt]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据页码和分页大小获取上一次的最后一个ScoreDoc
     */
    private ScoreDoc getLastScoreDoc(int pageIndex, int pageSize, Query query, IndexSearcher searcher) throws IOException {
        if (pageIndex == 1) return null;//如果是第一页就返回空
        int num = pageSize * (pageIndex - 1);//获取上一页的数量
        TopDocs tds = searcher.search(query, num);
        return tds.scoreDocs[num - 1];
    }

    /***
     * 在使用时，searchAfter查询的是指定页数后面的数据，效率更高，推荐使用
     *
     * @param query
     * @param pageIndex
     * @param pageSize
     */
    public void searchPageByAfter(String query, int pageIndex, int pageSize) {
        try {
            IndexSearcher searcher = getSearcher();
            QueryParser parser = new QueryParser("content", new StandardAnalyzer());
            Query q = null;
            try {
                q = parser.parse(query);
            } catch (org.apache.lucene.queryparser.classic.ParseException e) {
                e.printStackTrace();
            }
            //先获取上一页的最后一个元素
            ScoreDoc lastSd = getLastScoreDoc(pageIndex, pageSize, q, searcher);
            //通过最后一个元素搜索下页的pageSize个元素
            TopDocs tds = searcher.searchAfter(lastSd, q, pageSize);
            for (ScoreDoc sd : tds.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                String id = doc.get("id");
                int arrInt = -1;
                for (int j = 0; j < ids.length; j++) {
                    if (id.equals(ids[j])) {
                        arrInt = j;
                        break;
                    }
                }
                System.out.println(doc.get("name") + "-->" + contents[arrInt]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
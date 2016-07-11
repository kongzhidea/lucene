package com.kk.lucene;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * 依赖jar:Lucene-core，Lucene-analysis（使用标准分词器做测试），Lucene-queryParser
 * 作用：索引操作
 * <p/>
 * Store.YES表示将索引并且存储，Store.NO表示索引但不存储
 * 在Lucene中使用评分来确定文档的重要度和优先级。评分越高，表示文档优先级越高，进行排序显示的时候显示的位置越靠前。
 * 在Lucene4.10中，无法对整个文档进行评分，不过可以通过对文档中各个Field的评分来提高整个文档的评分，评分可以使用doc.setBoost()来设定。
 */
public class IndexUtil {
    private static Version Lucene_Version = Version.LUCENE_4_10_2;
    private static final String INDEX = "D:\\workspace\\idea\\lucene-data\\indexutil\\index";


    public static void main(String[] args) {
        IndexUtil s = new IndexUtil();

//        s.index();
        s.query();
//        s.delete("22");

//        s.forceDelete();

//        s.update();

        s.search();
    }


    private String[] ids = {"1", "2", "3", "4", "5", "6"};
    private String[] emails = {"aa@itat.org", "bb@itat.org", "cc@cc.org", "dd@sina.org", "ee@zttc.edu", "ff@itat.org"};
    private String[] contents = {
            "welcome to visited the space,I like book",
            "hello boy, I like pingpeng ball",
            "my name is cc I like game",
            "I like football",
            "I like football and I like basketball too",
            "I like movie and swim"
    };
    private Date[] dates = null;
    private int[] attachs = {2, 3, 1, 4, 5, 5};
    private String[] names = {"zhangsan", "lisi", "john", "jetty", "mike", "jake"};
    private Directory directory = null;
    private Map<String, Float> scores = new HashMap<String, Float>();
    private DirectoryReader reader = null;
    IndexWriter writer = null;

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

    //数据初始化
    public IndexUtil() {
        setDates();
        scores.put("itat.org", 2.0f);//设定评分
        scores.put("zttc.edu", 1.5f);
        try {
            directory = FSDirectory.open(new File(INDEX));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //directory = new RAMDirectory();//创建内存索引
//        index();

    }

    /**
     * 建立索引
     **/
    public void index() {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(directory, new IndexWriterConfig(Lucene_Version, new StandardAnalyzer()));
            //writer.deleteAll();
            for (int i = 0; i < ids.length; i++) {
                Document doc = new Document();
                doc.add(new StringField("id", ids[i], Store.YES));
                doc.add(new StringField("email", emails[i], Store.YES));
                doc.add(new TextField("content", contents[i], Store.NO));
                doc.add(new StringField("name", names[i], Store.YES));
                //存储数字
                doc.add(new IntField("attach", attachs[i], Store.YES));
                //存储日期
                doc.add(new LongField("date", dates[i].getTime(), Store.YES));

                String et = emails[i].substring(emails[i].lastIndexOf("@") + 1);
                /**
                 * 在Lucene4.x中，只能给域加权，不能给文档加权，如果要提高文档的加权，需要给
                 * 文档的每个域进行加权
                 * StringField field = new StringField("newScore", "test", Store.NO);
                 * field.setBoost(2.0f);//设置评分
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


    /***
     * 查询
     **/
    public void query() {
        try {
            IndexReader reader = DirectoryReader.open(directory);

            //通过reader可以有效的获取到文档的数量
            System.out.println("numDocs:" + reader.numDocs());
            System.out.println("maxDocs:" + reader.maxDoc());
            System.out.println("deleteDocs:" + reader.numDeletedDocs());
            reader.close();
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除
     **/
    public void delete(String id) {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(directory, new IndexWriterConfig(Lucene_Version, new StandardAnalyzer()));
            //参数是一个选项，可以是一个Query，也可以是一个term，term是一个精确查找的值
            //此时删除的文档并不会被完全删除，而是存储在一个回收站中的，可以恢复
            writer.deleteDocuments(new Term("id", id));
            writer.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 索引合并/优化
     **/
    public void merge() {
        //会将索引合并为2段，这两段中的被删除的数据会被清空
        //特别注意：此处Lucene在3.5之后不建议使用，因为会消耗大量的开销，
        //Lucene会根据情况自动处理的
        //将多份索引合并可以使用writer.addIndexes(d1,d2);//传入各自的Diretory或者IndexReader进行合并 
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(directory, new IndexWriterConfig(Lucene_Version, new StandardAnalyzer()));
            writer.forceMerge(2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 强制删除，将 delete的数据强制删除
     **/
    public void forceDelete() {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(directory, new IndexWriterConfig(Lucene_Version, new StandardAnalyzer()));
            writer.forceMergeDeletes();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 修改，如果没有会添加进去， 更新的时候会把现在的doc 完全 覆盖原来的
     **/
    public void update() {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(directory, new IndexWriterConfig(Lucene_Version, new StandardAnalyzer()));
            Document doc = new Document();
            /*
             * Lucene并没有提供更新，这里的更新操作其实是如下两个操作的合集
             * 先删除之后再添加
             */
            doc.add(new TextField("id", "22", Store.YES));
            doc.add(new TextField("email", "aa.bb@s", Store.YES));
            doc.add(new TextField("content", "update content like", Store.NO));
            doc.add(new StringField("name", "jackson", Store.YES));
            writer.updateDocument(new Term("id", "7"), doc);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 实现近实时查询，不关闭reader，但是Index有变化时，重新获取reader
     **/
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
     * 查询
     **/
    public void search() {
        try {
            IndexSearcher search = getSearcher();
            TermQuery query = new TermQuery(new Term("content", "like"));
            TopDocs tds = search.search(query, 10);
            for (ScoreDoc sd : tds.scoreDocs) {
                Document doc = search.doc(sd.doc);
                System.out.println("sd.doc=" + sd.doc + "," + doc.get("id") + "---->" +
                        doc.get("name") + "[" + doc.get("email") + "]-->" + doc.get("id") + "," +
                        doc.get("attach") + "," + doc.get("date") + "," + doc.getValues("email")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
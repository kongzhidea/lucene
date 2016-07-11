package com.kk.lucene.test;


import com.kk.lucene.SearchUtil;
import org.junit.Before;
import org.junit.Test;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

public class SearchUtilTest {
    private SearchUtil su = null;

    @Before
    public void init() {
        su = new SearchUtil();
    }

    @Test
    public void testInex() {
        su.index();
    }

    @Test
    public void testSearchByTerm() {
        su.searchByTerm("content", "java", 5);
    }

    @Test
    public void testSearchByTermRange() {
        //查询name  between a  and k
        su.searchByTermRange("name", "a", "k", 10);
        //由于attachs是数字类型，使用TermRange无法查询
        System.out.println("------------");
        su.searchByTermRange("attach", "2", "10", 5);
    }

    @Test
    public void testSearchByQueryParse() throws Exception {
        //1、创建QueryParser对象,默认搜索域为content
        QueryParser parser = new QueryParser("content", new StandardAnalyzer());
        //改变空格的默认操作符，以下可以改成AND
//        parser.setDefaultOperator(QueryParser.Operator.AND);
        //开启第一个字符的通配符匹配（后缀匹配）,默认关闭因为效率不高
//        parser.setAllowLeadingWildcard(true);
        //搜索content中包含有like的
        Query query = parser.parse("java");

        //有basketball或者football的，空格默认就是OR
//        query = parser.parse("basketball football");

        //改变搜索域为name为mike
//        query = parser.parse("content:like");

//        query = parser.parse("name:mike");

        //同样可以使用*和?来进行通配符匹配
//        query = parser.parse("name:l*");

        // 后缀匹配， 需要：parser.setAllowLeadingWildcard(true);
//        query = parser.parse("email:*@itat.org");

        //匹配name中没有jake但是content中必须有java的，+和-要放置到域说明前面
//        query = parser.parse("-name:jake +java ");

        //匹配一个区间，包含左右区间，注意:TO必须是大写
//        query = parser.parse("id:[1 TO 5]");

        //闭区间匹配只会匹配到2，不包含左右区间
//        query = parser.parse("id:{1 TO 3}");

        //完全匹配I Like Football的
//        query = parser.parse("\"I like football\"");

        //匹配I 和football之间有一个单词距离的
//        query = parser.parse("\"I football\"~1");

        //模糊查询
//        query = parser.parse("name:make~");

        //没有办法匹配数字范围（自己扩展Parser）
        query = parser.parse("attach:[2 TO 10]");
        su.searchByQueryParse(query, 10);
    }

    @Test
    public void testSearchPage() {
        su.searchPage("java", 1, 20);
    }

    @Test
    public void testSearchPageByAfter() {
        su.searchPageByAfter("java", 1, 20);
    }
}

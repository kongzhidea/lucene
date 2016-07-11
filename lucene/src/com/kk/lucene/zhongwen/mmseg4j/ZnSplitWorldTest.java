package com.kk.lucene.zhongwen.mmseg4j;

import com.chenlb.mmseg4j.analysis.ComplexAnalyzer;
import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.chenlb.mmseg4j.analysis.MaxWordAnalyzer;
import com.chenlb.mmseg4j.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;

public class ZnSplitWorldTest {
    /**
     * Description:         查看分词信息
     *
     * @param str      待分词的字符串
     * @param analyzer 分词器
     */
    public static void displayToken(String str, Analyzer analyzer) {
        try {
            //将一个字符串创建成Token流
            TokenStream stream = analyzer.tokenStream("", new StringReader(str));
            //lucene从4.6.0开始tokenstream使用方法更改的问题，在使用incrementtoken方法前必须调用reset方法
            stream.reset();
            //保存相应词汇
            CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
            while (stream.incrementToken()) {
                System.out.print("[" + cta + "]");
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Analyzer aly1 = new SimpleAnalyzer(); // 最大正向匹配
        Analyzer aly2 = new ComplexAnalyzer(); // 正向最大匹配, 加四个过虑规则的分词方式.
        Analyzer aly3 = new MaxWordAnalyzer(); // 最多分词. 在ComplexSeg基础上把长的词拆.
        Analyzer aly4 = new MMSegAnalyzer(); // 默认 最多分词. 在ComplexSeg基础上把长的词拆.

        String str = "中华人民共和国，京华时报1月23日报道 昨天，受一股来自中西伯利亚的强冷空气影响，本市出现大风降温天气，白天最高气温只有零下7摄氏度，同时伴有6到7级的偏北风。";
//        String str = "Lucene默认提供的分词器中有中文分词器，但是它的分词是基于单个字进行拆分的，所以在正式的项目中基本无用。所有要在项目中Lucene，需要添加另外的中分词器，比如IK、mmseg4j、paoding等。关于中文分词器的比较和适用情况，可以Google，文章很多，不是我们这里讨论的重点。如果需要使用中文分词器，也很简单，只要在使用分词器的地方，将分词器替换成我们的中文分词器即可";

        displayToken(str, aly1);
        displayToken(str, aly2);
        displayToken(str, aly3);
        displayToken(str, aly4);
    }
}

/**
 * 输出结果
 * [中华人民共和国][京华][时报][1][月][23][日][报道][昨天][受][一股][来自][中西][伯][利][亚][的][强][冷空气][影响][本市][出现][大风][降温][天气][白天][最高气温][只有][零下][7][摄氏度][同时][伴有][6][到][7][级][的][偏][北风]
 * [中华人民共和国][京华][时报][1][月][23][日][报道][昨天][受][一股][来自][中][西伯利亚][的][强][冷空气][影响][本市][出现][大风][降温][天气][白天][最高气温][只有][零下][7][摄氏度][同时][伴有][6][到][7][级][的][偏][北风]
 * [中华][华人][人民][共和][国][京华][时报][1][月][23][日][报道][昨天][受][一股][来自][中][西][伯][利][亚][的][强][冷][空气][影响][本市][出现][大风][降温][天气][白天][最高][气温][只有][零下][7][摄氏][度][同时][伴有][6][到][7][级][的][偏][北风]
 * [中华][华人][人民][共和][国][京华][时报][1][月][23][日][报道][昨天][受][一股][来自][中][西][伯][利][亚][的][强][冷][空气][影响][本市][出现][大风][降温][天气][白天][最高][气温][只有][零下][7][摄氏][度][同时][伴有][6][到][7][级][的][偏][北风]
 */
package com.kk.lucene.analyzer;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * WhitespaceAnalyzer只对文本进行空格切分；
 * <p/>
 * SimpleAnalyzer除了按空格切分之外遇到标点符号也会切分，同时还把所有的字母变成了小写的；
 * <p/>
 * StopAnalyzer在SimpleAnalyzer功能的基础上还去掉了”the”, “a”等停用词；
 * <p/>
 * StandardAnalyzer最为强大，表面上看它是按空格切分，然后去掉一些停用词，但实际上它有很强的token识别功能，像”xyz@example.com”这样的字符串它可以识别为email。
 */
public class AnalyzerUtil {

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

            stream.end();
            stream.close();
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Description:        显示分词的全部信息
     *
     * @param str
     * @param analyzer
     */
    public static void displayAllTokenInfo(String str, Analyzer analyzer) {
        try {
            //第一个参数只是标识性没有实际作用
            TokenStream stream = analyzer.tokenStream("", new StringReader(str));
            stream.reset();
            //获取词与词之间的位置增量
            PositionIncrementAttribute postiona = stream.addAttribute(PositionIncrementAttribute.class);
            //获取各个单词之间的偏移量
            OffsetAttribute offseta = stream.addAttribute(OffsetAttribute.class);
            //获取每个单词信息
            CharTermAttribute chara = stream.addAttribute(CharTermAttribute.class);
            //获取当前分词的类型
            TypeAttribute typea = stream.addAttribute(TypeAttribute.class);
            while (stream.incrementToken()) {
                System.out.print("位置增量" + postiona.getPositionIncrement() + ":\t");
                System.out.println(chara + "\t[" + offseta.startOffset() + " - " + offseta.endOffset() + "]\t<" + typea + ">");
            }
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Analyzer aly1 = new StandardAnalyzer();
        Analyzer aly2 = new StopAnalyzer();
        Analyzer aly3 = new SimpleAnalyzer();
        Analyzer aly4 = new WhitespaceAnalyzer();
        Analyzer aly5 = new CJKAnalyzer();

        String str = "hello kim,I am dennisit,我是 中国人,my email is dennisit@163.com, and my QQ is 1325103287 ,563@qq.com";

        AnalyzerUtil.displayToken(str, aly1);
        AnalyzerUtil.displayToken(str, aly2);
        AnalyzerUtil.displayToken(str, aly3);
        AnalyzerUtil.displayToken(str, aly4);
        AnalyzerUtil.displayToken(str, aly5);
    }
}
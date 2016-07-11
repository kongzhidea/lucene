package com.kk.lucene.zhongwen.ikanalyser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * @param useSmart 为true，使用智能分词策略
     *                 true:智能分词： 合并数词和量词，对分词结果进行歧义判断，最长词切分
     *                 false:非智能分词：细粒度输出所有可能的切分结果, 例如 中华人民共和国 ==> [中华人民共和国, 中华人民, 中华, 华人, 人民共和国, 人民, 共和国, 共和, 国]
     * @return
     */
    public static List<String> participle(String str, boolean useSmart) {
        List<String> list = new ArrayList<String>();//对输入进行分词
        try {

            StringReader reader = new StringReader(str);
            IKSegmenter ik = new IKSegmenter(reader, useSmart);//当为true时，分词器进行最大词长切分
            Lexeme lexeme = null;

            while ((lexeme = ik.next()) != null) {
                list.add(lexeme.getLexemeText());
            }

            if (list.size() == 0) {
                return null;
            }
            //分词后
            System.out.println("str分词后：" + list);

        } catch (IOException e1) {
            System.out.println();
        }
        return list;
    }

    public static void main(String[] args) {
        Analyzer aly1 = new IKAnalyzer(true); // 智能分词
        Analyzer aly2 = new IKAnalyzer(false); // 非只能分词

        String str = "中华人民共和国，京华时报1月23日报道 昨天，受一股来自中西伯利亚的强冷空气影响，本市出现大风降温天气，白天最高气温只有零下7摄氏度，同时伴有6到7级的偏北风。";
//        String str = "Lucene默认提供的分词器中有中文分词器，但是它的分词是基于单个字进行拆分的，所以在正式的项目中基本无用。所有要在项目中Lucene，需要添加另外的中分词器，比如IK、mmseg4j、paoding等。关于中文分词器的比较和适用情况，可以Google，文章很多，不是我们这里讨论的重点。如果需要使用中文分词器，也很简单，只要在使用分词器的地方，将分词器替换成我们的中文分词器即可";

        displayToken(str, aly1);
        displayToken(str, aly2);

        System.out.println("............");
        participle(str, true);
        participle(str, false);
    }
}

/**
 * 输出结果
 * <p/>
 * [中华人民共和国][京华][时报][1月][23日][报道][昨天][受][一股][来自][中][西伯利亚][的][强冷空气][影响][本市][出现][大风][降温][天气][白天][最高][气温][只有][零下][7][摄氏度][同时][伴有][6][到][7级][的][偏北风]
 * [中华人民共和国][中华人民][中华][华人][人民共和国][人民][共和国][共和][国][京华][时报][1][月][23][日报][日][报道][昨天][受][一股][一][股][来自][中西][西伯利亚][西伯][伯利][亚][的][强冷空气][冷空气][空气][影响][本市][出现][大风][降温][天气][白天][最高][高气][气温][只有][有][零下][零][下][7][摄氏度][摄氏][度][同时][伴有][有][6][到][7][级][的][偏北风][偏北][北风]
 * ............
 * str分词后：[中华人民共和国, 京华, 时报, 1月, 23日, 报道, 昨天, 受, 一股, 来自, 中, 西伯利亚, 的, 强冷空气, 影响, 本市, 出现, 大风, 降温, 天气, 白天, 最高, 气温, 只有, 零下, 7, 摄氏度, 同时, 伴有, 6, 到, 7级, 的, 偏北风]
 * str分词后：[中华人民共和国, 中华人民, 中华, 华人, 人民共和国, 人民, 共和国, 共和, 国, 京华, 时报, 1, 月, 23, 日报, 日, 报道, 昨天, 受, 一股, 一, 股, 来自, 中西, 西伯利亚, 西伯, 伯利, 亚, 的, 强冷空气, 冷空气, 空气, 影响, 本市, 出现, 大风, 降温, 天气, 白天, 最高, 高气, 气温, 只有, 有, 零下, 零, 下, 7, 摄氏度, 摄氏, 度, 同时, 伴有, 有, 6, 到, 7, 级, 的, 偏北风, 偏北, 北风]
 */
package com.kk.lucene.spellcheck;


import java.io.IOException;
import java.io.File;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

//搜索关键字拼音智能提示的实现，通过拼音检查库
public class SpellCheckerExample {


    private static final String INDEX = "D:\\workspace\\idea\\lucene-data\\spellchecker\\index";

    private static final String DATA = "D:\\workspace\\idea\\lucene-data\\spellchecker\\data\\dict.txt";

    /**
     * 根据字典文件创建spellchecker所使用的索引。
     *
     * @param spellIndexPath spellchecker索引文件路径
     * @param idcFilePath    原始字典文件路径
     * @throws IOException
     */
    public static void createSpellIndex(String spellIndexPath, String idcFilePath)
            throws IOException {
        Directory spellIndexDir = FSDirectory.open(new File(spellIndexPath));

        SpellChecker spellChecker = new SpellChecker(spellIndexDir);

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, null);

        // PlainTextDictionary（1 word per line）
        PlainTextDictionary dataDictionary = new PlainTextDictionary(new File(idcFilePath));

        // FileDictionary（1 string per line, optionally with a tab-separated integer value | 词组之间用tab分隔）

        spellChecker.indexDictionary(dataDictionary, config, false);
        // close
        spellIndexDir.close();
        spellChecker.close();
    }


    private static void search() throws IOException {
        Directory directory = FSDirectory.open(new File(INDEX));

        SpellChecker spell = new SpellChecker(directory);

        String wordToRespell = "中华人民供和国";

        spell.setStringDistance(new LevensteinDistance());  //#B
        //spell.setStringDistance(new JaroWinklerDistance());

        // 设置精度, 默认0.5f
        spell.setAccuracy(0.7f);

        int numSug = 5;
        String[] suggestions = spell.suggestSimilar(wordToRespell, numSug); //#C
        System.out.println(suggestions.length + " suggestions for '" + wordToRespell + "':");
        for (String suggestion : suggestions) {
            System.out.println("  " + suggestion);
        }
    }

    public static void main(String[] args) throws IOException {
//        createSpellIndex(INDEX, DATA);

        search();
    }


}
/*
  #A Create SpellCheck from existing spell check index
  #B Sets the string distance metric used to rank the suggestions
  #C Generate respelled candidates
*/

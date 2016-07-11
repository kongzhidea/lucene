package com.kk.lucene.suggestion.chinese;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class WordIterator implements InputIterator {
    private Iterator<Word> wordIterator;
    private Word current;


    public WordIterator(List<Word> wordIterator) {
        this.wordIterator = wordIterator.iterator();
    }

    public WordIterator(Iterator<Word> iterator) {
        this.wordIterator = iterator;
    }

    /**
     * 权重
     *
     * @return
     */
    @Override
    public long weight() {
        return current.getWeight();
    }

    /**
     * payload 直接从结果获取payload数据。
     * <p/>
     * 当前不设置
     *
     * @return
     */
    @Override
    public BytesRef payload() {
        return null;
    }

    /**
     * payload 直接从结果获取payload数据。
     * <p/>
     * 当前不设置
     *
     * @return
     */
    @Override
    public boolean hasPayloads() {
        return false;
    }

    /**
     * 过滤条件
     * <p/>
     * context里可以是任意的自定义数据，一般用于数据过滤， Set集合里的每一个元素都会被创建一个TermQuery
     * <p/>
     * lookup时候 可以筛选 是否 筛选context条件。
     *
     * @return
     */
    @Override
    public Set<BytesRef> contexts() {
        return null;
    }

    /**
     * 过滤条件
     * <p/>
     * context里可以是任意的自定义数据，一般用于数据过滤， Set集合里的每一个元素都会被创建一个TermQuery
     * <p/>
     * lookup时候 可以筛选 是否 筛选context条件。
     *
     * @return
     */
    @Override
    public boolean hasContexts() {
        return false;
    }

    /**
     * 指定 lookup时候的key，  key不能重复，否则覆盖原来的数据
     *
     * @return
     * @throws IOException
     */
    @Override
    public BytesRef next() throws IOException {
        if (wordIterator.hasNext()) {
            current = wordIterator.next();
            try {
                //将 word.content 指定为key
                return new BytesRef(current.getContent().getBytes("UTF8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Couldn't convert to UTF-8", e);
            }
        } else {
            return null;
        }
    }

    @Override
    public Comparator<BytesRef> getComparator() {
        return null;
    }
}

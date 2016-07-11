package com.kk.lucene.filter;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.OpenBitSet;

/**
 * 自定义过滤器，每次查询都会进行过滤，所以最好做成单例，保存在内存中。
 *
 * @author Johnny
 */
public class MyIDFilter extends Filter {

    private FilterAccessor filterAccessor;


    public MyIDFilter(FilterAccessor filterAccessor) {
        this.filterAccessor = filterAccessor;
    }

    /**
     * 对于特价商品的Filter，可以反过来处理，将符合条件的设置为1，不符合条件的默认即可（默认为0）
     */
    @Override
    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs)
            throws IOException {
        //openBitset是docIdSet的实现类,obs默认值都是0，0表示不显示，1表示显示
        //获取所有的docId
        OpenBitSet obs = new OpenBitSet(context.reader().maxDoc());
        //int base = context.docBase;//段的相对基数，保证多个段时相对位置正确

        if (filterAccessor.hasSet()) {
            set(context, obs);
        } else {
            clear(context, obs);
        }

        return obs;
    }

    /**
     * 用来设置docidset值为1，证明通过过滤
     */
    private void set(AtomicReaderContext context, OpenBitSet obs) {
        //设置不通过过滤ID的位置的值为0
        for (String id : filterAccessor.needOperateValues()) {
            try {
                DocsEnum de = context.reader().termDocsEnum(new Term(filterAccessor.getField(), id));//必须是唯一的不重复
                //保证是单个不重复的term,如果重复的话，默认会取第一个作为返回结果集,分词后的term也不适用自定义term
                if (de.nextDoc() != -1) {
                    obs.set(de.docID());//将符合条件的doc的值设置为1，默认为0
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用来设置docidset值为0，证明不通过过滤
     */
    private void clear(AtomicReaderContext context, OpenBitSet obs) {
        try {
            /** //先把元素填满 //set的值为docId，这里设置完成后，就会将值设置为1，表示会通过过滤**/
            obs.set(0, context.reader().maxDoc());
            for (String id : filterAccessor.needOperateValues()) {
                DocsEnum de = context.reader().termDocsEnum(new Term(filterAccessor.getField(), id));//必须是唯一的不重复
                //保证是单个不重复的term,如果重复的话，默认会取第一个作为返回结果集,分词后的term也不适用自定义term
                if (de.nextDoc() != -1) {
                    obs.clear(de.docID());
                    ;//将符合条件的doc的值设置为0，默认为1
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

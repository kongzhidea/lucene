package com.kk.lucene.filter;

/**
 * 定义数据处理的接口，用来提高数据处理的通用性
 * 使用情况参见customFilter和MyIdFilter
 *
 * @author Johnny
 */
public interface FilterAccessor {
    public String[] needOperateValues();//获取需要处理的元素

    public String getField();//需要进行过滤的字段（也就是所谓的域）

    /**
     * 如果返回值为true，表示needOperateValues需要进行显示，
     * 如果返回值为false,表示needOperateValues需要进行隐藏
     **/
    public boolean hasSet();//设定需要处理的数据是否通过过滤
}
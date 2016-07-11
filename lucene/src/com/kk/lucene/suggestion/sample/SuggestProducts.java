package com.kk.lucene.suggestion.sample;

import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class SuggestProducts {
    private static void lookup(AnalyzingInfixSuggester suggester, String name,
                               String region) throws IOException {

        HashSet<BytesRef> contexts = new HashSet<BytesRef>();
        contexts.add(new BytesRef(region.getBytes("UTF8")));

        int num = 10;

        // 搜索 带context
//        List<LookupResult> results = suggester.lookup(name, contexts, num, true, false);

        // 搜索不带 context
        List<LookupResult> results = suggester.lookup(name, num, true, false);


        System.out.println("-- \"" + name + "\" (" + region + "):");
        for (LookupResult result : results) {
            System.out.println("key=" + result.key);
            BytesRef bytesRef = result.payload;
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bytesRef.bytes));
            Product product = null;
            try {
                product = (Product) is.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println("product-Name:" + product.getName());
            System.out.println("product-regions:" + Arrays.asList(product.getRegions()));
            System.out.println("product-image:" + product.getImage());
            System.out.println("product-numberSold:" + product.getNumberSold());
        }
        System.out.println();
    }

    public static void main(String[] args) {
        try {
            Directory indexDir = new RAMDirectory();
            StandardAnalyzer analyzer = new StandardAnalyzer();
            AnalyzingInfixSuggester suggester = new AnalyzingInfixSuggester(Version.LUCENE_4_10_2, indexDir, analyzer);
            ArrayList<Product> products = new ArrayList<Product>();
            products.add(new Product("Electric Guitar,产品1",
                    "http://images.example/electric-guitar.jpg", new String[]{
                    "US", "CA"}, 100));
            products.add(new Product("Electric Train,产品2",
                    "http://images.example/train.jpg", new String[]{"US",
                    "CA"}, 100));
            products.add(new Product("Acoustic Guitar,产品3",
                    "http://images.example/acoustic-guitar.jpg", new String[]{
                    "US", "ZA"}, 80));
            products.add(new Product("Guarana Soda,产品4",
                    "http://images.example/soda.jpg",
                    new String[]{"ZA", "IE"}, 130));

            suggester.build(new ProductIterator(products.iterator()));

            // 新增一个索引
            suggester.add(parse("GuDRF,产品5"), contexts("US"), 1000, parse(new Product("GuDRF",
                    "http://images.example/GuDRF.jpg",
                    new String[]{"ZA", "IE"}, 1000)));
            suggester.refresh();

            lookup(suggester, "Gu", "US");
            lookup(suggester, "Gu", "ZA");
            lookup(suggester, "Gui", "CA");
            lookup(suggester, "Electric guit", "US");
            suggester.refresh();
        } catch (IOException e) {
            System.err.println("Error!");
        }
    }

    public static Set<BytesRef> contexts(String region) {
        try {
            Set<BytesRef> regions = new HashSet<BytesRef>();
            regions.add(new BytesRef(region.getBytes("UTF8")));
            return regions;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't convert to UTF-8");
        }
    }

    public static BytesRef parse(String cont) throws UnsupportedEncodingException {
        return new BytesRef(cont.getBytes("utf-8"));
    }

    public static BytesRef parse(Product product) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(product);
        out.close();
        return new BytesRef(bos.toByteArray());
    }
}
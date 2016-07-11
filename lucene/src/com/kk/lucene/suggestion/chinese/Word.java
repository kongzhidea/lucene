package com.kk.lucene.suggestion.chinese;

public class Word {
    private String content;
    private int weight;

    public Word() {
    }

    public Word(String content, int weight) {
        this.content = content;
        this.weight = weight;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}

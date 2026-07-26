package org.example.searcher;

/*
    文档id 和 词相关性权重
 */
public class Weight {
    private int docId;
    private int weight;

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}

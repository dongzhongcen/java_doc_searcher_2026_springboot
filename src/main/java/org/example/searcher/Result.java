package org.example.searcher;

public class Result {
    private String url;
    private String title;
    //是描述但不是正文
    private String desc;

    @Override
    public String toString() {
        return "Result{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

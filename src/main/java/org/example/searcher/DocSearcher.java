package org.example.searcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocSearcher {

    private  Index index = new Index();
    /*
        输入的参数为用户给出的查询词
        输出部分为搜索结果的集合
     */

    public DocSearcher(){
        index.load();
    }
    public List<Result> search(String query){
        /*
            1.规范化用户输入
            2.触发
            3.排序
            4.包装结果
         */
        String word = normalizeQuery(query);

        List<List<Weight>> termResults = new ArrayList<>();

        if (!word.equals("")){
            List<Weight> invertedList= index.getInverted(word);
            if (invertedList != null){
                invertedList.sort(new Comparator<Weight>() {
                    @Override
                    public int compare(Weight o1, Weight o2) {
                        return o2.getWeight() - o1.getWeight();
                    }
                });
                termResults.add(invertedList);
            }
        }

        List<Weight> allTermResult = mergeWeightLists(termResults);

        List<Result> results = new ArrayList<>();
        for (Weight weight : allTermResult){
            DocInfo docInfo = index.getDocInfo(weight.getDocId());
            Result result = new Result();
            result.setTitle(docInfo.getTitle());
            result.setUrl(normalizeUrl(docInfo.getUrl()));
            result.setDesc(GenDesc(docInfo.getContent(), word));
            results.add(result);
        }
        return results;
    }

    private String normalizeUrl(String url) {
        if (url == null) {
            return "";
        }
        return url.replace("\\", "/");
    }

    public static List<Weight> mergeWeightLists(List<List<Weight>> lists) {
        class Cursor {
            public int listIndex;
            public int elementIndex;
            public Weight weight;
        }

        PriorityQueue<Cursor> queue = new PriorityQueue<>(new Comparator<Cursor>() {
            @Override
            public int compare(Cursor o1, Cursor o2) {
                return o2.weight.getWeight() - o1.weight.getWeight();
            }
        });

        for (int i = 0; i < lists.size(); i++) {
            List<Weight> list = lists.get(i);
            if (list == null || list.isEmpty()) {
                continue;
            }
            Cursor cursor = new Cursor();
            cursor.listIndex = i;
            cursor.elementIndex = 0;
            cursor.weight = list.get(0);
            queue.offer(cursor);
        }

        List<Weight> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            Cursor cursor = queue.poll();
            result.add(cursor.weight);

            int nextIndex = cursor.elementIndex + 1;
            List<Weight> list = lists.get(cursor.listIndex);
            if (nextIndex < list.size()) {
                Cursor nextCursor = new Cursor();
                nextCursor.listIndex = cursor.listIndex;
                nextCursor.elementIndex = nextIndex;
                nextCursor.weight = list.get(nextIndex);
                queue.offer(nextCursor);
            }
        }
        return result;
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        return query.replaceAll("\\s+", "").toLowerCase();
    }

    private String GenDesc(String content, String word){
        Matcher firstMatcher = null;

        if (!word.equals("")) {
            //全词匹配, 不区分大小写
            Pattern pattern = buildWholeWordPattern(word);
            Matcher matcher = pattern.matcher(content);
            if(matcher.find()){
                firstMatcher = matcher;
            }
        }
        if (firstMatcher == null){
            //查找词不在正文中
            String desc = content.length() <= 160 ? content : content.substring(0, 160) + "...";
            return escapeHtml(desc);
        }

        String desc ="";
        int firstPos = firstMatcher.start();
        int descBeg = firstPos <60 ? 0 : firstPos -60;
        if (descBeg + 160 > content.length()){
            desc = content.substring((descBeg));
        }else{
            desc = content.substring(descBeg, descBeg + 160) + "...";
        }
        return highlightTerm(desc, word);
    }

    private Pattern buildWholeWordPattern(String word) {
        return Pattern.compile("(?i)(?<![a-zA-Z0-9_])" + Pattern.quote(word) + "(?![a-zA-Z0-9_])");
    }

    private String highlightTerm(String desc, String word) {
        if (word == null || word.equals("")) {
            return escapeHtml(desc);
        }

        Pattern pattern = buildWholeWordPattern(word);
        Matcher matcher = pattern.matcher(desc);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            result.append(escapeHtml(desc.substring(lastEnd, matcher.start())));
            result.append("<i>").append(escapeHtml(matcher.group())).append("</i>");
            lastEnd = matcher.end();
        }
        result.append(escapeHtml(desc.substring(lastEnd)));
        return result.toString();
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }


    public static void main(String[] args) {
        DocSearcher docSearcher = new DocSearcher();
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("输入搜索的关键词-> ");
            String query = scanner.next();
            List<Result> results = docSearcher.search(query);
            for(Result result : results) {
                System.out.println("======================================");
                System.out.println(result);
            }
        }
    }
}

package org.example;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

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
            1.分词
            2.触发
            3.排序
            4.包装结果
         */
        List<Term> terms = ToAnalysis.parse(query).getTerms();

        List<Weight> allTermResult = new ArrayList<>();


        for(Term term : terms){
            String word = term.getName();
            List<Weight> invertedList= index.getInverted(word);
            if (invertedList == null){
                continue;
            }
            allTermResult.addAll(invertedList);
        }

        allTermResult.sort(new Comparator<Weight>() {
            @Override
            public int compare(Weight o1, Weight o2) {
                return o2.getWeight() - o1.getWeight();
            }
        });

        List<Result> results = new ArrayList<>();
        for (Weight weight : allTermResult){
            DocInfo docInfo = index.getDocInfo(weight.getDocId());
            Result result = new Result();
            result.setTitle(docInfo.getTitle());
            result.setUrl(docInfo.getUrl());
            result.setDesc(GenDesc(docInfo.getContent(), terms));
            results.add(result);
        }
        return results;
    }

    private String GenDesc(String content, List<Term> terms){
        int firstPos = -1;

        for (Term term : terms){
            String word = term.getName();
            //分词库直接对词进行转小写
            //应该先把正文转小写再经行查询

            //此处需要全词匹配
            firstPos = content.toLowerCase().indexOf(" " + word + " ");
            if(firstPos >= 0){
                break;
            }
        }
        if (firstPos == -1){
            //查找词不在正文中
            return content.length() <= 160 ? content : content.substring(0, 160) + "...";
        }

        String desc ="";
        int descBeg = firstPos <60 ? 0 : firstPos -60;
        if (descBeg + 160 > content.length()){
            desc = content.substring((descBeg));
        }else{
            desc = content.substring(descBeg, descBeg + 160) + "...";
        }
        return desc;
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

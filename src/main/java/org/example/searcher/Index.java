package org.example.searcher;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.DeployConfig;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Index {
    private static final String INDEX_PATH = DeployConfig.getIndexPath();
    private static final String STOP_WORD_PATH = DeployConfig.getStopWordPath();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private ObjectMapper objectMapper = new ObjectMapper();
    /*
        提供的方法:
        1. 给出docId, 正排索引, 查询文档的详细信息
        2. 给定词语, 在倒排索引中, 查找相关词文档
        3. 往索引中加入一个新文档
        4.把索引结构存到磁盘中
        5.把磁盘中的索引数据加载到内存中
     */
    //数组下标正排索引
    private ArrayList<DocInfo> forwardIndex = new ArrayList<>();
    //使用hash来倒排 key, value
    private HashMap<String, ArrayList<Weight>> invertedIndex = new HashMap<>();

    private Object locker1 = new Object();
    private Object locker2 = new Object();
    private HashSet<String> stopWords = new HashSet<>();

    public Index() {
        loadStopWords();
    }

    public DocInfo getDocInfo(int docId){
        return forwardIndex.get(docId);
    }

    public List<Weight> getInverted(String term){
        return invertedIndex.get(term);
    }

    public void addDoc(String title, String url, String content){
        /*
            需要同时构建两种索引
         */

        DocInfo docInfo = buildForward(title, url, content);
        buildInverted(docInfo);
    }

    private void buildInverted(DocInfo docInfo) {
        /*
            当前文档的词来建立关系 所以需要分词 并统计次数
            1.正文
            2.标题

            根据结果 就知道加入那个key中
         */
        class WordCount{
            public int titleCount;
            public int contentCount;
        }

        HashMap<String, WordCount> wordCountHashMap = new HashMap<>();

        synchronized (locker2)
        {
            //标题分词
            List<Term> terms = ToAnalysis.parse(docInfo.getTitle()).getTerms();

            for(Term term : terms){
                //判断是存在
//            System.out.println(term.getName());
                String word = term.getName();
                if (isStopWord(word)) {
                    continue;
                }
                WordCount wordCount = wordCountHashMap.get(word);

                if(wordCount == null){
                    //如果不存在就设置titleCount为1 并插入
                    WordCount newWordCount = new WordCount();
                    newWordCount.titleCount = 1;
                    newWordCount.contentCount = 0;
                    wordCountHashMap.put(word, newWordCount);
                }else{
                    //如果存在就把之前的titleCount + 1
                    wordCount.titleCount += 1;
                }
            }

            terms = ToAnalysis.parse(docInfo.getContent()).getTerms();

            for(Term term : terms){
                //判断是存在
//            System.out.println(term.getName());
                String word = term.getName();
                if (isStopWord(word)) {
                    continue;
                }
                WordCount wordCount = wordCountHashMap.get(word);

                if(wordCount == null){
                    //如果不存在就设置--title--Count为1 并插入
                    WordCount newWordCount = new WordCount();
                    newWordCount.titleCount = 0;
                    newWordCount.contentCount = 1;
                    wordCountHashMap.put(word, newWordCount);
                }else{
                    //如果存在就把之前的--title--Count + 1
                    wordCount.contentCount += 1;
                }
            }
            //实现汇总到HashMap Map不可遍历,所以这里转换成Set把键值对打包在一起Entry
            for(Map.Entry<String, WordCount> entry : wordCountHashMap.entrySet()){
                List<Weight> invertedList = invertedIndex.get(entry.getKey());
                if(invertedList == null){
                    ArrayList<Weight> newInvertedList = new ArrayList<>();
                    Weight weight = new Weight();
                    weight.setDocId(docInfo.getDocId());
                    weight.setWeight(entry.getValue().titleCount * 10 + entry.getValue().contentCount);
                    newInvertedList.add(weight);
                    invertedIndex.put(entry.getKey(),newInvertedList);
                }else{
                    Weight weight = new Weight();
                    weight.setDocId(docInfo.getDocId());
                    weight.setWeight(entry.getValue().titleCount * 10 + entry.getValue().contentCount);
                    invertedList.add(weight);
                }
            }
        }

    }

    private DocInfo buildForward(String title, String url, String content) {
        DocInfo docInfo = new DocInfo();

        docInfo.setTitle(title);
        docInfo.setUrl(url);
        docInfo.setContent(content);
        synchronized (locker1){
            docInfo.setDocId(forwardIndex.size());
            forwardIndex.add(docInfo);
        }
        return docInfo;
    }

    public void save(){
        /*
            保存两个文件正序和倒序
            判断索引对应的目录是否存在不存在就创建
         */
        System.out.println(now() + " 开始保存...");
        File indexPathFile = new File(INDEX_PATH);
        if(!indexPathFile.exists()){
            indexPathFile.mkdirs();
        }

        File forwardIndexFile = new File(indexPathFile, "forward.txt");
        File invertedIndexFile = new File(indexPathFile, "index.txt");

        try {
            System.out.println(now() + " 保存正排索引: " + forwardIndexFile.getAbsolutePath());
            objectMapper.writeValue(forwardIndexFile, forwardIndex);
            System.out.println(now() + " 保存倒排索引: " + invertedIndexFile.getAbsolutePath());
            objectMapper.writeValue(invertedIndexFile, invertedIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(now() + " 保存索引完成!");
    }

    public void load(){
        System.out.println(now() + " 加载索引开始!");

        File indexPathFile = new File(INDEX_PATH);
        File forwardIndexFile = new File(indexPathFile, "forward.txt");
        File invertedIndexFile = new File(indexPathFile, "index.txt");
        try{
            forwardIndex = objectMapper.readValue(forwardIndexFile, new TypeReference<ArrayList<DocInfo>>() {});
            invertedIndex = objectMapper.readValue(invertedIndexFile, new TypeReference<HashMap<String, ArrayList<Weight>>>() {});
        } catch (StreamReadException e) {
            throw new RuntimeException(e);
        } catch (DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.out.println(now() + " 加载索引结束!");
    }

    private static String now() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    private void loadStopWords() {
        File stopWordFile = new File(STOP_WORD_PATH);
        if (!stopWordFile.exists()) {
            System.out.println(now() + " 停用词文件不存在: " + stopWordFile.getAbsolutePath());
            return;
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(STOP_WORD_PATH), StandardCharsets.UTF_8)) {
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                String word = line.trim().toLowerCase();
                if (word.equals("") || word.startsWith("#")) {
                    continue;
                }
                stopWords.add(word);
            }
            System.out.println(now() + " 加载停用词数量: " + stopWords.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isStopWord(String word) {
        return word == null || stopWords.contains(word.toLowerCase());
    }
}

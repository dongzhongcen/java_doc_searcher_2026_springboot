package org.example;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class Parser {
    private static final String INPUT_PATH="D:\\Github\\java_doc_searcher_2026\\jdk-21.0.12_doc-all\\docs\\api";
    private static Index index = new Index();
//    private static void run(){
    public static void run(){
        /*
            1.根据指定的路径列举出所有的文件(html), 还需要所有子目录
            2.针对罗列的文件路径, 打开文件, 读取文件内容, 并且解析, 构建索引
            3.在内存中构建好的索引保存到指定文件中
         */
        ArrayList<File> filelist = new ArrayList<>();
        enumFile(INPUT_PATH, filelist);
        System.out.println("待解析文件数: " + filelist.size());

        for(int i = 0; i < filelist.size(); i++){
            File file = filelist.get(i);
            System.out.println("开始解析(" + (i + 1) + "/" + filelist.size() + "):" + file.getAbsolutePath());
            parseHtml(file);
        }

        index.save();
    }

    private static void parseHtml(File file) {
        /*
        1.解析html的标题
        2.解析html对应的url
        3.解析出html对应的正文
        4.把解析出来的结果加入文件index中
         */
        String title = parseTitle(file);
        String url = parseUrl(file);
        String content = parseContent(file);
        index.addDoc(title, url, content);
    }

    private static String parseUrl(File file) {
        String part1 = "https://docs.oracle.com/en/java/javase/21/docs/api";
        String part2 = file.getAbsolutePath().substring(INPUT_PATH.length());

        return part1 + part2;
    }

    private static String parseTitle(File file) {
        String name = file.getName();

        return file.getName().substring(0, file.getName().length() - ".html".length());
    }

    public static String parseContent(File file) {
        /*
            单字符读取 > < 控制拷贝数据的开关
            FileInputStream 按字节读取  FileReader按字符读取
         */
        try (BufferedReader fileReader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            /*
                Boolean 作为是否拷贝的开关
             */
            boolean isCopy = true;
            //存结果
            StringBuilder content = new StringBuilder();

            while(true){
                /*
                    使用int作为返回值, 应对非法, 如读到末尾返回-1
                 */
                int ret = fileReader.read();
                if(ret == -1){
                    break;
                }
                /*
                    结果不是-1就是合理的字符
                 */
                char c = (char)ret;
                if(isCopy){
                    if (c == '<') {
                        isCopy = false;
                        continue;
                    }
                    if(c == '\n' || c == '\r'){
                        c = ' ';
                    }
                    content.append(c);
                    } else {
                        if(c == '>'){
                            isCopy = true;
                        }
                    }
                }

            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void enumFile(String inputPath, ArrayList<File> filelist) {
        /*
            listFiles()获取当前目录下的文件,只能看到一级目录, 想看子目录用递归实现
         */
        File rootPath = new File(inputPath);
        File[] files = rootPath.listFiles();

        for(File file : files){
//            System.out.println(file);
            /*
                如果file是一个普通文件就加入结果中,
                如果是一个目录就递归调用该函数方法, 获取子目录
             */
            if(file.isDirectory()){
                enumFile(file.getAbsolutePath(), filelist);
            }else{
                if(file.getAbsolutePath().endsWith(".html")) {
                    filelist.add(file);
                }
            }
        }
    }

    public static void main(String[] args) {
        Parser parser = new Parser();
        parser.run();
    }
}

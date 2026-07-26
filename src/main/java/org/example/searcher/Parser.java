package org.example.searcher;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class Parser {
    private static final String INPUT_PATH = Config.getJdkApiPath();
    private static Index index = new Index();

    private AtomicLong t1 = new AtomicLong(0);
    private AtomicLong t2 = new AtomicLong(0);

    public void run(){
        long beg = System.currentTimeMillis();
        System.out.println("索引制作开始!");
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
        long end = System.currentTimeMillis();
        System.out.println("索引制作完毕! 消耗时间" + (end - beg) + "ms");
    }

    public void runByThread() throws InterruptedException {

        long beg = System.currentTimeMillis();
        System.out.println("索引制作开始!");

        ArrayList<File> files = new ArrayList<>();
        enumFile(INPUT_PATH, files);
        CountDownLatch latch = new CountDownLatch(files.size());

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (File file : files){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("解析 " + file.getAbsolutePath());
                    parseHtml(file);
                    latch.countDown();
                }
            });
        }

        //所有文件countDown后, 才会阻塞结束
        latch.await();

        //非守护线程需要手动关闭
        executorService.shutdown();

        /*
            需要所有任务处理完毕再保存
         */
        index.save();

        long end = System.currentTimeMillis();
        System.out.println("索引制作完毕! 消耗时间" + (end - beg) + "ms");
        System.out.println("t1: " + t1 + ",t2 " + t2);
    }


    private void parseHtml(File file) {
        /*
        1.解析html的标题
        2.解析html对应的url
        3.解析出html对应的正文
        4.把解析出来的结果加入文件index中
         */
        String title = parseTitle(file);
        String url = parseUrl(file);
        long beg = System.nanoTime();
        String content = parseContentRegx(file);
        long mid = System.nanoTime();
        index.addDoc(title, url, content);
        long end = System.nanoTime();

        t1.addAndGet(mid - beg);
        t2.addAndGet(end - mid);
    }

    private String readFile(File file){
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
            StringBuilder content = new StringBuilder();
            while(true){
                int ret = bufferedReader.read();
                if(ret == -1){
                    break;
                }
                char c = (char)ret;
                if(c == '\n' || c =='\r'){
                    c = ' ';
                }
                content.append(c);
            }
            return content.toString();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }


    public String parseContentRegx(File file){
        /*
            读取整个文件再创建正则
         */
        String content = readFile(file);
        content = content.replaceAll("<script.*?>(.*?)</script>"," ");
        content = content.replaceAll("<.*?>", " ");
        content = content.replaceAll("\\s+", " ");
        return content;
    }


    private static String parseUrl(File file) {
        String part1 = "https://docs.oracle.com/en/java/javase/21/docs/api/";
        Path inputPath = Paths.get(INPUT_PATH);
        Path filePath = file.toPath();
        String part2 = inputPath.relativize(filePath).toString().replace(File.separatorChar, '/');

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

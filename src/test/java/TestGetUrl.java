import java.io.File;


public class TestGetUrl {
    private static final String INPUT_PATH="D:\\Github\\java_doc_searcher_2026\\jdk-21.0.12_doc-all\\docs\\api";

    public static void main(String[] args) {
        File file = new File("D:\\Github\\java_doc_searcher_2026\\jdk-21.0.12_doc-all\\docs\\api\\java.base\\java\\util\\ArrayList.html");
        /*
            先获取固定前缀: https://docs.oracle.com/en/java/javase/21/docs/api/
         */
        String part1 = "https://docs.oracle.com/en/java/javase/21/docs/api";
        String part2 = file.getAbsolutePath().substring(INPUT_PATH.length());
        String result = part1 + part2;

        System.out.println(result);
        }
}

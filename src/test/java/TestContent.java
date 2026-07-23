import org.example.Parser;

import java.io.File;

public class TestContent {
    public static void main(String[] args) {
        Parser parser = new Parser();
        File file = new File("D:\\Github\\java_doc_searcher_2026\\jdk-21.0.12_doc-all\\docs\\api\\java.base\\java\\util\\ArrayList.html");

        String result = parser.parseContent(file);
        System.out.println(result);
    }
}

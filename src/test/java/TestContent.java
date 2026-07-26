import org.example.searcher.Config;
import org.example.searcher.Parser;

import java.io.File;
import java.nio.file.Paths;

public class TestContent {
    public static void main(String[] args) {
        Parser parser = new Parser();
        File file = Paths.get(Config.getJdkApiPath(), "java.base", "java", "util", "ArrayList.html").toFile();

        String result = parser.parseContent(file);
        System.out.println(result);
    }
}

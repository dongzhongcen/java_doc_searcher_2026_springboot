import org.example.config.DeployConfig;
import org.example.searcher.Parser;

import java.io.File;

public class TestContent {
    public static void main(String[] args) {
        Parser parser = new Parser();
        File file = new File(DeployConfig.getJdkApiPath() + "/java.base/java/util/ArrayList.html");

        String result = parser.parseContent(file);
        System.out.println(result);
    }
}

import org.example.searcher.Config;

import java.io.File;
import java.nio.file.Paths;

public class TestGetName {
    public static void main(String[] args) {
        File file = Paths.get(Config.getJdkApiPath(), "java.base", "java", "util", "ArrayList.html").toFile();
        System.out.println(file.getAbsoluteFile());
        System.out.println(file.getName().substring(0, file.getName().length() - ".html".length()));
    }
}

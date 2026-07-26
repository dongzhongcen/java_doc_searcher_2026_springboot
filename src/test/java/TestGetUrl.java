import org.example.searcher.Config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


public class TestGetUrl {
    private static final String INPUT_PATH = Config.getJdkApiPath();

    public static void main(String[] args) {
        File file = Paths.get(INPUT_PATH, "java.base", "java", "util", "ArrayList.html").toFile();
        /*
            先获取固定前缀: https://docs.oracle.com/en/java/javase/21/docs/api/
         */
        String part1 = "https://docs.oracle.com/en/java/javase/21/docs/api/";
        Path inputPath = Paths.get(INPUT_PATH);
        String part2 = inputPath.relativize(file.toPath()).toString().replace(File.separatorChar, '/');
        String result = part1 + part2;

        System.out.println(result);
        }
}

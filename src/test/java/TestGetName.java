import java.io.File;

public class TestGetName {
    public static void main(String[] args) {
        File file = new File("D:\\Github\\java_doc_searcher_2026\\jdk-21.0.12_doc-all\\docs\\api\\ArrayList.html");
        System.out.println(file.getAbsoluteFile());
        System.out.println(file.getName().substring(0, file.getName().length() - ".html".length()));
    }
}

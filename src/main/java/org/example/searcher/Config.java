package org.example.searcher;

import java.nio.file.Paths;

public class Config {
    // true 表示云服务器运行, false 表示本地运行
    public static boolean isOnline = false;

    private static final String WINDOWS_PROJECT_ROOT = "D:/Github/java_doc_searcher_2026/java_doc_searcher";

    private Config() {
    }

    public static String getProjectRoot() {
        return isOnline || isOnlineEnvironment() ? System.getProperty("user.dir") : WINDOWS_PROJECT_ROOT;
    }

    private static boolean isOnlineEnvironment() {
        String appOnline = System.getenv("APP_ONLINE");
        return appOnline != null && appOnline.equalsIgnoreCase("true");
    }

    public static String getJdkApiPath() {
        return Paths.get(getProjectRoot(), "jdk-21.0.12_doc-all", "docs", "api").toString();
    }

    public static String getIndexPath() {
        return Paths.get(getProjectRoot(), "doc_search_index").toString();
    }

    public static String getStopWordPath() {
        return Paths.get(getIndexPath(), "stop_word.txt").toString();
    }
}

package org.example.config;

public class DeployConfig {
    public static final boolean LOCAL_WINDOWS = true;

    private static final String WINDOWS_PROJECT_ROOT = "D:/Github/java_doc_searcher_2026/java_doc_searcher";
    private static final String LINUX_PROJECT_ROOT = "/opt/java_doc_searcher_2026/java_doc_searcher";

    private DeployConfig() {
    }

    public static String getProjectRoot() {
        return LOCAL_WINDOWS ? WINDOWS_PROJECT_ROOT : LINUX_PROJECT_ROOT;
    }

    public static String getJdkApiPath() {
        return getProjectRoot() + "/jdk-21.0.12_doc-all/docs/api";
    }

    public static String getIndexPath() {
        return getProjectRoot() + "/doc_search_index/";
    }

    public static String getStopWordPath() {
        return getIndexPath() + "stop_word.txt";
    }
}

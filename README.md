# Java Doc Searcher

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/java-8-orange">
  <img alt="Spring Boot" src="https://img.shields.io/badge/spring--boot-2.7.x-brightgreen">
  <img alt="Maven" src="https://img.shields.io/badge/maven-3.9-c71a36">
  <img alt="ansj_seg" src="https://img.shields.io/badge/ansj__seg-5.1.6-blue">
  <img alt="JDK Docs" src="https://img.shields.io/badge/docs-JDK%2021%20API-lightgrey">
  <img alt="Docker" src="https://img.shields.io/badge/deploy-docker%20%7C%20render-2496ed">
</p>

Java Doc Searcher 是一个针对 JDK 21 API 文档的站内搜索引擎，基于 Spring Boot 2.7 实现。项目会解析仓库中附带的 JDK 21 离线文档（HTML），使用 ansj_seg 分词构建正排索引和倒排索引并保存到本地文件，然后通过 Web 页面和 `/search` 接口提供搜索，搜索结果链接到 Oracle 官方在线文档。项目目前实现了文档解析、索引构建与加载、按权重排序的搜索、摘要生成和 Docker / Render 部署配置。

## 功能特性

- **文档解析**：遍历 `jdk-21.0.12_doc-all/docs/api` 下的 `.html` 文件，提取标题、正文，并生成对应的 `https://docs.oracle.com/en/java/javase/21/docs/api/` 在线地址。
- **索引构建**：使用 ansj_seg 对标题和正文分词，构建正排索引和倒排索引，权重为「标题出现次数 × 10 + 正文出现次数」，支持停用词过滤。
- **索引持久化**：索引以 JSON 形式保存到 `doc_search_index/forward.txt` 和 `doc_search_index/index.txt`，服务首次搜索时加载。
- **搜索接口**：`GET /search?query=` 返回按权重降序排列的结果（标题、URL、摘要），空查询返回 400。
- **摘要生成**：以关键词首次出现位置为中心截取约 160 字符的摘要，并对 HTML 转义。
- **健康检查**：`GET /health` 返回 `OK`。
- **前端页面**：`src/main/resources/static/index.html` 提供搜索页面。

## 项目结构

```text
.
├── src/main/java/org/example
│   ├── JavaDocSearcherApplication.java   # Spring Boot 启动类
│   ├── controller/
│   │   └── DocSearcherController.java    # /search、/health 接口
│   └── searcher/
│       ├── Config.java                   # 文档、索引、停用词路径配置
│       ├── Parser.java                   # 解析 HTML 文档并构建索引（main 入口）
│       ├── Index.java                    # 正排/倒排索引的构建、保存、加载
│       ├── DocSearcher.java              # 查询、排序、摘要生成
│       └── DocInfo / Weight / Result     # 数据结构
├── src/main/resources
│   ├── application.properties            # server.port=${PORT:8080}
│   └── static/                           # 搜索页面
├── src/test/java/                        # 分词、解析、合并等测试代码（main 方法）
├── jdk-21.0.12_doc-all/                  # JDK 21 离线 API 文档
├── doc_search_index/stop_word.txt        # 停用词；构建后的索引文件也保存在此目录
├── Dockerfile
└── render.yaml
```

## 快速开始

### 环境要求

- JDK 8（`pom.xml` 中 `java.version` 为 1.8）
- Maven 3.x

### 路径配置

`Config.java` 默认使用作者本机的 Windows 项目路径。在其他机器上运行时，请设置环境变量 `APP_ONLINE=true`，并在项目根目录下执行命令，此时会以当前工作目录作为项目根目录。

### 编译

```bash
mvn -DskipTests dependency:copy-dependencies package
```

### 构建索引

```bash
APP_ONLINE=true java -cp "target/classes:target/dependency/*" org.example.searcher.Parser
```

Windows PowerShell：

```powershell
$env:APP_ONLINE="true"
java -cp "target/classes;target/dependency/*" org.example.searcher.Parser
```

### 启动服务

```bash
APP_ONLINE=true java -jar target/java_doc_searcher-1.0-SNAPSHOT.jar
```

启动后访问 http://localhost:8080 ，端口可通过环境变量 `PORT` 修改。

### Docker 部署

```bash
docker build -t java-doc-searcher .
docker run -p 8080:8080 java-doc-searcher
```

Dockerfile 会在构建阶段编译项目并生成索引，运行阶段只包含 jar 和索引文件（JVM 最大堆 384 MB）。`render.yaml` 已配置使用该 Dockerfile 部署到 Render，并以 `/health` 作为健康检查路径。

## 当前状态

项目已完成从文档解析、索引构建到搜索服务的完整流程。后续可继续完善：

- 对查询词进行分词，支持多关键词搜索和结果合并（目前去掉空白后整体作为一个词查询）
- 把本机项目路径改为通过配置文件或环境变量指定
- 将 `src/test/java` 中的测试改为 JUnit 测试
- 考虑不在仓库中保存约 300 MB 的 JDK 离线文档，改为构建时下载

## 数据和敏感信息

构建产物 `target/`、生成的索引文件（`doc_search_index/` 中除 `stop_word.txt` 外的文件）、日志和 IDE 配置已通过 `.gitignore` 排除，不应提交到仓库。

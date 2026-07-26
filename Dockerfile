FROM maven:3.9-eclipse-temurin-8 AS build

WORKDIR /app
ENV APP_ONLINE=true

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY . .
RUN mvn -q -DskipTests dependency:copy-dependencies package
RUN java -cp "target/classes:target/dependency/*" org.example.searcher.Parser

FROM eclipse-temurin:8-jre

WORKDIR /app
ENV APP_ONLINE=true

COPY --from=build /app/target/java_doc_searcher-1.0-SNAPSHOT.jar target/java_doc_searcher-1.0-SNAPSHOT.jar
COPY --from=build /app/doc_search_index doc_search_index

EXPOSE 8080

CMD ["java", "-jar", "target/java_doc_searcher-1.0-SNAPSHOT.jar"]

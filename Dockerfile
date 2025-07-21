# 使用官方的Java基础镜像
FROM openjdk:8-jdk-alpine

WORKDIR /app

# 确保路径匹配构建输出的JAR
COPY target/xd11cc-single-1.0-SNAPSHOT.jar xd11cc-single.jar

# 暴露应用端口
EXPOSE 10001

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/xd11cc-single.jar"]
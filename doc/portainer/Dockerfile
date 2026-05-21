# portainer 专用
FROM eclipse-temurin:8-jre-focal
MAINTAINER xd11cc

WORKDIR /app

# 时区配置
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 动态参数注入
ENV PARAMS="--spring.profiles.active=prod"

# 确保路径匹配构建输出的JAR
COPY xd11cc-single-*.jar xd11cc-single.jar

# 暴露应用端口
EXPOSE 10001

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/xd11cc-single.jar $PARAMS"]

# 第一阶段：Maven 编译打包
FROM maven:3.8-openjdk-11 AS builder
WORKDIR /app
# 先复制 pom.xml 利用 Docker 缓存层加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B
# 复制源码并编译
COPY src ./src
RUN mvn clean package -DskipTests -B

# 第二阶段：运行 JRE（精简镜像）
FROM openjdk:11-jre-slim
WORKDIR /app
# 从构建阶段复制 jar 包
COPY --from=builder /app/target/*.jar wx-oauth.jar
# 对外暴露端口
EXPOSE 8080
# 启动命令
ENTRYPOINT ["java", "-jar", "wx-oauth.jar"]

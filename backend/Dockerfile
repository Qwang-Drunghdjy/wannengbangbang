# ============ 构建阶段 ============
# 国内构建若拉取缓慢，可将基础镜像替换为腾讯云内网加速地址，如：
# mirror.ccs.tencentyun.com/library/maven:3.9-eclipse-temurin-17
# FROM maven:3.9-eclipse-temurin-17 AS build
FROM mirror.ccs.tencentyun.com/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 阿里云 Maven 镜像加速（Q4：如无需加速可删除此段）
RUN mkdir -p /root/.m2 \
    && printf '%s\n' \
        '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">' \
        '  <mirrors>' \
        '    <mirror><id>aliyun</id><mirrorOf>central</mirrorOf>' \
        '      <url>https://maven.aliyun.com/repository/public</url></mirror>' \
        '  </mirrors>' \
        '</settings>' > /root/.m2/settings.xml

# 先只拷贝 pom.xml 下载依赖，最大化层缓存
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline || true

# 拷贝源码并打包（跳过测试；测试在本地/CI 执行）
COPY src ./src
RUN mvn -B clean package -DskipTests

# ============ 运行阶段 ============
# FROM eclipse-temurin:17-jre-alpine AS runtime
FROM mirror.ccs.tencentyun.com/library/eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# 时区：东八区
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 非 root 运行（安全加固）
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar /app/app.jar

# 微信云托管：容器端口 8080；健康检查路径 /health（Q3a）
# 环境变量（在云托管控制台配置）：
#   MYSQL_ADDRESS / MYSQL_USERNAME / MYSQL_PASSWORD / JWT_SECRET
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m -Duser.timezone=Asia/Shanghai"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

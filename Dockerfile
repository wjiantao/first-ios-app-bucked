# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# 构建阶段：Maven + JDK 17
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -q clean package -DskipTests

# ---------------------------------------------------------------------------
# 运行阶段：JRE 17
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# 与本地开发/数据库保持一致（Asia/Shanghai）
RUN apt-get update \
    && apt-get install -y --no-install-recommends tzdata \
    && rm -rf /var/lib/apt/lists/*
ENV TZ=Asia/Shanghai

# 上传目录；Render 免费 Web 服务的文件系统是临时的，重启/重新部署会清空，
# 生产环境如需持久化上传文件，应改用对象存储或挂载持久盘。
RUN mkdir -p /app/uploads

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "/app/app.jar"]

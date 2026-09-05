FROM mysql:8.0

ENV TZ=Asia/Shanghai

# 统一数据库时区，使 SQL 里的 NOW() 与 Java 端 LocalDateTime.now() 保持一致
COPY docker/mysql/my.cnf /etc/mysql/conf.d/zz-shiguang.cnf

# 首次初始化（数据目录为空）时自动建库建表并写入种子数据
COPY sql/init.sql /docker-entrypoint-initdb.d/10-init.sql

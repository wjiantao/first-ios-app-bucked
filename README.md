# shiguang-server

「拾光」个人内容 App（firstIosApp）的服务端 —— 当前为**邮箱注册 + 账号密码登录**模块。

## 技术栈

- Java 17 + Spring Boot 2.7.3
- MyBatis（SQL 全部收敛在 `resources/mapper/*.xml`）
- MySQL 8（本地库名 `shiguang`）
- Redis（邮箱验证码存储，TTL 自动过期）
- SMTP 邮件发送（QQ/163/Gmail 等均可，未配置时验证码仅开发模式回显）
- Lombok / JWT（jjwt 0.9.1）
- 密码摘要：PBKDF2WithHmacSHA256（随机盐 + 120000 次迭代）

代码分层：`controller -> service -> mapper -> MySQL`，统一返回
`{code, msg, data}`（code=1 表示成功）。

## 快速启动

前置条件：本机 MySQL 与 Redis 已启动；MySQL root 密码为 123456（可用环境变量覆盖）。

```bash
# 1. 建库建表（可重复执行）
mysql -uroot -p123456 < sql/schema.sql

# 2. 升级已有数据库，创建关注关系并放宽关注通知的作品字段（可重复执行）
mysql -uroot -p123456 < sql/migrations/20260914_add_user_follows.sql

# 3. 升级已有数据库，创建作品评论及评论通知字段
mysql -uroot -p123456 < sql/migrations/20260915_add_comments.sql

# 4. 写入种子数据（可重复执行）
mysql -uroot -p123456 shiguang < sql/seed.sql

# 5. 启动
mvn spring-boot:run
```

服务默认监听 `http://localhost:8080`（8080 被本机其他 Java 服务占用、8081 被 Node 占用）。
接口文档（Swagger UI）：`http://localhost:8082/swagger-ui/index.html`，
`/api/users/**` 这类需登录的接口可在页面右上角 Authorize 里填入 `Bearer <token>` 后直接调试。

MySQL 连接与 JWT 密钥可通过环境变量覆盖：

```bash
SHIGUANG_DB_HOST=localhost SHIGUANG_DB_PORT=3306 \
SHIGUANG_DB_USERNAME=root SHIGUANG_DB_PASSWORD=123456 \
SHIGUANG_JWT_SECRET=your-secret mvn spring-boot:run
```

Redis 连接默认 `localhost:6379`（无密码），可用 `SHIGUANG_REDIS_HOST` /
`SHIGUANG_REDIS_PORT` / `SHIGUANG_REDIS_PASSWORD` 覆盖。

### 发送邮件（验证码）

配置 SMTP 后验证码才会真实发送到邮箱；未配置时仅开发模式会在日志与响应
`devCode` 中回显，生产模式会直接报错。本地通过 `mvn spring-boot:run` 启动时会
自动激活 `dev` profile，因此默认开启开发模式；打包后的生产 jar 默认关闭开发模式，
可通过 `SHIGUANG_DEV_MODE=true` 显式开启、`SHIGUANG_DEV_MODE=false` 显式关闭。

QQ 邮箱示例（需要先在 QQ 邮箱开启 SMTP 并获取授权码）：

```bash
SHIGUANG_MAIL_HOST=smtp.qq.com \
SHIGUANG_MAIL_PORT=465 \
SHIGUANG_MAIL_USERNAME=you@qq.com \
SHIGUANG_MAIL_PASSWORD=授权码 \
mvn spring-boot:run
```

常用环境变量：

- `SHIGUANG_MAIL_HOST` / `SHIGUANG_MAIL_PORT`：SMTP 服务器与端口
- `SHIGUANG_MAIL_USERNAME` / `SHIGUANG_MAIL_PASSWORD`：登录账号与密码/授权码
- `SHIGUANG_MAIL_FROM`：发件人地址，默认等于登录账号（QQ/163 必须等于账号）
- `SHIGUANG_MAIL_SMTP_SSL`：SSL（默认 true，对应 465 端口）
- `SHIGUANG_MAIL_SMTP_STARTTLS`：使用 587 端口时设为 true，并将 SSL 设为 false

## 认证接口

## 地图工具接口

地图工具统一挂载在 `/api/map`，返回格式仍为 `{code, msg, data}`：

- `POST /api/map/elevation`：批量高程，兼容 OpenTopoData 风格 provider。
- `POST /api/map/works`：按 Cesium 可视范围查询作品点位。

provider 地址通过环境变量配置，不把第三方密钥写入客户端：

```bash
SHIGUANG_MAP_ELEVATION_URL=https://elevation.example/v1 \
SHIGUANG_MAP_LOCATION_SEARCH_URL=https://geocoder.example/search \
SHIGUANG_MAP_USER_AGENT='ShiguangMap/1.0 (+https://example.com)' \
SHIGUANG_MAP_PROVIDER_TIMEOUT_MS=8000 \
mvn spring-boot:run
```

高程 provider 通过环境变量配置，未配置时地图仍可浏览，但地形信息会提示服务不可用。

### 邮箱注册（两步）

1. 发送验证码：`POST /api/auth/email-code`，body：`{"email":"demo@shiguang.app"}`
2. 校验验证码并创建待激活账号：`POST /api/auth/register/email-verify`，body：`{"email":"...","code":"..."}`
3. 设置密码并激活账号：`POST /api/auth/register/set-password`，body：`{"email":"...","password":"至少6位","nickname":"可选"}`

### 登录

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 默认登录：邮箱 + 密码，body：`{"email":"...","password":"..."}` |
| POST | `/api/auth/login/{channel}` | 第三方登录，channel=wechat/douyin/apple（开发模式 mock） |
| GET | `/api/users/me` | 当前用户信息（需 `Authorization: Bearer <token>`） |

开发模式（本地 `mvn spring-boot:run` 默认开启；生产 jar 默认关闭，可用
`SHIGUANG_DEV_MODE` 覆盖）说明：

- 发送验证码响应带 `devCode`，日志同时打印验证码；
- 固定验证码 `123456` 可直接通过邮箱验证；
- 第三方登录使用固定 mock uid（`mock-wechat` 等），命中种子用户 u-1002~u-1004。

登录成功返回：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "token": "...",
    "user": { "id": "u-xxx", "nickname": "demo", "avatarUrl": null, "bio": "", "tagline": "" }
  }
}
```

## 数据库表

- `users`：邮箱（唯一）、PBKDF2 密码摘要、status（pending=已验证待设置密码，active=可登录）
- `auth_accounts`：第三方登录绑定关系

邮箱验证码不落库，以 `email:code:{email}` 为键存 Redis（TTL 5 分钟，
同邮箱重发覆盖旧码，验证通过后立即删除）。

## 目录结构

```text
src/main/java/com/shiguang/
├── config/          Web MVC 配置（JWT 拦截器/CORS）
├── controller/      接口层：AuthController、UserController
├── service/         业务层：AuthService（邮箱注册/登录/当前用户）
├── mapper/          MyBatis Mapper 接口
├── entity/          users / auth_accounts 实体
├── dto/             认证请求体
├── vo/              认证返回体
├── store/           轻量存储（邮箱验证码 Redis 读写）
├── interceptor/     JWT 鉴权拦截器
├── result/          统一 Result
├── exception/       业务异常
├── handler/         全局异常处理
└── utils/           JWT、密码摘要工具
```

## 部署到 Render

仓库已内置 Render Blueprint，可在 Render 上一次性创建后端所需的全部资源：

```text
render.yaml             Blueprint 配置（Web 服务 + MySQL + Redis）
Dockerfile              Spring Boot 应用镜像（Maven 构建 + JRE 17 运行）
docker/mysql/Dockerfile MySQL 8 镜像，首次启动自动执行 sql/init.sql
sql/init.sql            完整可重复执行的建库建表 + 种子数据脚本
```

### 一键部署步骤

1. 把本仓库推送到 GitHub。
2. 打开 Render，点击 **New -> Blueprint**，选择本仓库。
3. 按页面提示填写敏感信息：
   - `MYSQL_PASSWORD`（业务库密码，与后端共用）
   - `MYSQL_ROOT_PASSWORD`（MySQL root 密码）
   - `SHIGUANG_MAIL_*`（SMTP，可选；不填则生产模式下发验证码会报错）
   - `SHIGUANG_JPUSH_*`（极光推送，可选）
4. Render 会自动创建并部署三个资源：
   - `shiguang-server`：Spring Boot 后端（免费 Web 服务）
   - `shiguang-mysql`：MySQL 8（私有服务，含 10 GB 持久盘）
   - `shiguang-redis`：Redis / Key Value（免费档）

部署完成后，后端地址形如 `https://shiguang-server.onrender.com`，Swagger 文档在
`https://shiguang-server.onrender.com/swagger-ui/index.html`，健康检查为
`https://shiguang-server.onrender.com/health`。

### 费用与免费档限制

- Web 服务与 Redis 使用免费档，MySQL 私有服务无免费档，需要付费（约 $7/月
  计算实例 + $0.25/GB/月的持久盘，10 GB 约 $2.5/月）。
- 免费 Web 服务空闲 15 分钟会休眠，且**不能**对外发送 465/587 端口的 SMTP 邮件；
  如需真实发送邮箱验证码，请把 `shiguang-server` 升级为付费档，或改用 HTTP 邮件服务。
- 免费 Web 服务文件系统是临时的，`/app/uploads` 里的上传图片在重启/重新部署后会丢失；
  生产环境应改为对象存储或挂载持久盘。

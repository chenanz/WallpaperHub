# WallpaperHub - 二次元女性角色壁纸收集器

> 适配 Redmi K80 Ultra (3200×1440 / 20:9)

## 📁 项目结构

```
.
├── wallpaper_crawler/          # 后端爬虫服务
│   ├── docker-compose.yml      # 一键部署编排
│   ├── Dockerfile              # 爬虫 + API 镜像
│   ├── api_server.py           # FastAPI REST API
│   ├── crawl_scheduler.py      # Celery Beat 定时调度
│   ├── init_db.sql             # 数据库初始化
│   ├── .env.example            # 环境变量示例
│   ├── requirements.txt        # Python 依赖
│   └── wallpaper_crawler/
│       ├── settings.py         # Scrapy 配置
│       ├── items.py            # 数据项定义
│       ├── pipelines.py        # MD5去重/男性过滤/下载/入库
│       └── spiders/
│           └── anime_girls_spider.py  # 4个爬虫(Safebooru/E926/零度/Pixiv)
│
└── WallpaperHub/               # Android 客户端
    ├── app/build.gradle.kts    # 依赖配置
    └── app/src/main/java/com/wallpaperhub/app/
        ├── MainActivity.kt     # 入口 Activity
        ├── WallpaperHubApp.kt  # Application + Coil 配置
        ├── data/
        │   ├── api/            # Retrofit API + OkHttp Client
        │   ├── local/          # Room 数据库 + DAO
        │   ├── repository/     # 仓库层（远程+本地协调）
        │   └── worker/         # WorkManager 后台同步
        ├── ui/
        │   ├── screens/        # 6个页面(Home/Category/Favorites/Settings/Preview/R18Gallery)
        │   ├── navigation/     # 导航 + 隐藏路由
        │   ├── theme/          # 暗色科技风主题
        │   └── components/     # 可复用组件
        └── util/
            ├── SecretUtils.kt      # Android KeyStore 加密
            ├── WallpaperSetter.kt  # 壁纸设置(20:9适配)
            └── ScreenshotBlocker.kt # 截屏防护
```

## 🚀 服务端部署

### 前置条件
- 阿里云轻量应用服务器 (2核4G, Ubuntu 22.04)
- Docker + Docker Compose

### 部署步骤

```bash
# 1. 克隆项目
git clone https://github.com/chenanz/wallpaper_crawler.git
cd wallpaper_crawler

# 2. 配置环境变量
cp .env.example .env
nano .env  # 修改所有密码和密钥

# 3. 启动全部服务
docker-compose up -d

# 4. 检查服务状态
docker-compose ps
curl http://localhost:8000/api/health

# 5. 首次全量爬取
docker-compose exec celery-worker scrapy crawl safebooru -a mode=full
docker-compose exec celery-worker scrapy crawl e926 -a mode=full
docker-compose exec celery-worker scrapy crawl zerodegree -a mode=full
# Pixiv 需要配置 PIXIV_PHPSESSID
# docker-compose exec celery-worker scrapy crawl pixiv -a mode=full

# 6. 验证数据
curl http://localhost:8000/api/stats
```

### 服务端口

| 服务 | 端口 | 用途 |
|------|------|------|
| API Server | 8000 | REST API |
| PostgreSQL | 5432 | 数据库 |
| Redis | 6379 | 队列 |
| MinIO API | 9000 | 对象存储 |
| MinIO Console | 9001 | 管理界面 |

## 📱 Android 客户端

### 构建步骤

```bash
# 1. 使用 Android Studio 打开 WallpaperHub 目录
# 2. 配置 API 地址 (app/build.gradle.kts 中的 API_BASE_URL)
# 3. 生成 release keystore
keytool -genkey -v -keystore wallpaperhub.jks -keyalg RSA -keysize 2048 -validity 10000 -alias wallpaperhub

# 4. 在 app/build.gradle.kts 中配置签名
# signingConfigs {
#     create("release") {
#         storeFile = file("wallpaperhub.jks")
#         storePassword = "YOUR_KEYSTORE_PASSWORD"
#         keyAlias = "wallpaperhub"
#         keyPassword = "YOUR_KEY_PASSWORD"
#     }
# }

# 5. 构建 release APK
./gradlew assembleRelease

# 6. 安装到手机
adb install app/build/outputs/apk/release/app-release.apk

# 7. 授予存储权限
adb shell pm grant com.wallpaperhub.app android.permission.SET_WALLPAPER
```

### 首次配置

1. 打开 App → 设置 → 服务器设置 → 输入你的 API 服务器地址
2. 设置 → 密钥设置 → 输入 `.env` 中的 `API_SECRET_KEY`
3. 设置 → 手动同步 → 等待壁纸数据拉取完成

### 进入隐藏模块

1. 进入「设置」页面
2. 长按标题 "WallpaperHub" 3秒
3. 输入暗号（默认: `5201314`，可在密钥设置中修改）

## 🔒 安全隔离机制

| 层级 | 机制 | 说明 |
|------|------|------|
| 数据层 | SQL `WHERE is_r18 = FALSE/TRUE` | 普通/R18查询完全隔离 |
| API层 | `/api/r18/*` 独立路径 + `X-Secret-Key` Header | R18接口需密钥认证 |
| 缓存层 | Coil 双缓存目录 `cache/normal` / `cache/r18` | R18图片不参与预加载 |
| 路由层 | Base64编码路由 `"UjE4R2FsbGVyeQ=="` | 反编译看不出R18入口 |
| 入口层 | 长按3秒 + 密码验证 | ProGuard混淆入口类名 |
| 防护层 | `FLAG_SECURE` 截屏阻止 | R18页面不可截图/录屏 |
| 存储层 | Android KeyStore 加密 | 密码/密钥加密存储 |
| 对象存储 | MinIO 双Bucket | `wallpapers-normal` / `wallpapers-r18` |

## 📊 监控

```bash
# Prometheus 指标
curl http://localhost:8000/metrics

# 爬取状态
curl http://localhost:8000/api/crawl-status

# 推荐搭配 Grafana 看板
```

## 🔧 常见问题

### Q: 爬虫抓不到图片？
A: 检查网络连接。Safebooru 和 E926 国内直连可用，Danbooru/Yande.re 需要代理或部署在海外服务器。

### Q: CI 构建 Gradle 报错？
A: 使用 `npm install --legacy-peer-deps`（如果是 Capacitor 项目）或 `./gradlew build --no-daemon`。

### Q: Android Studio 找不到 JDK？
A: React 项目不需要 Android Studio，直接用 `./gradlew` 命令行构建。

### Q: 壁纸设置后在 MIUI 上被裁剪？
A: WallpaperSetter 已处理 20:9 比例缩放，确保图片先裁剪到目标比例再传给系统。

## 📝 环境变量说明

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DB_PASSWORD` | changeme_db_pass | PostgreSQL 密码 |
| `REDIS_PASSWORD` | changeme_redis_pass | Redis 密码 |
| `MINIO_ACCESS_KEY` | minioadmin | MinIO 访问密钥 |
| `MINIO_SECRET_KEY` | changeme_minio_pass | MinIO 密钥 |
| `API_SECRET_KEY` | changeme_api_secret | R18 接口验证密钥 |
| `PIXIV_PHPSESSID` | (空) | Pixiv 登录 Session |
| `BAIDU_OCR_API_KEY` | (空) | 百度OCR API Key（可选） |

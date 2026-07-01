pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 腾讯镜像 - 加速国内下载
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/maven-public/") }
    }
}

rootProject.name = "WallpaperHub"
include(":app")

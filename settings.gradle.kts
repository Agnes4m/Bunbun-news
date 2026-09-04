pluginManagement {
    repositories {
        // 官方源优先（CI 环境对阿里云镜像可能超时）
        gradlePluginPortal()
        google()
        mavenCentral()
        // 阿里云镜像作为国内加速
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        google()
        mavenCentral()
    }
}

rootProject.name = "BunbunNews"
include(":app")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://artifactory.img.ly/artifactory/imgly")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven(url = "https://artifactory.img.ly/artifactory/imgly")
    }
}

rootProject.name = "Simple-Gallery"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")



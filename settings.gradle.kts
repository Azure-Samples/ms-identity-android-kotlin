pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
/*dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        }
 }*/

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // or FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
       // jcenter() // Warning: this repository is going to shut down soon
        /*maven {
            name = "vsts-maven-adal-android"
            url =
                uri("https://identitydivision.pkgs.visualstudio.com/_packaging/AndroidADAL/maven/v1")

            credentials {
                username = System.getenv()["vstsUsername"]
                password = System.getenv()["vstsMavenAccessToken"]
            }
        }*/
    }
}
rootProject.name = "ms-identity-kotlin-sample"
include(":app")

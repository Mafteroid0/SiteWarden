pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}


rootProject.name = "SiteWarden"
include("monitor-service")
include("common-events")

include("probe-worker")
include("incident-service")
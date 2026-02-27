pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "OpenPDF"

include(":app")

include(":core:core-common")
include(":core:core-model")
include(":core:core-database")
include(":core:core-datastore")
include(":core:core-ui")
include(":core:core-security")

include(":mupdf:mupdf-fitz")
include(":mupdf:mupdf-wrapper")

include(":feature:feature-viewer")
include(":feature:feature-annotations")
include(":feature:feature-forms")
include(":feature:feature-signatures")
include(":feature:feature-search")
include(":feature:feature-settings")
include(":feature:feature-filemanager")

//import java.util.Properties
//import java.io.FileInputStream
//import java.io.FileOutputStream
//
//val versionPropsFile = file("$rootDir/version.properties")
//val versionProps = Properties()
//
//if (versionPropsFile.exists()) {
//    versionProps.load(FileInputStream(versionPropsFile))
//} else {
//    versionProps["VERSION_MAJOR"] = "1"
//    versionProps["VERSION_MINOR"] = "0"
//    versionProps["VERSION_PATCH"] = "0"
//    versionProps["VERSION_BUILD"] = "1"
//}
//
//val versionMajor = versionProps["VERSION_MAJOR"].toString().toInt()
//val versionMinor = versionProps["VERSION_MINOR"].toString().toInt()
//val versionPatch = versionProps["VERSION_PATCH"].toString().toInt()
//val versionBuild = versionProps["VERSION_BUILD"].toString().toInt()
//
//extra["versionMajor"] = versionMajor
//extra["versionMinor"] = versionMinor
//extra["versionPatch"] = versionPatch
//extra["versionBuild"] = versionBuild
//
//tasks.register("incrementVersionBuild") {
//    doLast {
//        versionProps["VERSION_BUILD"] = (versionBuild + 1).toString()
//        versionProps.store(FileOutputStream(versionPropsFile), null)
//        println("Build number incremented to ${versionProps["VERSION_BUILD"]}")
//    }
//}
//
//tasks.register("incrementVersionPatch") {
//    doLast {
//        versionProps["VERSION_PATCH"] = (versionPatch + 1).toString()
//        versionProps["VERSION_BUILD"] = (versionBuild + 1).toString()
//        versionProps.store(FileOutputStream(versionPropsFile), null)
//        println("Version incremented to ${versionProps["VERSION_MAJOR"]}.${versionProps["VERSION_MINOR"]}.${versionProps["VERSION_PATCH"]}")
//    }
//}
//
//tasks.register("incrementVersionMinor") {
//    doLast {
//        versionProps["VERSION_MINOR"] = (versionMinor + 1).toString()
//        versionProps["VERSION_PATCH"] = "0"
//        versionProps["VERSION_BUILD"] = (versionBuild + 1).toString()
//        versionProps.store(FileOutputStream(versionPropsFile), null)
//        println("Version incremented to ${versionProps["VERSION_MAJOR"]}.${versionProps["VERSION_MINOR"]}.${versionProps["VERSION_PATCH"]}")
//    }
//}
//
//tasks.register("incrementVersionMajor") {
//    doLast {
//        versionProps["VERSION_MAJOR"] = (versionMajor + 1).toString()
//        versionProps["VERSION_MINOR"] = "0"
//        versionProps["VERSION_PATCH"] = "0"
//        versionProps["VERSION_BUILD"] = (versionBuild + 1).toString()
//        versionProps.store(FileOutputStream(versionPropsFile), null)
//        println("Version incremented to ${versionProps["VERSION_MAJOR"]}.${versionProps["VERSION_MINOR"]}.${versionProps["VERSION_PATCH"]}")
//    }
//}
//
//tasks.register("incrementVersionBuildAndBundleRelease") {
//    dependsOn("incrementVersionBuild", ":app:bundleRelease")
//
//    doLast {
//        println("Build number incremented and AAB file created successfully.")
//    }
//}

import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

val versionPropsFile = file("$rootDir/version.properties")
val versionProps = Properties()

if (versionPropsFile.exists()) {
    versionProps.load(FileInputStream(versionPropsFile))
} else {
    versionProps["VERSION_MAJOR"] = "1"
    versionProps["VERSION_MINOR"] = "0"
    versionProps["VERSION_PATCH"] = "0"
    versionProps["VERSION_BUILD"] = "1"
}

val versionMajor = versionProps["VERSION_MAJOR"].toString().toInt()
val versionMinor = versionProps["VERSION_MINOR"].toString().toInt()
val versionPatch = versionProps["VERSION_PATCH"].toString().toInt()
val versionBuild = versionProps["VERSION_BUILD"].toString().toInt()

// Store version numbers (all builds now use timestamps)
extra["versionMajor"] = versionMajor
extra["versionMinor"] = versionMinor
extra["versionPatch"] = versionPatch
extra["versionBuild"] = versionBuild

// Function to generate a timestamp (seconds since epoch)
fun getTimestamp(): String {
    return (System.currentTimeMillis() / 1000).toString()
}

// Sequential build number increment (for dev/qa)
tasks.register("incrementVersionBuild") {
    doLast {
        versionProps["VERSION_BUILD"] = (versionBuild + 1).toString()
        versionProps.store(FileOutputStream(versionPropsFile), null)
        println("Build number incremented to: ${versionProps["VERSION_BUILD"]}")
    }
}

// Timestamp build number (for mainApp)
tasks.register("setTimestampBuild") {
    doLast {
        versionProps["VERSION_BUILD"] = getTimestamp()
        versionProps.store(FileOutputStream(versionPropsFile), null)
        println("Build number set to timestamp: ${versionProps["VERSION_BUILD"]}")
    }
}


tasks.register("incrementVersionPatch") {
    doLast {
        versionProps["VERSION_PATCH"] = (versionPatch + 1).toString()
        versionProps["VERSION_BUILD"] = (versionBuild + 1).toString() // Sequential increment for Google Play
        versionProps.store(FileOutputStream(versionPropsFile), null)
        println("Version incremented to ${versionProps["VERSION_MAJOR"]}.${versionProps["VERSION_MINOR"]}.${versionProps["VERSION_PATCH"]} (Build: ${versionProps["VERSION_BUILD"]})")
    }
}

tasks.register("incrementVersionMinor") {
    doLast {
        versionProps["VERSION_MINOR"] = (versionMinor + 1).toString()
        versionProps["VERSION_PATCH"] = "0"
        versionProps["VERSION_BUILD"] = (versionBuild + 1).toString() // Sequential increment for Google Play
        versionProps.store(FileOutputStream(versionPropsFile), null)
        println("Version incremented to ${versionProps["VERSION_MAJOR"]}.${versionProps["VERSION_MINOR"]}.${versionProps["VERSION_PATCH"]} (Build: ${versionProps["VERSION_BUILD"]})")
    }
}

tasks.register("incrementVersionMajor") {
    doLast {
        versionProps["VERSION_MAJOR"] = (versionMajor + 1).toString()
        versionProps["VERSION_MINOR"] = "0"
        versionProps["VERSION_PATCH"] = "0"
        versionProps["VERSION_BUILD"] = (versionBuild + 1).toString() // Sequential increment for Google Play
        versionProps.store(FileOutputStream(versionPropsFile), null)
        println("Version incremented to ${versionProps["VERSION_MAJOR"]}.${versionProps["VERSION_MINOR"]}.${versionProps["VERSION_PATCH"]} (Build: ${versionProps["VERSION_BUILD"]})")
    }
}

tasks.register("incrementVersionBuildAndBundleRelease") {
    // This task will now also use the timestamp for the build number
    dependsOn("incrementVersionBuild", ":app:bundleRelease")

    doLast {
        println("Build number incremented and AAB file created successfully.")
    }
}
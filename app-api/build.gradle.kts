
plugins {
}

dependencies {
    implementation(project(Projects.model))
    implementation(project(Projects.core))

    implementation(Libraries.spring_boot_starter_actuator)
    implementation(Libraries.spring_boot_starter_web)
    implementation(Libraries.spring_boot_starter_webflux)
}

tasks.jar {
    enabled = false
}

tasks.bootJar {
    enabled = true
    mainClass.set("com.tistory.eclipse4j.internal.api.AppApiApplicationKt")
    archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
}

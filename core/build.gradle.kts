plugins {
    // id(BuildPlugins.flyway) version BuildPlugins.Versions.flyway
}

dependencies {
    implementation(project(Projects.model))
    api(Libraries.mariadb_java_client)
    api(Libraries.spring_boot_starter_cache)
    api(Libraries.spring_boot_starter_redis)
    api(Libraries.spring_aspects)
    api(Libraries.kotlin_allopen)
    api(Libraries.kotlin_noarg)
    api(Libraries.kotlinx_coroutines_core)
    api(Libraries.kotlinx_coroutines_reactive)
    api(Libraries.mariadb_java_client)
    api(Libraries.jackson_module_kotlin)
    api(Libraries.jackson_databind)
    api(Libraries.jackson_datatype_jdk8)
    api(Libraries.jackson_datatype_jsr310)
    api(Libraries.caffeine)
    api(Libraries.commons_pool2)
    api(Libraries.kotlin_logging)
    // JPA/Querydsl 설정
    api(Libraries.spring_boot_starter_data_jpa)
    api(Libraries.querydsl_jpa)
    kapt(Libraries.querydsl_apt)
    kapt(Libraries.jakarta_persistence_api)
    kapt(Libraries.jakarta_annotation_api)

    api(Libraries.hibernate_envers)
    api(Libraries.h2_database)

    // API
    api(Libraries.circuitbreaker_resilience4j)
    api(Libraries.spring_boot_starter_webflux)
}

// flyway {
//    url = "jdbc:mysql://localhost:3306/githubstudy"
//    user = "githubstudy"
//    password = "githubstudy"
//    locations = arrayOf("filesystem:${file("src/main/resources/db/migration").absolutePath}")
//    encoding = "UTF-8"
//    outOfOrder = true
//    validateOnMigrate = true
// }

plugins { java; id("org.springframework.boot"); id("io.spring.dependency-management") }
dependencies {
 implementation(project(":inventory-service:domain"))
 implementation(project(":inventory-service:application"))

 implementation("org.springframework.kafka:spring-kafka")
    implementation(project(":contracts"))
    implementation("io.grpc:grpc-netty-shaded")
    implementation("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-stub")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
 implementation("org.springframework.boot:spring-boot-starter-web")
 implementation("org.springframework.boot:spring-boot-starter-jdbc")
 implementation("org.springframework.boot:spring-boot-starter-validation")
 implementation("org.springframework.boot:spring-boot-starter-data-redis")
 runtimeOnly("org.postgresql:postgresql")
}
tasks.named<Jar>("jar") { enabled = false }

sourceSets { main { java { exclude("ru/web/store/inventory/domain/**", "ru/web/store/inventory/application/**") } } }

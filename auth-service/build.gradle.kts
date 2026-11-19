plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
 implementation(project(":auth-service:domain"))
 implementation(project(":auth-service:application"))

 implementation(project(":contracts"))
 implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.named<Jar>("jar") { enabled = false }

sourceSets { main { java { exclude("ru/web/store/auth/domain/**", "ru/web/store/auth/application/**") } } }

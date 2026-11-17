dependencies {
 implementation(project(":auth-service:domain"))
 implementation(project(":contracts"))
 implementation("org.springframework:spring-context")
 implementation("org.springframework:spring-tx")
 implementation("org.springframework.security:spring-security-crypto")
}

sourceSets { main { java { setSrcDirs(listOf("../src/main/java")); include("ru/web/store/auth/application/**") } } }

dependencies { implementation("org.springframework.security:spring-security-core") }

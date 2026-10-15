plugins {
	java
	id("org.springframework.boot") version "4.1.1" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
	group = "ru.web"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

// Настройки, которые автоматически применятся ко всем сервисам:
subprojects {
	apply(plugin = "java")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(21))
		}
	}

	dependencies {
		// Базовые вещи для тестов во всех сервисах
		"testImplementation"("org.springframework.boot:spring-boot-starter-test")
	}
}
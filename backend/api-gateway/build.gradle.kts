plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.hospital"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// Spring Cloud 2024.0.x es la rama estable compatible con Spring Boot 3.x
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.1")
    }
}

dependencies {
    // Spring Cloud Gateway (WebFlux — NO usar spring-boot-starter-web aquí)
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    // Validación JWT vía JWKS (ms-auth expone /.well-known/jwks.json)
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Actuator — healthcheck para K8s probes
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
	id("org.springframework.boot") version "2.5.1"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("com.heroku.sdk.heroku-gradle") version "2.0.0"
	kotlin("jvm") version "1.5.10"
	kotlin("plugin.spring") version "1.5.10"
	kotlin("plugin.serialization") version "1.5.10"
}

group = "com.credijusto.challenge"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_13


repositories {
	mavenCentral()
	maven {
		url = uri("https://jcenter.bintray.com/")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jsoup:jsoup:1.13.1")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
	implementation("khttp:khttp:1.0.0")
	implementation("me.paulschwarz:spring-dotenv:2.3.0")
	implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict","-Xopt-in=kotlin.RequiresOptIn")
		jvmTarget = "13"
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

tasks.withType<Test> {
	systemProperty("file.encoding", "UTF-8")
}

tasks.withType<Javadoc>{
	options.encoding = "UTF-8"
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register<Copy>("copyToLib") {
	into("$buildDir/libs")
	from(configurations.compileOnly)
}

// Heroku Deployment
tasks.register("stage") {
	dependsOn("copyToLib", "build", "clean")
}

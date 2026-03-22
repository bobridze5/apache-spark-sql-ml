plugins {
    id("java")
}

group = "com.alsaev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.apache.spark:spark-mllib_2.13:4.2.0-preview3")
    implementation("org.apache.spark:spark-core_2.13:4.2.0-preview3")
    implementation("org.apache.spark:spark-sql_2.13:4.2.0-preview3")
}

tasks.test {
    useJUnitPlatform()
}
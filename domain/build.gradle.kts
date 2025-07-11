plugins {
    customPlugin("java.library")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

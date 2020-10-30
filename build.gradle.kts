buildscript {
    repositories {
        jcenter()
        google()
        maven(url = "https://developer.huawei.com/repo/") // HUAWEI Maven repository
     }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0")
        classpath("com.google.gms:google-services:4.3.4")
        classpath("com.huawei.agconnect:agcp:1.4.1.300")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}")
    }
}

tasks.getByName<Wrapper>("wrapper") {
    gradleVersion = "6.1.1"
    distributionType = Wrapper.DistributionType.ALL
}

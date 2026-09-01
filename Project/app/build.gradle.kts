import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation.safe.args)
    alias(libs.plugins.room)
}

android {

    val keystorePath = providers.environmentVariable("RELEASE_KEYSTORE_PATH")
    val keystorePassword = providers.environmentVariable("RELEASE_KEYSTORE_PASSWORD")
    val releaseAlias = providers.environmentVariable("RELEASE_KEY_ALIAS")
    val releasePassword = providers.environmentVariable("RELEASE_KEY_PASSWORD")

    val signingFields = listOf(
        keystorePath,
        keystorePassword,
        releaseAlias,
        releasePassword
    )
    val hasCompleteSigningConfig = signingFields.all { it.isPresent }
    val hasAnySigningField = signingFields.any { it.isPresent }

    if (hasCompleteSigningConfig) {
        val keystoreFile = file(keystorePath.get())
        if (!keystoreFile.isFile) {
            throw GradleException("Release keystore file does not exist: ${keystoreFile.absolutePath}")
        }

        signingConfigs {
            create("release") {
                storeFile = keystoreFile
                storePassword = keystorePassword.get()
                keyAlias = releaseAlias.get()
                keyPassword = releasePassword.get()
            }
        }
    } else if (hasAnySigningField) {
        throw GradleException(
            "Release signing configuration is incomplete. Provide all four variables: " +
                "RELEASE_KEYSTORE_PATH, RELEASE_KEYSTORE_PASSWORD, RELEASE_KEY_ALIAS, " +
                "and RELEASE_KEY_PASSWORD."
        )
    }

    namespace = "com.juancarloslf.modu"
    compileSdk = 36

    flavorDimensions += "mode"

    productFlavors {
        create("demo") { dimension = "mode" }
        create("remote") {
            dimension = "mode"
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://scouts-embattled-naturist.ngrok-free.dev/\""
            )
        }
    }

    defaultConfig {
        applicationId = "com.juancarloslf.modu"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasCompleteSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    // Common
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Navigation
    implementation(libs.material)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // OkHttp
    implementation(libs.okhttp.logging)

    // Splash
    implementation(libs.androidx.core.splashscreen)

    // Coil
    implementation(libs.coil)

    // Flexbox
    implementation(libs.flexbox)

    // Paging
    implementation(libs.androidx.paging.runtime)

    // Shimmer
    implementation(libs.shimmer)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // MockK
    testImplementation(libs.mockk)

    // Coroutines
    testImplementation(libs.kotlinx.coroutines.test)

    // Demo catalog
    add("demoImplementation", libs.androidx.room.paging)
    add("androidTestDemoImplementation", libs.androidx.test.core)
}

room {
    schemaDirectory("$projectDir/schemas")
}

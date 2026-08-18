plugins {
    alias(libs.plugins.locationjoystick.android.application)
    alias(libs.plugins.locationjoystick.hilt)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.locationjoystick.app"
    buildFeatures {
        compose = true
        buildConfig = true
    }
    defaultConfig {
        // 独立包名：改造版应用 ID，与原作者（com.locationjoystick.app）互不干扰
        applicationId = "com.cnxiekun.mocklocation"
        testInstrumentationRunner = "com.locationjoystick.app.HiltTestRunner"
        ndk {
            // 只保留真机架构，去掉 x86/x86_64 模拟器架构以显著减小体积
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
        // 只保留中文字符串资源
        resourceConfigurations += listOf("zh")
    }
    signingConfigs {
        create("release") {
            storeFile = rootProject.file("cnxiekun-mock-location.keystore")
            storePassword = "xiekun123"
            keyAlias = "xiekun"
            keyPassword = "xiekun123"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:location"))
    implementation(project(":core:model"))
    implementation(project(":core:overlay"))
    implementation(project(":core:routing"))

    implementation(project(":feature:onboarding:api"))
    implementation(project(":feature:onboarding:impl"))
    implementation(project(":feature:map:api"))
    implementation(project(":feature:map:impl"))
    implementation(project(":feature:joystick:impl"))
    implementation(project(":feature:routes:api"))
    implementation(project(":feature:routes:impl"))
    implementation(project(":feature:favorites:api"))
    implementation(project(":feature:favorites:impl"))
    implementation(project(":feature:settings:api"))
    implementation(project(":feature:settings:impl"))
    implementation(project(":feature:widget:impl"))
    implementation(project(":feature:group:api"))
    implementation(project(":feature:group:impl"))

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.room.runtime)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

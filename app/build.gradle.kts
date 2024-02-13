plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}


android {

    signingConfigs {
        getByName("debug") {
            storeFile =
                file("..\\gradle\\debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }


    }


    namespace = "com.azuresamples.msalandroidkotlinapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.azuresamples.msalandroidkotlinapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        signingConfig = signingConfigs.getByName("debug")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }

    }
}

dependencies {
    implementation("io.opentelemetry:opentelemetry-api:1.18.0")
    implementation("io.opentelemetry:opentelemetry-context:1.18.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation ("androidx.appcompat:appcompat:1.1.0")
    implementation ("androidx.core:core-ktx:1.1.0")
    implementation ("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation ("com.google.android.material:material:1.0.0")
    testImplementation ("junit:junit:4.12")
    androidTestImplementation ("androidx.test:runner:1.2.0")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.2.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")

    implementation ("com.microsoft.identity.client:msal:5.+")
    {
        exclude(group = "io.opentelemetry")
        exclude (group="com.microsoft.device.display")
    }

    implementation("com.microsoft.graph:microsoft-graph:5.80.0")

    implementation("com.azure:azure-identity:1.10.0")
}
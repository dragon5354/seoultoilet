plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.seoultoilet"
    compileSdk = 33

    // 데이터 바인딩
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.seoultoilet"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // 구글 맵 관련 라이브러리
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    // 구글 맵 클러스터를 위한 라이브러리
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")

    // 책에 나온 라이브러리가 좀 오래된 버전이라 아래 2개는 X에서 지원하는 걸로 마이그레이션 했음.

    // 맵 서치 바 카드를 만들기 위한 CardView 라이브러리
    implementation("androidx.cardview:cardview:1.0.0")
    // 현재위치 버튼에서 사용하는 FloatingActionButton을 위한 라이브러리
    implementation("com.google.android.material:material:1.0.0")
}
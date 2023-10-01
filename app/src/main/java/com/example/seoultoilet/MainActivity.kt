package com.example.seoultoilet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.seoultoilet.databinding.ActivityMainBinding
import com.google.android.gms.maps.MapView


class MainActivity : AppCompatActivity() {

    // 뷰 바인딩
    private lateinit var binding: ActivityMainBinding
    // 맵뷰 바인딩
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 바인딩
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 맵뷰에 onCreate 함수 호출
        this.mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
    }


    // 하단부터 맵뷰의 라이프사이클 함수 호출을 위한 코드들
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}
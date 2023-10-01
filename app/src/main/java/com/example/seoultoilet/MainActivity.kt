package com.example.seoultoilet

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.seoultoilet.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng


class MainActivity : AppCompatActivity() {

    // 뷰 바인딩
    private lateinit var binding: ActivityMainBinding
    // 맵뷰 바인딩
    private lateinit var mapView: MapView

    // 런타임에서 권한이 필요한 퍼미션 목록
    val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // 퍼미션 승인 요청시 사용하는 요청 코드
    private val REQUEST_PERMISSION_CODE = 1

    // 기본 맵 줌 레벨
    val DEFAULT_ZOOM_LEVEL = 17f

    // 현재위치를 가져올 수 없는 경우 서울 시청을 보여주기 위해 설정한 변수.
    val CITY_HALL = LatLng(37.5662952, 126.97794509999994 )

    // 구글 맵 객체를 참조할 멤버 변수
    var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 바인딩
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 맵뷰에 onCreate 함수 호출
        this.mapView = binding.mapView
        mapView.onCreate(savedInstanceState)

        // 앱이 시작될 때 런타임에서 위치 서비스 관련 권한체크
        if(hasPermissons()) {
            // 권한이 있는 경우 앱 초기화
            initMap()
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_CODE)
        }

        // 현재 위치 버튼. 클릭에 이벤트 리스너 설정
        binding.myLocationButton.setOnClickListener{
            onMyLocationButtonClick()
        }
    }


    // Request Permisson에 대한 값을 받는 부분
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 맵 초기화
        initMap()
    }


    // 앱에서 사용하는 권한이 있는지 체크하는 함수
    fun hasPermissons(): Boolean {
        // 퍼미션 중 하나라도 권한이 없으면 false 반환
        for (permisson in PERMISSIONS) {
            if(ActivityCompat.checkSelfPermission(this, permisson) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }


    // 맵을 초기화하는 함수
    @SuppressLint("MissingPermission")
    fun initMap() {
        // 맵뷰에서 구글 맵을 불러오는 함수. 콜백함수에서 구글 맵 객체가 전달됨
        mapView.getMapAsync {
            // 구글맵 멤버 변수에 구글맵 객체 저장
            googleMap = it
            // 현재 위치로 이동 버튼 비활성화
            it.uiSettings.isMyLocationButtonEnabled = false
            // 위치 사용 권한이 있으면
            when {
                hasPermissons() -> {
                    // 현재위치 표시 활성화
                    it.isMyLocationEnabled = true
                    // 현재위치로 카메라 이동
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(), DEFAULT_ZOOM_LEVEL))
                }
                else -> {
                    // 권한이 없으면 서울시청의 위치로 이동
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(CITY_HALL, DEFAULT_ZOOM_LEVEL))
                }
            }
        }
    }

//    // 마지막으로 업데이트된 위치를 찾는 코드지만, getLastKnownLocation에서 null이 들어갈 수 있는 것 때문에 타입 오류가 발생, 새 코드로 대체함.
//    @SuppressLint("MissingPermission")
//    fun getMyLocation(): LatLng {
//        // 위치를 측정하는 프로바이더를 GPS 센서로 지정
//        val locationProvider: String = LocationManager.GPS_PROVIDER
//        // 위치 서비스 객체를 불러옴
//        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        // 마지막으로 업데이트된 위치를 가져옴
//        // null을 반환할 수도 있어서 오류 발생하는 듯.
//        val lastKnownLocation: Location = locationManager.getLastKnownLocation(locationProvider)
//        // 위도 경도 객체로 반환
//        return LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
//    }

    // 내 현재 위치를 찾는 법(
    @SuppressLint("MissingPermission")
    private fun getMyLocation(): LatLng {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        // 기본값을 서울시청으로 고정해놓고, 위치가 받아와질 경우(NULL이 아닐 경우) 해당 위치로 이동
        var latitude = 37.5662952
        var longitude = 126.97794509999994

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location : Location? ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
            }
        }
        return LatLng(latitude,longitude)
    }


    // 현재 위치 버튼 클릭한 경우
    fun onMyLocationButtonClick() {
        when {
            hasPermissons() -> googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(), DEFAULT_ZOOM_LEVEL))
            else -> Toast.makeText(applicationContext, "위치사용권한 설정에 동의해주세요", Toast.LENGTH_SHORT).show()
        }
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
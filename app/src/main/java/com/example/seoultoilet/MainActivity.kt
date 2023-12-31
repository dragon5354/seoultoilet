package com.example.seoultoilet

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.seoultoilet.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


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

    // ClusterManager 변수 선언
    var clusterManager: ClusterManager<MyItem>? = null

    // ClusterRenderer 변수 선언
    var clusterRenderer: ClusterRenderer? = null


    // 맵을 초기화하는 함수
    @SuppressLint("MissingPermission")
    fun initMap() {
        // 맵뷰에서 구글 맵을 불러오는 함수. 콜백함수에서 구글 맵 객체가 전달됨
        mapView.getMapAsync {
            // ClusterManager 객체 초기화
            clusterManager = ClusterManager(this, it)
            clusterRenderer = ClusterRenderer(this, it, clusterManager)

            // OnCameraIdleListener와 OnMarkerIdleListener를 clustermanager로 지정
            it.setOnCameraIdleListener(clusterManager)
            it.setOnMarkerClickListener(clusterManager)

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

    /**
     *  공공데이터 연동 관련
     */
    // 서울 열린 데이터 광장 API 키
    val API_KEY = "7a676c44756472613130346a6c6a4575"

    // 앱이 비활성화될 때 백그라운드 작업도 취소하기 위한 변수선언
    var task: ToiletReadTask? = null

    // 서울시 화장실 정보 집합을 저장할 Array 변수
    var toilets = JSONArray()

    // JSONObject를키로 MyItem 객체를 저장할 맵
    var itemMap = mutableMapOf<JSONObject, MyItem>()

    // 화장실 이미지로 사용할 Bitmap
    val bitmap by lazy {
        val drawable = resources.getDrawable(R.drawable.restroom_sign) as BitmapDrawable
        Bitmap.createScaledBitmap(drawable.bitmap, 64, 64, false)
    }

    // JSONArray를 병합하기 위해 확장함수 사용
    fun JSONArray.merge(anotherArray: JSONArray) {
        for (i in 0 until anotherArray.length()) {
            this.put(anotherArray.get(i))
        }
    }

    // 화장실 정보를 읽어와 JSONObject로 반환하는 함수
    fun readData(startIndex: Int, lastIndex: Int): JSONObject {
        val url =
            URL("http://openAPI.seoul.go.kr:8088/${API_KEY}/json/SearchPublicToiletPOIService/${startIndex}/${lastIndex}/")
        val connection = url.openConnection()
        val data = connection.getInputStream().readBytes().toString(charset("UTF-8"))
        return JSONObject(data)
    }

    // 화장실 데이터를 읽어오는 AsyncTask
    inner class ToiletReadTask : AsyncTask<Void, JSONArray, String>() {
        // 데이터를 읽기 전에 기존 데이터 초기화
        override fun onPreExecute() {
            // 구글맵 마커 초기화
            googleMap?.clear()
            // 화장실 정보 초기화
            toilets = JSONArray()
            // item맵 변수 초기화
            itemMap.clear()
        }

        override fun doInBackground(vararg params: Void?): String {
            // 서울시 데이터는 최대 1000개씩 가져올 수 있으므로, 여러 번 호출해야 함.
            val step = 1000
            var startIndex = 1
            var lastIndex = step
            var totalCount = 0

            do {
                // 백그라운드 작업이 취소된 경우 루프를 빠져나감
                if (isCancelled) break
                // totalCount가 0이 아닌 경우 값을 증가시켜, 다음 데이터를 가져오도록 함
                if (totalCount != 0) {
                    startIndex += step
                    lastIndex += step
                }

                // 데이터 조회
                val jsonObject = readData(startIndex, lastIndex)

                // totalCount를 가져옴
                totalCount = jsonObject.getJSONObject("SearchPublicToiletPOIService")
                    .getInt("list_total_count")

                // 화장실 정보 데이터 집합을 가져옴
                val rows =
                    jsonObject.getJSONObject("SearchPublicToiletPOIService").getJSONArray("row")

                // 기존 데이터와 병합
                toilets.merge(rows)

                // UI 업데이트를 위해 progress 발행
                publishProgress(rows)
            } while (lastIndex < totalCount) // 데이터 다 가져올 때까지 반복
            return "complete"
        }

        // 데이터를 읽어올때마다 중간중간 실행
        override fun onProgressUpdate(vararg values: JSONArray?) {
            // vararg는 JSONArray 파라미터를 가변적으로 전달하도록 하는 키워드, 인덱스 0의 데이터 사용
            val array = values[0]
            array?.let {
                for (i in 0 until array.length()) {
                    addMarker(array.getJSONObject(i))
                }
            }
        }

        // 백그라운드 작업이 완료된 후 실행
        override fun onPostExecute(result: String?) {
            // 자동완성 텍스트뷰(AutoCompleteTextView)에서 사용할 텍스트 리스트
            val textList = mutableListOf<String>()

            // 모든 화장실의 이름을 텍스트 리스트에 추가
            for (i in 0 until toilets.length()) {
                val toilet = toilets.getJSONObject(i)
                textList.add(toilet.getString("FNAME"))
            }

            // 자동완성 텍스트뷰에서 사용하는 어댑터 추가
            val adapter = ArrayAdapter<String>(
                this@MainActivity,
                android.R.layout.simple_dropdown_item_1line, textList
            )

            // 자동완성이 시작되는 글자수 지정
            binding.searchBar.autoCompleteTextView.threshold = 1

            // autoCompleteTextViwe의 어댑터를 상단에서 만든 어댑터로 지정
            binding.searchBar.autoCompleteTextView.setAdapter(adapter)
        }
    }

    // JSONArray에서 원소의 속성으로 원소를 검색
    // propertyName : 속성이름, value : 값
    fun JSONArray.findByChildProperty(propertyName: String, value: String) : JSONObject? {
        // JSONArray를 순환하면서 각 JSONObject의 프로퍼티 값이 같은지 확인
        for(i in 0 until length()) {
            val obj = getJSONObject(i)
            if(value == obj.getString(propertyName)) return obj
        }
        return null
    }

    // 앱이 활성화될때 서울시 데이터를 읽어옴
    override fun onStart() {
        super.onStart()
        task?.cancel(true)
        task = ToiletReadTask()
        task?.execute()

        // searchbar의 검색 아이콘의 리스너 설정
        binding.searchBar.imageView.setOnClickListener {
            // autoCompleteTextView의 텍스트를 읽어 키워드로 가져옴
            val keyword = binding.searchBar.autoCompleteTextView.text.toString()
            // 키워드 값이 없으면 그대로 리턴
            if (TextUtils.isEmpty(keyword)) return@setOnClickListener

            // 검색 키워드에 해당하는 JSONObject를 찾는다.
            toilets.findByChildProperty("FNAME", keyword)?.let {
                // itemMap에서 JSONObject를 키로 가진 MyItem 객체를 가져온다
                val myItem = itemMap[it]
                // ClusterRenderer에서 myItem을 기반으로 마커를 검색.
                val marker = clusterRenderer?.getMarker(myItem)
                // 마커에 info 윈도우를 보여준다.
                marker?.showInfoWindow()

                // 마커 위치로 맵의 카메라를 이동
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.getDouble("Y_WGS84"), it.getDouble("X_WGS84")),
                        DEFAULT_ZOOM_LEVEL
                    )
                )
                clusterManager?.cluster()
            }
            // 검색 텍스트뷰의 텍스트를 지운다.
            binding.searchBar.autoCompleteTextView.setText("")
        }
    }

    // 앱이 비활성화될때 백그라운드 작업 취소
    override fun onStop() {
        super.onStop()
        task?.cancel(true)
        task = null
    }

    // 마커를 추가하는 함수
    fun addMarker(toilet: JSONObject) {
        val item = MyItem(
            LatLng(toilet.getDouble("Y_WGS84"), toilet.getDouble("X_WGS84")),
            toilet.getString("FNAME"),
            toilet.getString("ANAME"),
            BitmapDescriptorFactory.fromBitmap(bitmap)
        )
        // clusterManager를 이용해서 마커 추가
        clusterManager?.addItem(
            MyItem(
                LatLng(toilet.getDouble("Y_WGS84"), toilet.getDouble("X_WGS84")),
                toilet.getString("FNAME"),
                toilet.getString("ANAME"),
                BitmapDescriptorFactory.fromBitmap(bitmap)
            )
        )
        // 아이템맵에 toilet 객체를 키로 Myitem 객체 저장
        itemMap.put(toilet, item)
    }
}
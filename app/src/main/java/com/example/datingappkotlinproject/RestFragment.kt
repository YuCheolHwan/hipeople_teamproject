package com.example.datingappkotlinproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.datingappkotlinproject.ActivityForMain.AppMainActivity
import com.example.datingappkotlinproject.databinding.FragmentThreeBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import noman.googleplaces.*
import java.io.IOException
import java.util.*


class RestFragment : Fragment(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback, PlacesListener {
    lateinit var binding: FragmentThreeBinding
    var search_LATLNG = LatLng(0.0, 0.0)
    private var mMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    var needRequest = false
    lateinit var appMainActivity: AppMainActivity
    var previous_marker: MutableList<Marker>? = null
    var currentflag = 0
    var searchflag = 0
    var circle: Circle? = null
    var circle1KM: CircleOptions? = null

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) // 외부 저장소
    var mCurrentLocatiion: Location? = null
    var currentPosition: LatLng? = null

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var location: Location? = null
    private var mLayout: View? = null // Snackbar 사용하기 위해서는 View가 필요합니다.

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appMainActivity = context as AppMainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentThreeBinding.inflate(inflater)
        //화면이 꺼지지않도록 해주는 코드
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        mLayout = binding.layoutMain
        //위치정보를 요청하는코드 2분마다 업데이트
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(120000) // UPDATE_INTERVAL_MS 상수 대신 1000 값을 사용
            .setFastestInterval(110000) // FASTEST_UPDATE_INTERVAL_MS 상수 대신 500 값을 사용
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)
        //위치 정보를 제공하는 서비스를 사용하기 위한것
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        //지도를 표시하기 위한 SupportMapFragment 객체를 가져오는 코드 해당 객체가 OnMapReadyCallback 인터페이스를 구현하고있는지확인
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        //이전에 추가한 마커들을 저장하기 위한 ArrayList 객체를 생성하는 코드입니다.
        previous_marker = ArrayList()

        val btnRestaurant: Button = binding.btnRestaurant
        btnRestaurant.setOnClickListener({ showRestInformation(currentPosition!!, search_LATLNG) })

        val btnCafe: Button = binding.btnCafe
        btnCafe.setOnClickListener({ showCafeInformation(currentPosition!!, search_LATLNG) })

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady :")
        mMap = googleMap
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(appMainActivity)
        updateLocation()

        // AutocompleteSupportFragment 초기화 검색기능에서 자동완성기능을 추가
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        //검색결과로 반환받을 정보를 지정
        autocompleteFragment.setPlaceFields(
            listOf(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.LAT_LNG
            )
        )


        //검색결과 선택시 onplaceselected함수호출
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: com.google.android.libraries.places.api.model.Place) {
                //검색후 선택된 장소의 위도 경도정보를 생성
                val location = Location("")
                location.latitude = p0.latLng.latitude
                location.longitude = p0.latLng.longitude
                //검색된 위치에 마커표시
                search_LATLNG = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions().position(search_LATLNG)
                //카메라이동
                val cameraPosition =
                    CameraPosition.Builder().target(search_LATLNG).zoom(15.0f).build()
                mMap?.clear()
                mMap?.addMarker(markerOptions)
                mMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                searchflag = 2
            }

            override fun onError(status: Status) {
                // 검색 결과를 처리하지 못한 경우의 코드 작성
                Log.e(TAG, "An error occurred: ${status.statusMessage}")
            }
        })

        //지도의 초기위치를 서울로 이동
        setDefaultLocation()

        // 2. 이미 퍼미션을 가지고 있다면
        // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
        startLocationUpdates()                                                                                                                                                                                                                                                                                                                            // 3. 위치 업데이트 시작

        mMap!!.uiSettings.isMyLocationButtonEnabled = true
        mMap!!.setOnMapClickListener {
            Log.d(
                TAG,
                "onMapClick :"
            )
        }
        Places.initialize(appMainActivity, "xxxxxxxxxxxxxxxxxxx")
    }

    @SuppressLint("MissingPermission")
    //updateLocation() 함수에서는 위치 정보 요청 객체(LocationRequest)를 생성하고, 위치 업데이트 콜백(LocationCallback)을 등록합니다.
    // 위치 요청 객체에서는 위치 정보의 정확도와 업데이트 간격을 설정하고, requestLocationUpdates() 함수를 호출하여 위치 정보를 요청합니다.
    fun updateLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }
        mFusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )

    }

    //위치정보 업데이트 결과를 처리
    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                location = locationList[locationList.size - 1]
                currentPosition = LatLng(location!!.latitude, location!!.longitude)
                //위치정보가 있을경우 마커타이틀,스니펫설정
                val markerTitle = getCurrentAddress(currentPosition!!)
                val markerSnippet =
                    "위도:" + location!!.latitude.toString() + " 경도:" + location!!.longitude.toString()
                Log.d(TAG, "onLocationResult : $markerSnippet")
                searchflag = 1

                //setcurrentLocation 함수를 호출하여 현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet)
                mCurrentLocatiion = location
            }
        }
    }

    private fun startLocationUpdates() {
        //위치 서비스 사용가능 여부를 확인
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting")
            //사용 불가능할경우 showDialogForLocationServiceSetting() 함수를 호출하여 사용자에게 위치 서비스 활성화를 요청합니다.
            showDialogForLocationServiceSetting()
            //위치 서비스 사용 가능한 경우, 두개의 권한이있는지확인 권한이없는경우 함수를 종료
        } else {
            val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                appMainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                appMainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음")
                return
            }
            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates")
            //위치권한있는경우 requestLocationUpdates함수를 호출하여 위치정보를요청
            //이후 checkPermission 함수를 호출하여 권한이있는경우
            //mMap!!.isMyLocationEnabled = true 현재위치버튼활성화
            mFusedLocationClient!!.requestLocationUpdates(
                locationRequest!!,
                locationCallback,
                Looper.myLooper()
            )
            if (checkPermission()) mMap!!.isMyLocationEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates")
            mFusedLocationClient!!.requestLocationUpdates(locationRequest!!, locationCallback, null)
            if (mMap != null) mMap!!.isMyLocationEnabled = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates")
            mFusedLocationClient!!.removeLocationUpdates(locationCallback)
        }
    }

    fun getCurrentAddress(latlng: LatLng): String {

        //지오코더... GPS를 주소로 변환
        val geocoder = Geocoder(appMainActivity, Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1
            )
        } catch (ioException: IOException) {
            //네트워크 문제
            Toast.makeText(appMainActivity, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            return "지오코더 서비스 사용불가"

        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(appMainActivity, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
            return "잘못된 GPS 좌표"
        }
        if (addresses == null || addresses.size == 0) {
            //Toast.makeText(this, "주소미발견", Toast.LENGTH_LONG).show()
            return ""
        } else {
            val address = addresses[0]
            return address.getAddressLine(0).toString()
        }
    }

    fun checkLocationServicesStatus(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun setCurrentLocation(location: Location?, markerTitle: String?, markerSnippet: String?) {
        if (currentMarker != null) currentMarker!!.remove()
        val currentLatLng = LatLng(location!!.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(currentLatLng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        currentMarker = mMap!!.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
        mMap!!.moveCamera(cameraUpdate)
    }

    fun setDefaultLocation() {
        //현재위치정보를 가져올수없을때 기본위치를 설정 해당위치에 마커추가
        val DEFAULT_LOCATION = LatLng(37.56, 126.97)//기본위치(서울)
        val markerTitle = "위치정보 가져올 수 없음"
        val markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요"
        if (currentMarker != null) currentMarker!!.remove()
        val markerOptions = MarkerOptions()
        markerOptions.position(DEFAULT_LOCATION)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        currentMarker = mMap!!.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15f)
        mMap!!.moveCamera(cameraUpdate) //카메라이동
    }

    //위치정보를위해 런타임 퍼미션 처리을 위한 메소드들
    //두가지 퍼미션이 모두 허용되어있는경우 true를 반환 현재위치 정보를가져오기전에 퍼미션체크수행
    private fun checkPermission(): Boolean {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            appMainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            appMainActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else false
    }

    /*
    * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
    */
    override fun onRequestPermissionsResult(
        permsRequestCode: Int,
        permissions: Array<String>,
        grandResults: IntArray
    ) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults)
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.size == REQUIRED_PERMISSIONS.size) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            var check_result = true


            // 모든 퍼미션을 허용했는지 체크합니다.
            for (result: Int in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }
            if (check_result) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates()
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다. 2가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        appMainActivity,
                        REQUIRED_PERMISSIONS[0]
                    )
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                        appMainActivity,
                        REQUIRED_PERMISSIONS[1]
                    )
                ) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(
                        mLayout!!, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "확인"
                    ) { requireActivity().finish() }.show()
                } else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(
                        mLayout!!, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "확인"
                    ) { requireActivity().finish() }.show()
                }
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private fun showDialogForLocationServiceSetting() {
        val builder = AlertDialog.Builder(appMainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                    + "위치 설정을 수정하실래요?"
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { dialog, id ->
            val callGPSSettingIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(
                callGPSSettingIntent,
                GPS_ENABLE_REQUEST_CODE
            )
        }
        builder.setNegativeButton(
            "취소"
        ) { dialog, id -> dialog.cancel() }
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음")
                        needRequest = true
                        return
                    }
                }
        }
    }

    companion object {
        private val TAG = "googlemap_example"
        private val GPS_ENABLE_REQUEST_CODE = 2001

        // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
        private val PERMISSIONS_REQUEST_CODE = 100
    }

    override fun onPlacesFailure(e: PlacesException?) {
    }

    override fun onPlacesStart() {
    }

    override fun onPlacesSuccess(places: MutableList<Place>?) {

        //디폴트 위치, Seoul
        var descripter: BitmapDescriptor? = null

        if (currentflag == 1) {
            var bitmapDrawable: BitmapDrawable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bitmapDrawable = context?.getDrawable(R.drawable.restaurant) as BitmapDrawable
            } else {
                bitmapDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.restaurant
                ) as BitmapDrawable
            }
            val scaleBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 60, 60, false)
            descripter = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
        } else if (currentflag == 2) {
            var bitmapDrawable: BitmapDrawable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bitmapDrawable = context?.getDrawable(R.drawable.cafe) as BitmapDrawable
            } else {
                bitmapDrawable =
                    ContextCompat.getDrawable(requireContext(), R.drawable.cafe) as BitmapDrawable
            }
            val scaleBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 60, 60, false)
            descripter = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
        }
        requireActivity().runOnUiThread {
            if (places != null) {
                for (place in places) {
                    val latLng = LatLng(
                        place.latitude, place.longitude
                    )
                    val markerSnippet = getCurrentAddress(latLng)
                    val markerOptions = MarkerOptions()
                    markerOptions.position(latLng)
                    markerOptions.icon(descripter)
                    markerOptions.title(place.name)
                    markerOptions.snippet(markerSnippet)
                    markerOptions.alpha(0.5f)
                    val item = mMap!!.addMarker(markerOptions)
                    previous_marker?.add(item as Marker)
                }
            }
            //중복 마커 제거
            val hashSet = HashSet<Marker>()
            hashSet.addAll(previous_marker!!)
            previous_marker?.clear()
            previous_marker?.addAll(hashSet)
        }
    }

    override fun onPlacesFinished() {
    }

    fun showRestInformation(location: LatLng, location2: LatLng) {
        currentflag = 1
        if (searchflag == 1) {
            mMap!!.clear() //지도 클리어
            if (previous_marker != null)
                previous_marker?.clear()//지역정보 마커 클리어
            NRPlaces.Builder()
                .listener(this)
                .key("xxxxxxxxxxxxxxxxxxxxx")
                .latlng(location.latitude, location.longitude) //현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.RESTAURANT) //음식점
                .build()
                .execute()
            if (circle1KM == null) {
                circle1KM = CircleOptions().center(location) // 원점
                    .radius(500.0) // 반지름 단위 : m
                    .strokeWidth(1.0f) // 선너비 0f : 선없음
                    .fillColor(Color.parseColor("#80C7EDE8")); // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            } else {
                circle?.remove() // 반경삭제
                circle1KM?.center(location)
                circle1KM?.fillColor(Color.parseColor("#80C7EDE8")); // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            }
        }
        if (searchflag == 2) {
            mMap!!.clear() //지도 클리어
            if (previous_marker != null)
                previous_marker?.clear()//지역정보 마커 클리어
            NRPlaces.Builder()
                .listener(this)
                .key("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                .latlng(location2.latitude, location2.longitude) //현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.RESTAURANT) //음식점
                .build()
                .execute()
            if (circle1KM == null) {
                circle1KM = CircleOptions().center(location2) // 원점
                    .radius(500.0) // 반지름 단위 : m
                    .strokeWidth(1.0f) // 선너비 0f : 선없음
                    .fillColor(Color.parseColor("#80C7EDE8")); // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            } else {
                circle?.remove() // 반경삭제
                circle1KM?.center(location2)
                circle1KM?.fillColor(Color.parseColor("#80C7EDE8")); // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            }
        }

    }

    fun showCafeInformation(location: LatLng, location2: LatLng) {
        currentflag = 2
        if (searchflag == 1) {
            mMap!!.clear() //지도 클리어
            if (previous_marker != null)
                previous_marker?.clear()//지역정보 마커 클리어
            NRPlaces.Builder()
                .listener(this)
                .key("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                .latlng(location.latitude, location.longitude) //현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.CAFE) //음식점
                .build()
                .execute()
            if (circle1KM == null) {
                circle1KM = CircleOptions().center(location) // 원점
                    .radius(500.0) // 반지름 단위 : m
                    .strokeWidth(1.0f) // 선너비 0f : 선없음
                    .fillColor(Color.parseColor("#80C7EDE8")); // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            } else {
                circle?.remove() // 반경삭제
                circle1KM?.center(location)
                circle1KM?.fillColor(Color.parseColor("#80C7EDE8")); // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            }
        }
        if (searchflag == 2) {
            mMap!!.clear() //지도 클리어
            if (previous_marker != null)
                previous_marker?.clear()//지역정보 마커 클리어
            NRPlaces.Builder()
                .listener(this)
                .key("xxxxxxxxxxxxxxxxxxxxxxxxxxx")
                .latlng(location2.latitude, location2.longitude) //현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.CAFE) //음식점
                .build()
                .execute()
            if (circle1KM == null) {
                circle1KM = CircleOptions().center(location2) // 원점
                    .radius(500.0) // 반지름 단위 : m
                    .strokeWidth(1.0f) // 선너비 0f : 선없음
                    .fillColor(Color.parseColor("#80C7EDE8")); // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            } else {
                circle?.remove() // 반경삭제
                circle1KM?.center(location2)
                circle1KM?.fillColor(Color.parseColor("#80C7EDE8")); // 배경색 (회색)
                circle = mMap!!.addCircle(circle1KM!!)
            }
        }
    }
}
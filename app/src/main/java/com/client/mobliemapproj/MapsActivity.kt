package com.client.mobliemapproj

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private var store = mutableListOf("서울시청", "국회의사당", "서울 서초구 신반포로 194", "아라마크현대카드캐피탈여의도본사")
    private var zoomLevel = 12F
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        geocoder = Geocoder(this)

        val seoul = LatLng(37.5217, 126.9243)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, zoomLevel))
        mMap.addMarker(MarkerOptions().position(seoul).title("Marker in Seoul"))
        var zoom = seoul

        zoomPlus.setOnClickListener {

            if (zoomLevel != 21F) {
                zoomLevel += 1F
            }
            println(zoomLevel)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoom, zoomLevel))
        }
        zoomMinus.setOnClickListener {

            if (zoomLevel != 2F) {
                zoomLevel -= 1F
            }
            println(zoomLevel)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoom, zoomLevel))
        }
        next.setOnClickListener {

            if (index == store.size) {
                Toast.makeText(this, "마지막입니다", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    println(store[index])
                    val address = geocoder.getFromLocationName(store[index], 10)
                    println(address.toString())

                    val latitude = address[0].latitude
                    val longitude = address[0].longitude

                    val new = LatLng(latitude, longitude)
                    mMap.addMarker(MarkerOptions().position(new).title(store[index]))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new, zoomLevel))

                    zoom = new
                    index++

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
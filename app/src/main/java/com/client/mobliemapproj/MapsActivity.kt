package com.client.mobliemapproj

import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private var zoomLevel = 12F
    private var index = 0
    private lateinit var paymentList: MutableList<Payment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val parser = Parser()
        paymentList = parser.read(resources)

        val sorting = Sorter()
        sorting.sortList(paymentList)

        textView3.text = paymentList[0].init()

        mMap = googleMap
        geocoder = Geocoder(this)

        var zoomSpot = LatLng(37.5217, 126.9243)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomSpot, zoomLevel))

        zoomPlus.setOnClickListener {

            if (zoomLevel != 21F) {
                zoomLevel += 1F
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomSpot, zoomLevel))
        }
        zoomMinus.setOnClickListener {

            if (zoomLevel != 2F) {
                zoomLevel -= 1F
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomSpot, zoomLevel))
        }
        next.setOnClickListener {

            if (index == paymentList.size) {
                Toast.makeText(this, "마지막입니다", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val address = geocoder.getFromLocationName(paymentList[index].address, 10)

                    val latitude = address[0].latitude
                    val longitude = address[0].longitude

                    val new = LatLng(latitude, longitude)
                    val markString = paymentList[index].place
                    val marker = mMap.addMarker(MarkerOptions().position(new).title(markString))
                    marker.tag = paymentList[index]
                    marker.showInfoWindow()
                    onMarkerClick(marker)

                    zoomSpot = new
                    index++

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        mMap.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        textView3.text = marker.tag.toString()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.position, zoomLevel))
        return false
    }
}
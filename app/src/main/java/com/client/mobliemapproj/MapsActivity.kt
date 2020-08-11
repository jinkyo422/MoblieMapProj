package com.client.mobliemapproj

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private lateinit var paymentList: MutableList<Payment>
    private var zoomLevel = 12F
    private var zoomSpot = LatLng(37.5217, 126.9243)
    private var startIndex = 0
    private var countingIndex = 0

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
        start.setOnClickListener {
            start.isVisible = false
            pause.isVisible = true
            val job = GlobalScope.launch(Dispatchers.Main) {
                for (i in startIndex until paymentList.size) {
                    delay(1000)
                    drawMarker(paymentList[i])
                    Log.d("Thread", "place : ${paymentList[i].place}")
                    countingIndex++
                }
            }
            pause.setOnClickListener {
                start.isVisible = true
                pause.isVisible = false
                startIndex = countingIndex + 1
                job.cancel()
            }
            reset.setOnClickListener {
                start.isVisible = true
                pause.isVisible = false
                job.cancel()
                mMap.clear()
                zoomSpot = LatLng(37.5217, 126.9243)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zoomSpot, zoomLevel))

                textView3.text = paymentList[0].init()
                startIndex = 0
                countingIndex = 0
            }
        }

        mMap.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        textView3.text = marker.tag.toString()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.position, zoomLevel))
        return false
    }

    private fun drawMarker(payment: Payment) {
        val address = geocoder.getFromLocationName(payment.address, 10)

        val latitude = address[0].latitude
        val longitude = address[0].longitude

        val new = LatLng(latitude, longitude)
        val markString = payment.place

        val marker = mMap.addMarker(MarkerOptions().position(new).title(markString))
        marker.tag = payment
        marker.showInfoWindow()
        onMarkerClick(marker)

        zoomSpot = new
    }
}
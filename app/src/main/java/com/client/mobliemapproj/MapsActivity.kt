package com.client.mobliemapproj

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

@Suppress("UNCHECKED_CAST")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private lateinit var paymentList: MutableList<Payment>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PaymentAdapter
    private val markerList = mutableListOf<Marker>()
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

        recyclerView = findViewById(R.id.recyclerView)
        val viewManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = viewManager

        adapter = PaymentAdapter()
        recyclerView.adapter = adapter

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
                    if (drawMarker(paymentList[i])) {
                        Log.d("Thread", "place : ${paymentList[i].place}")
                        delay(1000)
                    }
                    countingIndex++
                }
                start.isVisible = true
                pause.isVisible = false
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

                adapter.clear()
                recyclerView.adapter = adapter
                markerList.clear()
                startIndex = 0
                countingIndex = 0
            }
        }

        mMap.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        adapter.clear()
        val temp = marker.tag as MutableList<Payment>
        for (i in 0 until temp.size) {
            adapter.addItem(temp[temp.size - 1 - i])
        }
        recyclerView.adapter = adapter
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.position, zoomLevel))
        zoomSpot = marker.position
        return false
    }

    private fun drawMarker(payment: Payment): Boolean {

        val address = geocoder.getFromLocationName(payment.address, 10)

        val latitude = address[0].latitude
        val longitude = address[0].longitude

        val new = LatLng(latitude, longitude)
        val markString = payment.place
        var flag = true

        for (i in markerList) {
            if (i.position == new) {
                flag = false
                val temp = i.tag as MutableList<Payment>
                temp.add(payment)
                i.showInfoWindow()
                onMarkerClick(i)
                break
            }
        }

        if (flag) {
            val marker = mMap.addMarker(MarkerOptions().position(new).title(markString))
            markerList.add(marker)
            marker.tag = mutableListOf(payment)
            marker.showInfoWindow()
            onMarkerClick(marker)
        }

        zoomSpot = new

        return true
    }
}
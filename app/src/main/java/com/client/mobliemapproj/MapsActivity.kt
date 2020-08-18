package com.client.mobliemapproj

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

@Suppress("UNCHECKED_CAST")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private var paymentList = mutableListOf<Payment>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PaymentAdapter
    private val markerList = mutableListOf<Marker>()
    private var zoomLevel = 12F
    private var zoomSpot = LatLng(37.5217, 126.9243)
    private var startIndex = 0
    private var countingIndex = 0
    private var items = mutableListOf("ALL", "CardA", "CardB", "CardC")
    private var filter = items[0]
    private val dateMap = mutableMapOf<String, Int>()
    private lateinit var barChart: BarChart
    private lateinit var chart: Chart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val parser = Parser()

        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference.child("PaymentTable")

        myRef.orderByChild("date").startAt("2020.08.01").endAt("2020.08.11").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for(i in snapshot.children){
                    val payment = parser.read(i.value.toString())
                    paymentList.add(payment)
                    progressBar.max = paymentList.size
                }
            }
        })

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        progressBar.progress = 0

        mMap = googleMap
        geocoder = Geocoder(this)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(zoomSpot))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel))

        recyclerView = findViewById(R.id.recyclerView)
        val viewManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        viewManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = viewManager
        adapter = PaymentAdapter()
        recyclerView.adapter = adapter

        barChart = findViewById(R.id.barchart)
        chart = Chart()
        chart.initChart(barChart)

        setButtonEvent()

        mMap.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        adapter.clear()
        val temp = marker.tag as MutableList<Payment>
        for (i in 0 until temp.size) {
            adapter.addItem(temp[temp.size - 1 - i])
        }
        recyclerView.adapter = adapter
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.position))
        zoomSpot = marker.position
        return false
    }

    private fun setButtonEvent() {
        zoomPlus.setOnClickListener {
            zoomLevel = mMap.cameraPosition.zoom
            if (zoomLevel != 21F) {
                zoomLevel += 1F
            }
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel))
        }
        zoomMinus.setOnClickListener {
            zoomLevel = mMap.cameraPosition.zoom
            if (zoomLevel != 2F) {
                zoomLevel -= 1F
            }
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel))
        }
        start.setOnClickListener {
            if (countingIndex >= paymentList.size - 1) {
                reset.callOnClick()
            }
            start.isVisible = false
            pause.isVisible = true
            val job = GlobalScope.launch(Dispatchers.Main) {
                for (i in startIndex until paymentList.size) {
                    if (drawMarker(paymentList[i])) {
                        val temp = dateMap[paymentList[i].simpleDate]
                        if (temp != null) {
                            dateMap[paymentList[i].simpleDate] = temp + 1
                        } else {
                            dateMap[paymentList[i].simpleDate] = 1
                        }
                        chart.drawChart(dateMap, barChart)
                        delay(100)
                    }
                    progressBar.progress++
                    countingIndex = i
                }
                start.isVisible = true
                pause.isVisible = false
            }
            pause.setOnClickListener {
                start.isVisible = true
                pause.isVisible = false
                startIndex = countingIndex + 2
                job.cancel()
            }
            reset.setOnClickListener {
                start.isVisible = true
                pause.isVisible = false
                job.cancel()
                mMap.clear()
                zoomSpot = LatLng(37.5217, 126.9243)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(zoomSpot))

                adapter.clear()
                recyclerView.adapter = adapter
                markerList.clear()
                dateMap.clear()
                progressBar.progress = 0
                startIndex = 0
                countingIndex = 0
                barChart.clear()
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                filter = items[0]
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                filter = items[p2]
                reset.callOnClick()
            }
        }
    }

    private fun drawMarker(payment: Payment): Boolean {
        if (filter != items[0]) {
            if (payment.card != filter) {
                return false
            }
        }
        try {
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

        } catch (e: IOException) {
            when (e.message) {
                "grpc failed" -> {
                    Toast.makeText(this, "grpc failed", Toast.LENGTH_SHORT).show()
                }
                else -> throw e
            }
        }

        return true
    }
}
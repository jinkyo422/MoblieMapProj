package com.client.mobliemapproj

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.github.mikephil.charting.charts.BarChart
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 결제정보를 지도에 찍어주는 앱입니다.
 *
 * @author jingyo
 * @version 1.0
 */
class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    DatePickerDialog.OnDateSetListener {

    private lateinit var googleMap: GoogleMap
    private val zoomSpot = LatLng(37.534844, 126.986697)
    private val zoomLevel = 11.5F
    private lateinit var recyclerView: RecyclerView
    private lateinit var paymentAdapter: PaymentAdapter
    private lateinit var dateBarChart: BarChart
    private lateinit var dateChart: Chart
    private val cardType = mutableListOf("ALL CARD", "현대카드M", "현대카드X", "DIGITAL LOVER")
    private var cardTypeFilter = cardType[0]
    /**
     * paymentList : Firebase DB의 내용을 파싱하여 저장하는 list
     * pairPaymentIndexList : 다른 Pair list의 payment의 index를 저장하는 list
     * pairMarkerOptionsPaymentList : MarkerOptions과 해당하는 payment의 list를 Pair로 저장하는 list
     * latLngList : payment에 대한 위도, 경도를 저장하는 list
     * markerList : 지도에 찍힌 marker를 저장하는 list
     * dateChartMap : 날짜별 결제 건수 chart를 저장하는 map
     */
    private val paymentList = mutableListOf<Payment>()
    private val pairPaymentIndexList = mutableListOf<Pair<Payment, Int>>()
    private val pairMarkerOptionsPaymentList = mutableListOf<Pair<MarkerOptions, MutableList<Payment>>>()
    private val latLngList = mutableListOf<LatLng>()
    private val markerList = mutableListOf<Marker>()
    private val dateChartMap = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(zoomSpot))
        this.googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel))
        this.googleMap.setOnMarkerClickListener(this)

        initUI()
        setZoomEvent()
        setButtonEvent()
        setSpinnerEvent()
        setCalendarEvent()
        datesetting.callOnClick()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        paymentAdapter.clear()
        for (pairMarkerOptionsPayment in pairMarkerOptionsPaymentList) {
            if (pairMarkerOptionsPayment.first.position == marker.position && pairMarkerOptionsPayment.first.title == marker.title) {
                for (i in 0 until pairMarkerOptionsPayment.second.size) {
                    paymentAdapter.addItem(pairMarkerOptionsPayment.second[pairMarkerOptionsPayment.second.size - 1 - i])
                }
                break
            }
        }
        recyclerView.adapter = paymentAdapter
        return false
    }

    /**
     * initUI() : UI 구성요소를 초기화하는 함수입니다.
     * => toolbar, spinner, progressBar, recyclerView, dateChart
     */
    private fun initUI() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cardType)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        markerProgressBar.progress = 0

        recyclerView = findViewById(R.id.recyclerView)
        val viewManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        viewManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = viewManager
        paymentAdapter = PaymentAdapter()
        recyclerView.adapter = paymentAdapter

        dateBarChart = findViewById(R.id.barChart)
        dateChart = Chart()
        dateChart.initChart(dateBarChart)
    }

    /**
     * setZoomEvent() : Zoom In/Out 버튼에 대한 이벤트 처리 함수입니다.
     */
    private fun setZoomEvent() {
        zoomPlusButton.setOnClickListener {
            var nowZoomLevel = googleMap.cameraPosition.zoom
            if (nowZoomLevel != 21F) {
                nowZoomLevel += 1F
            }
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(nowZoomLevel))
        }
        zoomMinusButton.setOnClickListener {
            var nowZoomLevel = googleMap.cameraPosition.zoom
            if (nowZoomLevel != 2F) {
                nowZoomLevel -= 1F
            }
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(nowZoomLevel))
        }
    }

    /**
     * setButtonEvent() : start, pause, reset 버튼에 대한 이벤트 처리 함수입니다.
     */
    private fun setButtonEvent() {
        var startIndex = 0
        var countingIndex = 0

        startButton.setOnClickListener {
            if (paymentList.size == 0) {
                Toast.makeText(this, "해당 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
            if (countingIndex >= paymentList.size - 1) {
                resetButton.callOnClick()
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(zoomSpot))
            startButton.isVisible = false
            pauseButton.isVisible = true
            val markerJob = GlobalScope.launch(Dispatchers.Main) {
                for (i in startIndex until paymentList.size) {
                    markerProgressBar.progress++
                    countingIndex = i
                    if (drawMarker(paymentList[i])) {
                        val temp = dateChartMap[paymentList[i].simpleDate]
                        if (temp != null) {
                            dateChartMap[paymentList[i].simpleDate] = temp + 1
                        } else {
                            dateChartMap[paymentList[i].simpleDate] = 1
                        }
                        dateChart.drawChart(dateChartMap, dateBarChart)
                        delay(100)
                    }
                }
                startButton.isVisible = true
                pauseButton.isVisible = false
            }
            pauseButton.setOnClickListener {
                startButton.isVisible = true
                pauseButton.isVisible = false
                startIndex = countingIndex + 1
                markerJob.cancel()
            }
            resetButton.setOnClickListener {
                startButton.isVisible = true
                pauseButton.isVisible = false
                markerJob.cancel()
                googleMap.clear()
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(zoomSpot))
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel))

                startIndex = 0
                countingIndex = 0
                markerProgressBar.progress = 0

                paymentAdapter.clear()
                recyclerView.adapter = paymentAdapter
                dateBarChart.clear()

                for (pairMarkerOptionsPayment in pairMarkerOptionsPaymentList) {
                    pairMarkerOptionsPayment.second.clear()
                }
                markerList.clear()
                dateChartMap.clear()
            }
        }
    }

    /**
     * setSpinnerEvent() : spinner에 대한 이벤트 처리 함수입니다.
     */
    private fun setSpinnerEvent() {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                cardTypeFilter = cardType[0]
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                cardTypeFilter = cardType[p2]
                resetButton.callOnClick()
            }
        }
    }

    /**
     * setCalendarEvent() : calender 에 대한 이벤트 처리 함수입니다.
     *
     * @usage : calender 의 확인을 누르면 onDateSet()함수가 실행됩니다.
     */
    @Suppress("DEPRECATION")
    private fun setCalendarEvent() {
        datesetting.setOnClickListener {
            val now = Calendar.getInstance()
            println(now.toString())
            val dpd =
                DatePickerDialog.newInstance(
                    this,
                    now[Calendar.YEAR],
                    now[Calendar.MONTH],
                    now[Calendar.DAY_OF_MONTH]
                )
            dpd.isAutoHighlight = true
            dpd.vibrate(false)
            dpd.show(fragmentManager, "Datepickerdialog")
        }
    }

    /**
     * onDateSet() : calender 에서 사용자가 입력한 날짜를 받아오는 함수입니다.
     *
     * @param : year, monthOfYear, dayOfMonth 은 시작날짜
     *          yearEnd, monthOfYearEnd, dayOfMonthEnd 은 종료날짜
     * @call : getSnapshot()
     */
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("SimpleDateFormat")
    override fun onDateSet(view: DatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int, yearEnd: Int, monthOfYearEnd: Int, dayOfMonthEnd: Int) {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd")
        val startDate = dateFormat.format(dateFormat.parse("$year.${monthOfYear + 1}.$dayOfMonth"))
        val endDate =
            dateFormat.format(dateFormat.parse("$yearEnd.${monthOfYearEnd + 1}.${dayOfMonthEnd + 1}"))

        getSnapshot(startDate, endDate)
    }

    /**
     * getSnapshot() : Firebase DB의 내용을 snapshot 의 형태로 받아오는 함수입니다.
     *
     * @param : startDate, endDate
     * @call : readSnapshot()
     *         getLatLng()
     *         makeMarkerOptions()
     */
    private fun getSnapshot(startDate: String, endDate: String) {
        dbProgressBar.isVisible = true

        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference.child("PaymentTable")
        myRef.orderByChild("date").startAt(startDate).endAt(endDate)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "DB 에러 발생", Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        paymentList.clear()
                        pairPaymentIndexList.clear()
                        pairMarkerOptionsPaymentList.clear()
                        latLngList.clear()
                        resetButton.callOnClick()
                        startButton.isVisible = false
                        resetButton.isVisible = false
                        withContext(Dispatchers.IO) {
                            readSnapshot(snapshot)
                            getLatLng()
                            makeMarkerOptions()
                        }
                        markerProgressBar.max = paymentList.size
                        dbProgressBar.isVisible = false
                        startButton.isVisible = true
                        resetButton.isVisible = true
                    }
                }
            })
    }

    /**
     * readSnapshot() : parser 를 호출하여 snapshot 을 paymentList 로 저장하는 함수입니다.
     *
     * @param : snapshot
     * @call : parser.read()
     */
    private fun readSnapshot(snapshot: DataSnapshot) {
        val parser = Parser()

        for (children in snapshot.children) {
            val payment = parser.read(children.value.toString())
            paymentList.add(payment)
        }
    }

    /**
     * getLatLng() : payment의 주소를 geocoding을 통해 위도,경도를 구하고 latLngList 로 저장하는 함수입니다.
     *
     * 병렬 프로그래밍 개발 필요 (Geocoder or getFromLocationName)
     */
    private fun getLatLng() {
        val geocoderList = mutableListOf<Geocoder>()

        for (payment in paymentList) {
            latLngList.add(zoomSpot)
            geocoderList.add(Geocoder(this))
        }

        for (i in 0 until paymentList.size) {
            try {
                val address = geocoderList[i].getFromLocationName(paymentList[i].address, 1)
                val latitude = address[0].latitude
                val longitude = address[0].longitude
                val newLatLng = LatLng(latitude, longitude)
                latLngList[i] = newLatLng
            } catch (e: IOException) {
                when (e.message) {
                    "grpc failed" -> {
                        Toast.makeText(this, "구글지도 연동에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                    else -> throw e
                }
            }
        }
    }

    /**
     * makeMarkerOptions() : latLngList와 paymentList를 통해 markerOptions를 생성하고 저장하는 함수입니다.
     */
    @Suppress("DEPRECATION")
    private fun makeMarkerOptions() {
        val pairLatLngPlaceList = mutableListOf<Pair<LatLng, String>>()

        for (i in 0 until paymentList.size) {
            val newLatLng = latLngList[i]
            val listIndex = pairLatLngPlaceList.indexOf(Pair(newLatLng, paymentList[i].place))

            if (listIndex == -1) {
                pairPaymentIndexList.add(Pair(paymentList[i], pairLatLngPlaceList.size))
                pairLatLngPlaceList.add(Pair(newLatLng, paymentList[i].place))
                val bitmapDraw = resources.getDrawable(R.drawable.marker) as BitmapDrawable
                val markerImage = Bitmap.createScaledBitmap(bitmapDraw.bitmap, 150, 150, false)
                val markerOptions = MarkerOptions().position(newLatLng).title(paymentList[i].place)
                    .icon(BitmapDescriptorFactory.fromBitmap(markerImage))
                pairMarkerOptionsPaymentList.add(Pair(markerOptions, mutableListOf()))
            } else {
                pairPaymentIndexList.add(Pair(paymentList[i], listIndex))
            }
        }
    }

    /**
     * makeMarkerOptions() : markerOptions를 통해 marker를 생성하고 지도에 찍어주는 함수입니다.
     *
     * @param : payment
     */
    private fun drawMarker(payment: Payment): Boolean {
        if (cardTypeFilter != cardType[0]) {
            if (payment.card != cardTypeFilter) {
                return false
            }
        }
        var paymentIndex = 0
        for (pairPaymentIndex in pairPaymentIndexList) {
            if (pairPaymentIndex.first == payment) {
                paymentIndex = pairPaymentIndex.second
                break
            }
        }
        if (pairMarkerOptionsPaymentList[paymentIndex].second.isEmpty()) {
            val marker = googleMap.addMarker(pairMarkerOptionsPaymentList[paymentIndex].first)
            markerList.add(marker)
            marker.showInfoWindow()
            pairMarkerOptionsPaymentList[paymentIndex].second.add(payment)
            onMarkerClick(marker)
        } else {
            for (marker in markerList) {
                if (marker.position == pairMarkerOptionsPaymentList[paymentIndex].first.position && marker.title == pairMarkerOptionsPaymentList[paymentIndex].first.title) {
                    marker.showInfoWindow()
                    pairMarkerOptionsPaymentList[paymentIndex].second.add(payment)
                    onMarkerClick(marker)
                    break
                }
            }
        }
        return true
    }
}
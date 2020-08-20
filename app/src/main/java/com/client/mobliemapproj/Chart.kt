package com.client.mobliemapproj

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * MainAcitivity의 dateChart 메소드를 관리하는 클래스입니다.
 *
 * @fun : initChart(), drawChart()
 */
class Chart {

    /**
     * initChart() : xml로 선언된 barChart를 초기화하는 함수입니다.
     *
     * @param : barChart
     */
    fun initChart(barChart: BarChart) {
        barChart.setTouchEnabled(true)
        barChart.setScaleEnabled(false)
        barChart.isDragEnabled = false
        barChart.setPinchZoom(false)

        barChart.xAxis.setDrawLabels(true)
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.setDrawAxisLine(true)

        barChart.axisLeft.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
    }

    /**
     * drawChart() : dateMap을 사용하여 barChart를 생성하는 함수입니다.
     *
     * @param : dateMap, barChart
     */
    fun drawChart(dateMap: MutableMap<String, Int>, barChart: BarChart) {
        val labels = mutableListOf<String>()
        val entries = mutableListOf<BarEntry>()
        var index = 0
        var sum = 0

        for (i in dateMap) {
            labels.add(i.key)
            entries.add(BarEntry(index.toFloat(), i.value.toFloat()))
            index++
            sum += i.value
        }

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.labelCount = entries.size
        barChart.xAxis.textSize = 7f

        val set = BarDataSet(entries, "총 결제건수 : $sum")
        set.valueTextSize = 12f
        set.valueFormatter = DefaultValueFormatter(0)
        barChart.data = BarData(set)

        barChart.invalidate()
    }
}
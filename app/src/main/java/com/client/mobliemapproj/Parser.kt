package com.client.mobliemapproj

import android.annotation.SuppressLint
import android.content.res.Resources
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.text.SimpleDateFormat

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class Parser {

    @SuppressLint("SimpleDateFormat")
    fun read(resources: Resources): MutableList<Payment> {

//        val inputStream = resources.openRawResource(R.raw.mookup)
        val inputStream = resources.openRawResource(R.raw.redundantmookup)
        val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))

        removeFirstLine(reader)
        val paymentList = mutableListOf<Payment>()

        reader.readLines().forEach {

            val comma = it.split(",")
            val paymentId = comma[0].toInt()
            val card = comma[2]
            val person = comma[3]
            val place = comma[4]
            val address = comma[5]

            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
            val date = dateFormat.parse(comma[1])

            val payment = Payment(paymentId, date, place, address, person, card)
            paymentList.add(payment)
        }

        return paymentList
    }

    private fun removeFirstLine(reader: BufferedReader) {
        reader.readLine()
    }
}
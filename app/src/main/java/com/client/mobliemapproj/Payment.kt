package com.client.mobliemapproj

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

data class Payment(
    val paymentID: Int,
    val date: Date,
    val simpleDate: String,
    val place: String,
    val address: String,
    val person: String,
    val card: String
) {
    @SuppressLint("SimpleDateFormat")
    override fun toString(): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")

        return "$place\n" +
                "$address\n\n" +
                "거래일자 : ${dateFormat.format(date)}\n" +
                "결제카드 : $card\n" +
                "카드 소지자 : $person\n\n"
    }

    fun init(): String {
        return "장소\n" +
                "주소\n\n" +
                "거래일자 : \n" +
                "결제카드 : \n" +
                "카드 소지자 : \n\n"
    }
}
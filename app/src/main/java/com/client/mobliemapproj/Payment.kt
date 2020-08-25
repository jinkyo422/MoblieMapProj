package com.client.mobliemapproj

import java.text.SimpleDateFormat
import java.util.*

/**
 * 결제정보를 저장하는 데이터클래스입니다.
 *
 * @fun : toString(), drawChart()
 */
data class Payment(
    val paymentID: Int,
    val date: Date,
    val simpleDate: String,
    val place: String,
    val address: String,
    val money: Int,
    val card: String
) {
    override fun toString(): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA)

        return "$place\n" +
                "$address\n\n" +
                "거래일자 : ${dateFormat.format(date)}\n" +
                "결제금액 : ${money}원\n" +
                "결제카드 : $card\n\n"
    }
}
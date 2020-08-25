package com.client.mobliemapproj

import java.text.SimpleDateFormat
import java.util.*

/**
 * Firebase DB의 snapshot을 payment로 파싱하는 클래스입니다.
 *
 * @fun : read(), removeBrace(), removeEq()
 */
class Parser {

    fun read(value: String): Payment {

        val data = removeBrace(value)
        val comma = data.split(", ")

        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA)
        val date = dateFormat.parse(removeEq(comma[0]))
        val address = removeEq(comma[1])
        val money = removeEq(comma[2]).toInt()
        val paymentId = removeEq(comma[3]).toInt()
        val place = removeEq(comma[4])
        val card = removeEq(comma[5])

        val simpleDate = removeEq(comma[0]).split(" ")

        return Payment(paymentId, date!!, simpleDate[0], place, address, money, card)
    }

    private fun removeBrace(data: String): String {
        val first = data.split("{")
        val last = first[1].split("}")
        return last[0]
    }

    private fun removeEq(s: String): String {
        val value = s.split("=")
        return value[1]
    }
}
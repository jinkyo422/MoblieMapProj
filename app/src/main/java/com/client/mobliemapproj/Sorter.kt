package com.client.mobliemapproj

class Sorter {

    fun sortList(paymentList: MutableList<Payment>) {
        paymentList.sortBy { it.date }
    }
}
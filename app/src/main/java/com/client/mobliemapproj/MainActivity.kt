package com.client.mobliemapproj

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val parser = Parser()
        val paymentList = parser.read(resources)

        val sorting = Sorter()
        sorting.sortList(paymentList)
    }
}
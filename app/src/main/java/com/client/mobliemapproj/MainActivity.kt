package com.client.mobliemapproj

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val parser = Parser()
        val paymentList = parser.read(resources)

        val sorting = Sorter()
        sorting.sortList(paymentList)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val viewManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = viewManager

        val recyclerViewAdapter = PaymentAdapter()
        for(i in paymentList){
            recyclerViewAdapter.addItem(i)
        }

        recyclerView.adapter = recyclerViewAdapter
    }
}
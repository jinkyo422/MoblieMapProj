package com.client.mobliemapproj

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var items = mutableListOf("ALL", "CardA", "CardB", "CardC")

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

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                textView1.text = items[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                textView1.text = "nothing"
            }
        }
    }
}
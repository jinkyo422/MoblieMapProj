package com.client.mobliemapproj

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var items = mutableListOf("ALL", "CardA", "CardB", "CardC")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference.child("PaymentTable")

        val parser = Parser()
        val paymentList = mutableListOf<Payment>()

        myRef.orderByChild("date").startAt("2020.08.01").endAt("2020.08.05").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for(i in snapshot.children){
                    val payment = parser.read(i.value.toString())
                    println(payment)
                    paymentList.add(payment)
                }
            }
        })

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val viewManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = viewManager

        val recyclerViewAdapter = PaymentAdapter()

        val dateMap = mutableMapOf<String, Int>()

        for (i in paymentList) {
            recyclerViewAdapter.addItem(i)

            val temp = dateMap[i.simpleDate]
            if (temp != null) {
                dateMap[i.simpleDate] = temp + 1
            } else {
                dateMap[i.simpleDate] = 1
            }
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
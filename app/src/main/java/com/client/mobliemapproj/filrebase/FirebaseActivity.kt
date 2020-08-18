package com.client.mobliemapproj.filrebase

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.client.mobliemapproj.R
import com.google.firebase.database.FirebaseDatabase

class FirebaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filrebase)

        val send = findViewById<Button>(R.id.sendingButton)

        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference

        send.setOnClickListener {

            val csvParser = CSVParser()
            val csvList = csvParser.read(resources)

            for (i in csvList) {
                myRef.child("PaymentTable").push().setValue(i)
                println(i.toString())
            }
        }
    }
}
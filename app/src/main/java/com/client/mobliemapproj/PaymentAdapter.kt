package com.client.mobliemapproj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView에 들어갈 payment를 관리하는 adapter클래스입니다.
 *
 * @fun : onCreateViewHolder(), getItemCount(), onBindViewHolder(), addItem(), clear()
 * @innerclass : PaymentViewHolder
 */
class PaymentAdapter : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    private val items = mutableListOf<Payment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.payment_item, parent, false)

        return PaymentViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    fun addItem(payment: Payment){
        items.add(payment)
    }

    fun clear(){
        items.clear()
    }

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var textView: TextView = itemView.findViewById(R.id.textView)

        fun setItem(item: Payment) {
            textView.text = item.toString()
        }
    }
}
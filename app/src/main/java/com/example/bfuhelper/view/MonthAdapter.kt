package com.example.bfuhelper.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bfuhelper.R
import com.example.bfuhelper.model.sport.Month

class MonthAdapter(
    private var months: List<Month>,
    private val onMonthClickListener: (Month) -> Unit
) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    private var selectedMonth: Month? = null

    fun setSelectedMonth(month: Month?) {
        this.selectedMonth = month
        notifyDataSetChanged()
    }

    fun updateMonths(newMonths: List<Month>) {
        months = newMonths
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_month, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val month = months[position]
        holder.monthName.text = month.getDisplayName(holder.itemView.context)
        Log.d("MonthAdapter", month.toString())
        // Проверяем, является ли текущий месяц выбранным
        if (month == selectedMonth) {
            holder.monthName.setTextAppearance(R.style.month_text_selected)
        } else {
            holder.monthName.setTextAppearance(R.style.month_text_unselected)
        }

        holder.itemView.setOnClickListener { onMonthClickListener(month) }
    }

    override fun getItemCount(): Int {
        return months.size
    }

    class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthName: TextView = itemView.findViewById(R.id.month_name)
    }
}

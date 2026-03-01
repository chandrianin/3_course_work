package com.example.bfuhelper.model.sport

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.bfuhelper.R
import com.example.bfuhelper.view.CircularProgressView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// BindingAdapter для установки внешнего прогресса
@BindingAdapter(value = ["app:progress", "app:maxProgress"], requireAll = true)
fun setOuterProgress(view: CircularProgressView, progress: Int, maxProgress: Int) {
    view.setOuterProgress(progress.toFloat(), maxProgress.toFloat())
}

// BindingAdapter для установки внутреннего прогресса
@BindingAdapter(value = ["app:innerProgress", "app:innerMaxProgress"], requireAll = true)
fun setInnerProgress(view: CircularProgressView, innerProgress: Int, innerMaxProgress: Int) {
    view.setInnerProgress(innerProgress.toFloat(), innerMaxProgress.toFloat())
}

// BindingAdapter для установки цвета посещений
@BindingAdapter("app:statusTextColor")
fun setStatusTextColor(textView: TextView, status: Status?) {
    textView.setTextColor(
        textView.context.getColor(
            when (status) {
                Status.Visit -> R.color.visit
                Status.Absence -> R.color.absence
                Status.Disease -> R.color.disease
                Status.Future -> R.color.future
                else -> {
                    R.color.dark
                }
            }
        )
    )
}


@BindingAdapter("app:formattedDayAndWeekday")
fun setFormattedDayAndWeekday(textView: TextView, data: SportItem?) {
    if (data == null) {
        textView.text = ""
        return
    }

    val dayOfMonth = data.day
    val monthEnum = data.month

    // Получаем текущий год
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    // Создаем экземпляр Calendar для указанной даты
    val calendar = Calendar.getInstance().apply {
        clear() // Очищаем все поля, чтобы избежать нежелательных остаточных значений
        isLenient =
            false // Делаем календарь строгим, чтобы ловить некорректные даты (например, 30 февраля)
        set(Calendar.YEAR, currentYear)
        // Важно: ordinal у enum Month (если он начинается с JANUARY = 0) соответствует константам Calendar.MONTH
        set(Calendar.MONTH, monthEnum.ordinal)
        set(Calendar.DAY_OF_MONTH, dayOfMonth.toInt())
        set(
            Calendar.HOUR_OF_DAY,
            0
        ) // Устанавливаем время на начало дня, чтобы избежать проблем с DST/часовыми поясами
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val dayOfMonthFormatter = SimpleDateFormat("dd", Locale("ru"))


    try {
        val dayString = dayOfMonthFormatter.format(calendar.time)

        val context = textView.context
        val weekdaysArray = context.resources.getStringArray(R.array.weekdays_short)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val weekdayString = weekdaysArray[dayOfWeek - 1]

        textView.text = "$dayString\n$weekdayString"
    } catch (e: IllegalArgumentException) {
        textView.text = ""
        e.printStackTrace()
    } catch (e: Exception) {
        // Общая обработка других возможных ошибок форматирования
        textView.text = ""
        e.printStackTrace()
    }
}

// BindingAdapter для динамического отображения текста статуса из ресурсов
@BindingAdapter("app:statusText")
fun setStatusText(textView: TextView, status: Status?) {
    textView.setText(
        when (status) {
            Status.Visit -> R.string.status_visit_text
            Status.Absence -> R.string.status_absence_text
            Status.Disease -> R.string.status_disease_text
            Status.Future -> R.string.status_future_text
            null -> R.string.status_default_text
        }
    )
}
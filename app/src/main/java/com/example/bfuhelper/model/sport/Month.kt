package com.example.bfuhelper.model.sport

import android.content.Context
import androidx.annotation.StringRes
import com.example.bfuhelper.R

enum class Month(@StringRes val displayNameResId: Int) {
    Jan(R.string.month_jan),
    Feb(R.string.month_feb),
    March(R.string.month_march),
    April(R.string.month_april),
    May(R.string.month_may),
    June(R.string.month_june),
    July(R.string.month_july),
    August(R.string.month_august),
    Sept(R.string.month_sept),
    Oct(R.string.month_oct),
    Nov(R.string.month_nov),
    Dec(R.string.month_dec);

    /**
     * Возвращает локализованное название месяца, используя предоставленный контекст.
     */
    fun getDisplayName(context: Context): String {
        return context.getString(displayNameResId)
    }

//    override fun toString(): String {
//        return when (this) {
//            Jan -> "Январь"
//            Feb -> "Февраль"
//            March -> "Март"
//            April -> "Апрель"
//            May -> "Май"
//            June -> "Июнь"
//            July -> "Июль"
//            August -> "Август"
//            Sept -> "Сентябрь"
//            Oct -> "Октябрь"
//            Nov -> "Ноябрь"
//            Dec -> "Декабрь"
//        }
//    }
}
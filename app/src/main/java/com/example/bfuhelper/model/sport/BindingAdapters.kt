package com.example.bfuhelper.model.sport

import androidx.databinding.BindingAdapter
import com.example.bfuhelper.view.CircularProgressView

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
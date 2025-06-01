package com.example.bfuhelper.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import com.example.bfuhelper.R

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var circleSpacing: Float = 0f // Расстояние между кругами

    // Внешний круг баллов
    private val outerBackgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerProgressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val outerOval = RectF()
    private var outerStrokeWidth: Float = 0f
    private var outerBackgroundColor: Int = Color.GRAY
    private var outerProgressColor: Int = Color.BLUE
    private var outerProgress: Float = 0f
    private var outerMaxProgress: Float = 100f

    private var outerCurrentAnimatedProgress: Float = 0f
    private var outerTargetProgress: Float = 0f


    // Внутренний круг
    private val innerBackgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerProgressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val innerOval = RectF()
    private var innerStrokeWidth: Float = 0f
    private var innerBackgroundColor: Int = Color.GRAY
    private var innerProgressColor: Int = Color.BLUE
    private var innerProgress: Float = 0f
    private var innerMaxProgress: Float = 100f
    private var innerCurrentAnimatedProgress: Float = 0f
    private var innerTargetProgress: Float = 0f

    private var animationDuration: Long = 1000L // в мс

    private var outerProgressAnimator: ValueAnimator? = null
    private var innerProgressAnimator: ValueAnimator? = null

    private var isOuterProgressInitialized = false
    private var isInnerProgressInitialized = false


    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CircularProgressView, defStyleAttr, 0)
        try {
            // Внешний круг
            outerStrokeWidth =
                typedArray.getDimension(R.styleable.CircularProgressView_strokeWidth, 20f)
            outerBackgroundColor = typedArray.getColor(
                R.styleable.CircularProgressView_backgroundColor,
                Color.parseColor("#E0E0E0")
            )
            outerProgressColor = typedArray.getColor(
                R.styleable.CircularProgressView_progressColor,
                Color.parseColor("#4CAF50")
            )
            outerProgress = typedArray.getFloat(R.styleable.CircularProgressView_progress, 0f)
            outerMaxProgress =
                typedArray.getFloat(R.styleable.CircularProgressView_maxProgress, 100f)

            // Внутренний круг
            innerStrokeWidth =
                typedArray.getDimension(R.styleable.CircularProgressView_innerStrokeWidth, 20f)
            innerBackgroundColor = typedArray.getColor(
                R.styleable.CircularProgressView_innerBackgroundColor,
                Color.parseColor("#C0C0C0")
            )
            innerProgressColor = typedArray.getColor(
                R.styleable.CircularProgressView_innerProgressColor,
                Color.parseColor("#673AB7")
            )
            innerProgress = typedArray.getFloat(R.styleable.CircularProgressView_innerProgress, 0f)
            innerMaxProgress =
                typedArray.getFloat(R.styleable.CircularProgressView_innerMaxProgress, 100f)

            circleSpacing =
                typedArray.getDimension(R.styleable.CircularProgressView_circleSpacing, 10f)

            animationDuration =
                typedArray.getFloat(R.styleable.CircularProgressView_animationDuration, 1000f)
                    .toLong()
        } finally {
            typedArray.recycle()
        }

        setupPaints()
    }

    private fun setupPaints() {
        // Настройка Paint для внешнего круга
        outerBackgroundPaint.style = Paint.Style.STROKE
        outerBackgroundPaint.strokeCap = Paint.Cap.ROUND
        outerBackgroundPaint.color = outerBackgroundColor
        outerBackgroundPaint.strokeWidth = outerStrokeWidth

        outerProgressPaint.style = Paint.Style.STROKE
        outerProgressPaint.strokeCap = Paint.Cap.ROUND
        outerProgressPaint.color = outerProgressColor
        outerProgressPaint.strokeWidth = outerStrokeWidth

        // Настройка Paint для внутреннего круга
        innerBackgroundPaint.style = Paint.Style.STROKE
        innerBackgroundPaint.strokeCap = Paint.Cap.ROUND
        innerBackgroundPaint.color = innerBackgroundColor
        innerBackgroundPaint.strokeWidth = innerStrokeWidth

        innerProgressPaint.style = Paint.Style.STROKE
        innerProgressPaint.strokeCap = Paint.Cap.ROUND
        innerProgressPaint.color = innerProgressColor
        innerProgressPaint.strokeWidth = innerStrokeWidth
    }

    // Методы для установки значений внешнего круга
    fun setOuterStrokeWidth(width: Float) {
        this.outerStrokeWidth = width
        outerBackgroundPaint.strokeWidth = width
        outerProgressPaint.strokeWidth = width
        invalidate()
    }

    fun setOuterBackgroundColor(@ColorInt color: Int) {
        this.outerBackgroundColor = color
        outerBackgroundPaint.color = color
        invalidate()
    }

    fun setOuterProgressColor(@ColorInt color: Int) {
        this.outerProgressColor = color
        outerProgressPaint.color = color
        invalidate()
    }

    fun setOuterProgress(current: Float, max: Float) {
        outerMaxProgress = max

        if (outerProgressAnimator?.isRunning == true) {
            outerProgressAnimator?.cancel()
        }

        val startProgress: Float
        if (!isOuterProgressInitialized) {
            // Первый раз, когда устанавливается внешний прогресс.
            // Устанавливаем значение сразу, без анимации
            outerCurrentAnimatedProgress = current
            isOuterProgressInitialized = true
            invalidate() // Перерисовываем View
            return
        } else {
            startProgress = outerCurrentAnimatedProgress
        }

        outerTargetProgress = current // Новое целевое значение

        if (startProgress != outerTargetProgress) {
            outerProgressAnimator =
                ValueAnimator.ofFloat(startProgress, outerTargetProgress).apply {
                    duration = animationDuration
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animator ->
                        outerCurrentAnimatedProgress = animator.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
        } else {
            outerCurrentAnimatedProgress = outerTargetProgress
            invalidate()
        }
    }

    // Методы для установки значений внутреннего круга
    fun setInnerStrokeWidth(width: Float) {
        this.innerStrokeWidth = width
        innerBackgroundPaint.strokeWidth = width
        innerProgressPaint.strokeWidth = width
        invalidate()
    }

    fun setInnerBackgroundColor(@ColorInt color: Int) {
        this.innerBackgroundColor = color
        innerBackgroundPaint.color = color
        invalidate()
    }

    fun setInnerProgressColor(@ColorInt color: Int) {
        this.innerProgressColor = color
        innerProgressPaint.color = color
        invalidate()
    }

    fun setInnerProgress(current: Float, max: Float) {
        innerMaxProgress = max

        if (innerProgressAnimator?.isRunning == true) {
            innerProgressAnimator?.cancel()
        }

        val startProgress: Float
        if (!isInnerProgressInitialized) {
            // Первый раз, когда устанавливается внутренний прогресс.
            // Устанавливаем значение сразу, без анимации
            innerCurrentAnimatedProgress = current
            isInnerProgressInitialized = true
            invalidate() // Перерисовываем View
            return
        } else {
            startProgress = innerCurrentAnimatedProgress
        }

        innerTargetProgress = current // Новое целевое значение

        if (startProgress != innerTargetProgress) {
            innerProgressAnimator =
                ValueAnimator.ofFloat(startProgress, innerTargetProgress).apply {
                    duration = animationDuration
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animator ->
                        innerCurrentAnimatedProgress = animator.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
        } else {
            innerCurrentAnimatedProgress = innerTargetProgress
            invalidate()
        }
    }

    // Метод для установки расстояния между кругами
    fun setCircleSpacing(spacing: Float) {
        this.circleSpacing = spacing
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Отрисовка внешнего круга
        val outerRadius = Math.min(centerX, centerY) - outerStrokeWidth / 2
        outerOval.set(
            centerX - outerRadius,
            centerY - outerRadius,
            centerX + outerRadius,
            centerY + outerRadius
        )

        canvas.drawArc(outerOval, 0f, 360f, false, outerBackgroundPaint)
        if (outerMaxProgress > 0) {
            val outerSweepAngle = (outerCurrentAnimatedProgress / outerMaxProgress) * 360f
            canvas.drawArc(outerOval, -90f, outerSweepAngle, false, outerProgressPaint)
        }

        // Отрисовка внутреннего круга
        // Радиус внутреннего круга = радиус внешнего - (ширина внешнего + расстояние + ширина внутреннего) / 2
        val innerRadius =
            outerRadius - (outerStrokeWidth / 2) - circleSpacing - (innerStrokeWidth / 2)

        if (innerRadius <= 0) return

        innerOval.set(
            centerX - innerRadius,
            centerY - innerRadius,
            centerX + innerRadius,
            centerY + innerRadius
        )

        canvas.drawArc(innerOval, 0f, 360f, false, innerBackgroundPaint)
        if (innerMaxProgress > 0) {
            val innerSweepAngle = (innerCurrentAnimatedProgress / innerMaxProgress) * 360f
            canvas.drawArc(innerOval, -90f, innerSweepAngle, false, innerProgressPaint)
        }
    }
}
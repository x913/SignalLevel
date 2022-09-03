package com.github.x913.signallevel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.lang.Float.min
import java.lang.Integer.max
import kotlin.properties.Delegates

enum class DefaultSignalLevelColor(val color: Int) {
    HIGH(Color.rgb(0x30, 0xD1, 0x58)),
    MEDIUM(Color.rgb(0xFF, 0xD6, 0x0A)),
    LOW(Color.rgb(0xFF, 0x45, 0x3A)),
    EMPTY(Color.rgb(0x91, 0x95, 0x9b));
}

class SignalLevelView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, R.style.DefaultSignalLevelStyle)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, R.attr.signalLevelStyle)
    constructor(context: Context) : this(context, null)

    private var highLevelColor by Delegates.notNull<Int>()
    private var mediumLevelColor by Delegates.notNull<Int>()
    private var lowLevelColor by Delegates.notNull<Int>()
    private var emptyLevelColor by Delegates.notNull<Int>()

    private val fieldRect = RectF(0f, 0f, 0f, 0f)
    private var barSize: Float = 0f

    private lateinit var highLevelPaint: Paint
    private lateinit var mediumLevelPaint: Paint
    private lateinit var lowLevelPaint: Paint
    private lateinit var emptyLevelPaint: Paint

    var signalLevel: Float = 0f
        set(value) {
            field = value
            updateViewSizes()
            requestLayout()
            invalidate()
        }

    var signalBarCorners: Float = 120f
        set(value) {
            field = value
            updateViewSizes()
            requestLayout()
            invalidate()
        }

    var signalBarsCount: Int = 4
        set(value) {
            field = value
            updateViewSizes()
            requestLayout()
            invalidate()
        }

    var signalBarsGap: Int = 20
        set(value) {
            field = value
            updateViewSizes()
            requestLayout()
            invalidate()
        }

    init {
        if(attributeSet != null) {
            initAttributes(attributeSet, defStyleAttr, defStyleRes)
        } else {
            initDefault()
        }
        initPaints()
    }

    private fun initPaints() {
        val strokeWidthDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics)

        highLevelPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = highLevelColor
            it.strokeWidth = strokeWidthDp
        }
        mediumLevelPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = mediumLevelColor
            it.strokeWidth = strokeWidthDp
        }
        lowLevelPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = lowLevelColor
            it.strokeWidth = strokeWidthDp
        }
        emptyLevelPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = emptyLevelColor
            it.strokeWidth = strokeWidthDp
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom



        val desiredBarSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DESIRED_BAR_WIDTH,
            resources.displayMetrics
        ).toInt()


        val desiredWidth = max(minWidth, signalBarsCount * desiredBarSizePx + paddingLeft + paddingRight)
        val desiredHeight = max(minHeight, 400 + paddingTop + paddingBottom)

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSizes()
    }


    private fun initAttributes(attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SignalLevelView, defStyleAttr, defStyleRes)

        highLevelColor = typedArray.getColor(R.styleable.SignalLevelView_highLevelColor, DefaultSignalLevelColor.HIGH.color)
        mediumLevelColor = typedArray.getColor(R.styleable.SignalLevelView_mediumLevelColor, DefaultSignalLevelColor.MEDIUM.color)
        lowLevelColor = typedArray.getColor(R.styleable.SignalLevelView_lowLevelColor, DefaultSignalLevelColor.LOW.color)
        emptyLevelColor = typedArray.getColor(R.styleable.SignalLevelView_emptyLevelColor, DefaultSignalLevelColor.EMPTY.color)
        signalLevel = typedArray.getFloat(R.styleable.SignalLevelView_signalLevel, 0f)
        signalBarCorners = typedArray.getFloat(R.styleable.SignalLevelView_signalBarCorners, 120f)
        signalBarsCount = typedArray.getInt(R.styleable.SignalLevelView_signalBarsCount, 4)
        signalBarsGap = typedArray.getInt(R.styleable.SignalLevelView_signalBarsGap, 5)

        typedArray.recycle()
    }

    private fun initDefault() {
        highLevelColor = DefaultSignalLevelColor.HIGH.color
        mediumLevelColor = DefaultSignalLevelColor.MEDIUM.color
        lowLevelColor = DefaultSignalLevelColor.LOW.color
        emptyLevelColor = DefaultSignalLevelColor.EMPTY.color
        signalLevel = 0f
        signalBarsCount = 4
        signalBarCorners = 120f
        signalBarsGap = 5
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var offset = fieldRect.left
        val snWidth = ((fieldRect.width()) / signalBarsCount) - signalBarsGap

        (1..signalBarsCount).forEachIndexed { index, _ ->
            val ratio = index / signalBarsCount.toFloat()
            canvas.drawRoundRect(
                offset + signalBarsGap, (fieldRect.height() / 2f) - (ratio * fieldRect.height() / 2f),
                offset + snWidth, fieldRect.height(),
                signalBarCorners, signalBarCorners,
                if(signalLevel >= 0.80) {
                    highLevelPaint
                } else if (signalLevel in 0.50f..0.79f) {
                    if(index / signalBarsCount.toFloat() < 0.50f) {
                        mediumLevelPaint
                    } else {
                        emptyLevelPaint
                    }
                } else {
                    if(index / signalBarsCount.toFloat() < 0.20f && signalLevel != 0f) {
                        lowLevelPaint
                    } else {
                        emptyLevelPaint
                    }
                }
            )
            offset += snWidth + signalBarsGap
        }
    }

    private fun updateViewSizes() {
        val safeWidth = width - paddingLeft - paddingRight
        val safeHeight = height - paddingBottom - paddingTop

        val singleBarWidth = safeWidth / signalBarsCount.toFloat()
        val singleBarHeight = safeHeight.toFloat()

        barSize = min(singleBarWidth, singleBarHeight)

        val fieldWidth = (barSize * signalBarsCount)
        val fieldHeight = safeHeight.toFloat()

        fieldRect.left = paddingLeft + (safeWidth - fieldWidth) / 2
        fieldRect.top = paddingTop + (safeHeight - fieldHeight) / 2
        fieldRect.right = fieldRect.left + fieldWidth
        fieldRect.bottom = fieldRect.top + fieldHeight
    }

    companion object {
        const val DESIRED_BAR_WIDTH = 50f
    }

}
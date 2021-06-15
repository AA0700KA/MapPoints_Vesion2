package ru.skillbranch.mappoints_vesion2.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import ru.skillbranch.mappoints_vesion2.R
import ru.skillbranch.mappoints_vesion2.extensions.dpToIntPx

class RoundedButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val circlePaint : Paint
//    private val bitmapBack : Bitmap

    init {
        circlePaint = Paint()
        circlePaint.color = context.resources.getColor(R.color.white)
        circlePaint.style = Paint.Style.FILL

//        bitmapBack = BitmapFactory.decodeResource(context.resources, R.drawable.ic_back_icon)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(context.dpToIntPx(100), context.dpToIntPx(100))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawCircle(100f, 100f, 100f, circlePaint)
    //    canvas?.drawBitmap(bitmapBack, 60f, 60f, null)
    }

}
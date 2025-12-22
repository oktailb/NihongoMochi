package org.nihongo.mochi.ui.dictionary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.mlkit.vision.digitalink.Ink

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        // Use a theme color for the ink if possible
        color = Color.BLACK // Fallback
        isAntiAlias = true
        strokeWidth = 12f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val path = Path()
    private var inkBuilder = Ink.builder()
    private var strokeBuilder: Ink.Stroke.Builder? = null

    // Double buffering
    private var extraCanvas: Canvas? = null
    private var extraBitmap: Bitmap? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            if (extraBitmap != null) extraBitmap!!.recycle()
            extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            extraCanvas = Canvas(extraBitmap!!)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        extraBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val t = System.currentTimeMillis()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                strokeBuilder = Ink.Stroke.builder()
                strokeBuilder?.addPoint(Ink.Point.create(x, y, t))
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                strokeBuilder?.addPoint(Ink.Point.create(x, y, t))
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                strokeBuilder?.let {
                    it.addPoint(Ink.Point.create(x, y, t))
                    inkBuilder.addStroke(it.build())
                }
                strokeBuilder = null
                extraCanvas?.drawPath(path, paint)
                path.reset()
                invalidate()
            }
        }
        return true
    }

    fun getInk(): Ink {
        return inkBuilder.build()
    }

    fun clear() {
        path.reset()
        inkBuilder = Ink.builder()
        extraBitmap?.eraseColor(Color.TRANSPARENT)
        invalidate()
    }
}
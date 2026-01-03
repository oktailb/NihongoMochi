package org.nihongo.mochi.ui.dictionary

import com.google.mlkit.vision.digitalink.Ink

actual class DrawingInk(val ink: Ink)

actual class InkPoint(val point: Ink.Point) {
    actual val x: Float get() = point.x
    actual val y: Float get() = point.y
    actual val t: Long get() = point.timestamp ?: 0L
}

actual fun createInkPoint(x: Float, y: Float, t: Long): InkPoint = InkPoint(Ink.Point.create(x, y, t))

actual class InkStroke(val stroke: Ink.Stroke) {
    actual fun getPoints(): List<InkPoint> = stroke.points.map { InkPoint(it) }
}

actual class InkStrokeBuilder {
    private val builder = Ink.Stroke.builder()
    actual fun addPoint(point: InkPoint) {
        builder.addPoint(point.point)
    }
    actual fun build(): InkStroke = InkStroke(builder.build())
}

actual class InkBuilder {
    private val builder = Ink.builder()
    actual fun addStroke(stroke: InkStroke) {
        builder.addStroke(stroke.stroke)
    }
    actual fun build(): DrawingInk = DrawingInk(builder.build())
}

actual fun newInkBuilder(): InkBuilder = InkBuilder()
actual fun newStrokeBuilder(): InkStrokeBuilder = InkStrokeBuilder()

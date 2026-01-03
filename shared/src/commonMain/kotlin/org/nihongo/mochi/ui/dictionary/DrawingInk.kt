package org.nihongo.mochi.ui.dictionary

// KMP abstraction for ML Kit Ink

expect class DrawingInk

expect class InkPoint {
    val x: Float
    val y: Float
    val t: Long
}

expect fun createInkPoint(x: Float, y: Float, t: Long): InkPoint

expect class InkStroke {
    fun getPoints(): List<InkPoint>
}

expect class InkStrokeBuilder {
    fun addPoint(point: InkPoint)
    fun build(): InkStroke
}

expect class InkBuilder {
    fun addStroke(stroke: InkStroke)
    fun build(): DrawingInk
}

expect fun newInkBuilder(): InkBuilder
expect fun newStrokeBuilder(): InkStrokeBuilder

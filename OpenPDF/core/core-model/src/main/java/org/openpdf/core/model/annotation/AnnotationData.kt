package org.openpdf.core.model.annotation

sealed class AnnotationData {

    data class Highlight(
        val quads: List<FloatArray>,
        val color: Int,
        val opacity: Float,
        val contents: String?,
    ) : AnnotationData()

    data class Underline(
        val quads: List<FloatArray>,
        val color: Int,
    ) : AnnotationData()

    data class StrikeOut(
        val quads: List<FloatArray>,
        val color: Int,
    ) : AnnotationData()

    data class Squiggly(
        val quads: List<FloatArray>,
        val color: Int,
    ) : AnnotationData()

    data class Ink(
        val paths: List<List<InkPoint>>,
        val color: Int,
        val lineWidth: Float,
        val opacity: Float,
    ) : AnnotationData()

    data class FreeText(
        val rectLeft: Float,
        val rectTop: Float,
        val rectRight: Float,
        val rectBottom: Float,
        val text: String,
        val fontSize: Float,
        val fontColor: Int,
        val fillColor: Int?,
    ) : AnnotationData()

    data class TextNote(
        val x: Float,
        val y: Float,
        val icon: String,
        val contents: String,
        val isOpen: Boolean,
    ) : AnnotationData()

    data class Square(
        val rectLeft: Float,
        val rectTop: Float,
        val rectRight: Float,
        val rectBottom: Float,
        val color: Int,
        val interiorColor: Int?,
        val borderWidth: Float,
    ) : AnnotationData()

    data class Circle(
        val rectLeft: Float,
        val rectTop: Float,
        val rectRight: Float,
        val rectBottom: Float,
        val color: Int,
        val interiorColor: Int?,
        val borderWidth: Float,
    ) : AnnotationData()

    data class Line(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val color: Int,
        val borderWidth: Float,
        val hasArrowStart: Boolean,
        val hasArrowEnd: Boolean,
    ) : AnnotationData()

    data class Stamp(
        val rectLeft: Float,
        val rectTop: Float,
        val rectRight: Float,
        val rectBottom: Float,
        val icon: String,
    ) : AnnotationData()
}

data class InkPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
)

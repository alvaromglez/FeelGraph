package com.example.feelgraph

import android.graphics.PointF
import android.view.View
import android.widget.ImageView
import org.opencv.core.Point
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class TouchUtils {
    companion object{
        fun transformTouchToBitmapCoordinates(x: Float, y: Float, view: View, imageView: ImageView): PointF {
            // Obtiene las dimensiones del ImageView y del Bitmap
            val imageViewWidth = view.width
            val imageViewHeight = view.height
            val bitmapWidth = imageView.drawable.intrinsicWidth
            val bitmapHeight = imageView.drawable.intrinsicHeight

            // Calcula la escala y el offset en caso de que el contenido esté centrado
            val scale =
                min(imageViewWidth.toFloat() / bitmapWidth, imageViewHeight.toFloat() / bitmapHeight)
            val dx = (imageViewWidth - bitmapWidth * scale) * 0.5f
            val dy = (imageViewHeight - bitmapHeight * scale) * 0.5f

            return PointF((x - dx) / scale, (y - dy) / scale)
        }

        fun isTouchNearStartPoint(touchX: Float, touchY: Float, startPoint: Point, threshold: Float): Boolean {
            val dx = Math.abs(touchX - startPoint.x.toFloat())
            val dy = Math.abs(touchY - startPoint.y.toFloat())
            return dx <= threshold && dy <= threshold
        }

        fun isTouchNearLine(touchX: Float, touchY: Float, line: Line, view: View, imageView: ImageView): Boolean {
            val touchPoint = transformTouchToBitmapCoordinates(touchX, touchY, view, imageView)
            val start = line.p1
            val end = line.p2
            val dx = end.x - start.x
            val dy = end.y - start.y
            val lengthSquared = dx * dx + dy * dy

            // Asegurarte de que la longitud al cuadrado no sea cero para evitar división por cero
            if (lengthSquared == 0.0) {
                // La línea es un punto; usa un cálculo de distancia punto a punto
                val distanceToPoint = sqrt((start.x - touchPoint.x) * (start.x - touchPoint.x) + (start.y - touchPoint.y) * (start.y - touchPoint.y))
                return distanceToPoint <= 20f
            } else {
                val t = max(0.0, min(1.0, ((touchPoint.x - start.x) * dx + (touchPoint.y - start.y) * dy) / lengthSquared))
                val projection = PointF((start.x + t * dx).toFloat(), (start.y + t * dy).toFloat())

                val distanceToLine = sqrt((projection.x - touchPoint.x) * (projection.x - touchPoint.x) + (projection.y - touchPoint.y) * (projection.y - touchPoint.y))

                return distanceToLine <= 15f
            }
        }

        fun getLineTypeBasedOnSlope(line: Line): LineType {
            val slope = if (line.p2.x != line.p1.x) {
                (line.p2.y - line.p1.y) / (line.p2.x - line.p1.x)
            } else {
                Double.POSITIVE_INFINITY
            }

            return when {
                slope > 0 -> LineType.DESCENDING
                slope == 0.0 -> LineType.HORIZONTAL
                else -> LineType.ASCENDING
            }
        }
    }
}
package com.example.feelgraph

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.opencv.core.Point

class ImageHandler {

    private val imageProcessor by lazy { ImageProcessor() }


    fun colourImage(bitmap: Bitmap, callback:(Point?, List<Line>, Bitmap) -> Unit) {
        val processedLines = imageProcessor.processImage(bitmap)
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawColor(Color.WHITE)
        processedLines.forEach{line ->
            canvas.drawLine(
                line.p1.x.toFloat(), line.p1.y.toFloat(),
                line.p2.x.toFloat(), line.p2.y.toFloat(),
                paint
            )
        }

        val startPoint = imageProcessor.findStartOfGraph(processedLines)
        drawStartPoint(startPoint, canvas)

        val trendBitmap = paintGraphLines(resultBitmap, processedLines)
        callback(startPoint, processedLines, trendBitmap)
    }

    private fun drawStartPoint(startPoint: Point?, canvas: Canvas){
        startPoint?.let {start ->
            val startPaint = Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            }
            canvas.drawCircle(start.x.toFloat(), start.y.toFloat(), 10f, startPaint)
        }
    }

    private fun paintGraphLines(bitmap: Bitmap, lines: List<Line>): Bitmap {
        val paintedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(paintedBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)  // Dibuja el bitmap original en el canvas

        val paintIncreasing = Paint().apply {
            color = Color.YELLOW
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

        val paintDecreasing = Paint().apply {
            color = Color.BLUE
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
        val paintStable = Paint().apply {
            color = Color.GREEN
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }

        // Calcula la tendencia de cada línea y pinta sobre el bitmap
        lines.forEach { line ->
            val slope = if (line.p2.x != line.p1.x)
                (line.p2.y - line.p1.y) / (line.p2.x - line.p1.x)
            else
                Double.POSITIVE_INFINITY

            val paint = when {
                slope > 0 -> paintDecreasing
                slope == 0.0 -> paintStable
                else -> paintIncreasing
            }

            canvas.drawLine(line.p1.x.toFloat(), line.p1.y.toFloat(), line.p2.x.toFloat(), line.p2.y.toFloat(), paint)
        }

        return paintedBitmap
    }

}
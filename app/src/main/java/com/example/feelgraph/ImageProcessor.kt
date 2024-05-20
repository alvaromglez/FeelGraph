package com.example.feelgraph

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ImageProcessor {

    fun processImage(bitmap: Bitmap): List<Line>{

        //Convierte a escala de grises
        val mat = convertBitmapToGrayscaleMatHandlingTransparency(bitmap)
        //Eliminación de ruido mediante filtro gaussiano
        Imgproc.GaussianBlur(mat, mat, Size(5.0, 5.0), 0.0)
        //Detectar bordes
        Imgproc.Canny(mat, mat, 50.0, 150.0)
        //Deteccion de lineas con HoughLinesP
        val lines = Mat()
        Imgproc.HoughLinesP(mat, lines, 1.0, Math.PI / 360, 30, 20.0, 10.0)

        val lineList = mutableListOf<Line>()
        for(i in 0 until lines.rows()){
            val data = lines.get(i, 0)
            val startPoint = Point(data[0], data[1])
            val endPoint = Point(data[2], data[3])
            lineList.add(Line(startPoint, endPoint))
        }
        lineList.sortBy { min(it.p1.x, it.p2.x) }


        mat.release()
        lines.release()

        return filterAxis(lineList)
    }


    private fun convertBitmapToGrayscaleMatHandlingTransparency(bitmap: Bitmap): Mat {
        val matWithAlpha = Mat(bitmap.width, bitmap.height, CvType.CV_8UC1)
        Utils.bitmapToMat(bitmap, matWithAlpha)

        // Comprobar si el Bitmap tiene transparencia.
        if (bitmap.hasAlpha()) {
            // Obtener el canal alpha y verificar la transparencia.
            val alphaChannel = ArrayList<Mat>(4)
            Core.split(matWithAlpha, alphaChannel)
            val alphaMat = alphaChannel[3]

            // Crear una máscara donde los píxeles transparentes se marcarán con blanco.
            val mask = Mat(alphaMat.size(), alphaMat.type(), Scalar(255.0))
            Core.compare(alphaMat, Scalar(0.0), mask, Core.CMP_EQ)

            // Rellenar los píxeles transparentes con blanco en el Mat original.
            val whiteMat = Mat(matWithAlpha.size(), matWithAlpha.type(), Scalar(255.0, 255.0, 255.0))
            whiteMat.copyTo(matWithAlpha, mask)

            // Liberar recursos de Mats temporales.
            alphaChannel.forEach { it.release() }
            mask.release()
            whiteMat.release()
        }

        // Convertir la imagen al espacio de color de escala de grises.
        val matGray = Mat()
        Imgproc.cvtColor(matWithAlpha, matGray, Imgproc.COLOR_RGBA2GRAY)
        matWithAlpha.release()

        return matGray
    }

    private fun identifyXAxis(lines: List<Line>): Line? {
        return lines.filter { abs(it.p1.y - it.p2.y) < 10 } // Líneas horizontales
            .maxByOrNull { max(it.p1.y, it.p2.y) } // El más bajo
    }

    private fun excludeYAxis(lines: List<Line>): List<Line> {

        // Identificar la línea vertical más a la izquierda que es potencialmente el eje Y
        val yAxis = lines.filter { abs(it.p1.x - it.p2.x) < 10 }
            .minByOrNull { it.p1.x } // Elige la línea con la coordenada 'x' más pequeña, que sería la más a la izquierda visualmente

        return lines.filter { line ->
            line != yAxis
        }
    }

    private fun excludeAxesLines(lines: List<Line>): List<Line> {

        val xAxis = lines.filter { abs(it.p1.y - it.p2.y) < 10 } // Líneas horizontales
            .maxByOrNull { max(it.p1.y, it.p2.y) } // El más bajo

        val yAxis = lines.filter { abs(it.p1.x - it.p2.x) < 10 }
            .minByOrNull { it.p1.x }

        return lines.filter { line ->
            line != xAxis && line != yAxis
        }
    }

    private fun findIntersectionPoints(lines: List<Line>, xAxis: Line?, threshold: Double = 10.0): List<Point> {
        val intersectionPoints = mutableListOf<Point>()

        if(xAxis != null) {
            val xAxisY = xAxis.p1.y // Asumiendo que el eje X es perfectamente horizontal

            lines.forEach { line ->
                // Comprobar si el inicio o el fin del segmento está cerca del eje X
                val startsAboveAndEndsOnAxis =
                    line.p1.y != xAxisY && abs(line.p2.y - xAxisY) < threshold
                val startsOnAxisAndEndsAbove =
                    abs(line.p1.y - xAxisY) < threshold && line.p2.y != xAxisY

                // Si cumple con las condiciones, añadir el punto de intersección a la lista
                if (startsAboveAndEndsOnAxis) {
                    intersectionPoints.add(Point(line.p2.x, xAxisY))
                } else if (startsOnAxisAndEndsAbove) {
                    intersectionPoints.add(Point(line.p1.x, xAxisY))
                }
            }
        }
        return intersectionPoints
    }



    private fun filterCloseIntersectionPoints(intersectionPoints: List<Point>, distanceThreshold: Double = 10.0): List<Point> {
        if (intersectionPoints.isEmpty()) return emptyList()

        // Ordenar los puntos por su coordenada x
        val sortedPoints = intersectionPoints.sortedBy { it.x }
        val filteredPoints = mutableListOf<Point>()

        var currentGroupStart = sortedPoints.first()
        filteredPoints.add(currentGroupStart) // Añadir el primer punto

        for (point in sortedPoints.drop(1)) {
            // Si el punto actual está más allá del umbral de distancia desde el inicio del grupo actual
            if (abs(point.x - currentGroupStart.x) > distanceThreshold) {
                // Finaliza el grupo actual y comienza uno nuevo
                currentGroupStart = point
                filteredPoints.add(point)
            }
            // Si el punto está dentro del umbral, sigue siendo parte del grupo actual y lo ignoramos
        }

        return filteredPoints
    }

    private fun divideAndFilterXAxis(xAxis: Line?, intersectionPoints: List<Point>): List<Line> {
        // Si no hay puntos de intersección, se elimina el eje X completo
        if (intersectionPoints.isEmpty()) {
            return emptyList()
        }

        val filteredSegments = mutableListOf<Line>()
        val sortedIntersectionPoints = intersectionPoints.sortedBy { it.x }

        // Añadir segmentos intermedios, alternando entre mantener y eliminar
        for (i in 0 until sortedIntersectionPoints.size - 1) {
            if (i % 2 == 0)
                filteredSegments.add(Line(sortedIntersectionPoints[i], sortedIntersectionPoints[i + 1]))
        }

        // Si hay un número impar de puntos de intersección, añadir el segmento desde el último punto hasta el final del eje X
        if(xAxis != null) {
            if (sortedIntersectionPoints.size % 2 != 0) {
                // Esto añade el último tramo si la cantidad de puntos de intersección es impar
                filteredSegments.add( Line( sortedIntersectionPoints.last(), Point(xAxis.p2.x, xAxis.p1.y)))
            }
        }

        return filteredSegments
    }

    private fun filterAxis(lines: List<Line>): List<Line>{

        //Ejes formados por doble linea
        val notYAxis = excludeYAxis(lines)
        val notAxis = excludeAxesLines(notYAxis)

        val xAxis = identifyXAxis(notAxis)

        val listPoints = findIntersectionPoints(notAxis, xAxis, 10.0)
        val intersectionPoints = filterCloseIntersectionPoints(listPoints, 10.0)

        //Se eliminan completamente los ejes, obteniendo solo el gráfico.
        val graphLines = excludeAxesLines(notAxis)

        return divideAndFilterXAxis(xAxis, intersectionPoints) + graphLines
    }


    fun findStartOfGraph(lines: List<Line>): Point? {
        return  lines.minByOrNull { it.p1.x }?.p1
    }

}
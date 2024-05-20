package com.example.feelgraph

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri

class PdfUtils(private val context: Context) {

    fun convertPdfToBitmap(pdfUri: Uri): Bitmap? {
        return try{
            context.contentResolver.openFileDescriptor(pdfUri, "r")?.use{ parcelFileDescriptor ->
                PdfRenderer(parcelFileDescriptor).use { pdfRenderer ->
                    val page: PdfRenderer.Page = pdfRenderer.openPage(0)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bitmap
                }

            }
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
}
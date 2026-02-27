package org.openpdf.mupdf

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.openpdf.core.model.search.SearchResult
import java.io.Closeable

class MuPdfPage internal constructor(
    // Uncomment when MuPDF AAR is integrated:
    // private val nativePage: com.artifex.mupdf.fitz.Page,
    // private val pdfPage: com.artifex.mupdf.fitz.PDFPage?,
    val pageIndex: Int,
    private val mupdfDispatcher: CoroutineDispatcher,
) : Closeable {

    val widthPt: Float get() = 612f // nativePage.bounds.width
    val heightPt: Float get() = 792f // nativePage.bounds.height

    suspend fun render(
        zoom: Float = 1f,
        colorInvert: Boolean = false,
    ): Bitmap = withContext(mupdfDispatcher) {
        val width = (widthPt * zoom).toInt()
        val height = (heightPt * zoom).toInt()

        // Full MuPDF rendering:
        // val matrix = Matrix(zoom, 0f, 0f, zoom, 0f, 0f)
        // val pixmap = nativePage.toPixmap(matrix, ColorSpace.DeviceRGB, true)
        // val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // bitmap.setPixels(pixmap.pixels, 0, width, 0, 0, width, height)
        // pixmap.destroy()
        // if (colorInvert) invertBitmap(bitmap)
        // return bitmap

        // Stub: return empty white bitmap
        Bitmap.createBitmap(
            width.coerceAtLeast(1),
            height.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
    }

    suspend fun renderTile(
        zoom: Float,
        tileX: Int,
        tileY: Int,
        tileWidth: Int,
        tileHeight: Int,
    ): Bitmap = withContext(mupdfDispatcher) {
        // Tile-based rendering for high zoom levels:
        // val matrix = Matrix(zoom, 0f, 0f, zoom, -tileX.toFloat(), -tileY.toFloat())
        // Render only the tile region
        Bitmap.createBitmap(tileWidth, tileHeight, Bitmap.Config.ARGB_8888)
    }

    suspend fun search(query: String): List<SearchResult> = withContext(mupdfDispatcher) {
        // val text = nativePage.toStructuredText()
        // val hits = text.search(query)
        // hits.map { quads -> SearchResult(pageIndex, quads, query) }
        emptyList()
    }

    suspend fun getTextContent(): String = withContext(mupdfDispatcher) {
        // val text = nativePage.toStructuredText()
        // text.copy() returns full page text for reflow
        ""
    }

    override fun close() {
        // nativePage.destroy()
    }
}

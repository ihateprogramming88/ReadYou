package org.openpdf.mupdf

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.openpdf.core.model.document.PdfDocumentInfo
import java.io.Closeable

data class OutlineItem(
    val title: String,
    val pageIndex: Int,
    val children: List<OutlineItem>,
)

class MuPdfDocument internal constructor(
    // Uncomment when MuPDF AAR is integrated:
    // private val nativeDoc: com.artifex.mupdf.fitz.Document,
    // private val pdfDoc: com.artifex.mupdf.fitz.PDFDocument?,
    private val mupdfDispatcher: CoroutineDispatcher,
) : Closeable {

    val pageCount: Int
        get() = 0 // nativeDoc.countPages()

    val isPdf: Boolean
        get() = true // nativeDoc.isPDF

    val info: PdfDocumentInfo by lazy {
        // Populated from nativeDoc.getMetaData() calls
        PdfDocumentInfo(
            uri = "",
            title = null, // nativeDoc.getMetaData(Document.META_INFO_TITLE),
            author = null, // nativeDoc.getMetaData(Document.META_INFO_AUTHOR),
            subject = null,
            creator = null,
            producer = null,
            creationDate = null,
            modificationDate = null,
            pageCount = pageCount,
            fileSizeBytes = 0,
            isEncrypted = false, // nativeDoc.needsPassword()
            hasAcroForm = false,
            pdfVersion = null,
        )
    }

    suspend fun loadPage(index: Int): MuPdfPage = withContext(mupdfDispatcher) {
        require(index in 0 until pageCount) { "Page index $index out of range [0, $pageCount)" }
        // val nativePage = nativeDoc.loadPage(index)
        // MuPdfPage(nativePage, nativePage as? PDFPage, mupdfDispatcher)
        MuPdfPage(index, mupdfDispatcher)
    }

    suspend fun getOutline(): List<OutlineItem> = withContext(mupdfDispatcher) {
        // Parse nativeDoc.loadOutline() into OutlineItem tree
        emptyList()
    }

    suspend fun save(incremental: Boolean = true) = withContext(mupdfDispatcher) {
        // pdfDoc?.save(outputPath, if (incremental) "incremental" else "")
    }

    override fun close() {
        // nativeDoc.destroy()
    }
}

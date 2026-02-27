package org.openpdf.mupdf

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.openpdf.core.common.di.MuPdfDispatcher
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton

sealed class DocumentSource {
    data class FromUri(val uri: Uri) : DocumentSource()
    data class FromBytes(val buffer: ByteArray, val magic: String) : DocumentSource()
}

@Singleton
class MuPdfEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    @MuPdfDispatcher private val mupdfDispatcher: CoroutineDispatcher,
) {

    companion object {
        private const val MAX_FILE_SIZE_BYTES = 500L * 1024 * 1024 // 500 MB
        private const val SMALL_FILE_THRESHOLD = 8L * 1024 * 1024 // 8 MB
        private val PDF_MAGIC = byteArrayOf(0x25, 0x50, 0x44, 0x46) // %PDF
    }

    suspend fun openDocument(
        source: DocumentSource,
        password: String? = null,
    ): MuPdfDocument = withContext(mupdfDispatcher) {
        when (source) {
            is DocumentSource.FromUri -> openFromUri(source.uri, password)
            is DocumentSource.FromBytes -> openFromBytes(source.buffer, source.magic, password)
        }
    }

    private fun openFromUri(uri: Uri, password: String?): MuPdfDocument {
        val resolver = context.contentResolver
        val fd = resolver.openFileDescriptor(uri, "r")
            ?: throw IllegalArgumentException("Cannot open file descriptor for $uri")
        val size = fd.statSize

        if (size > MAX_FILE_SIZE_BYTES) {
            fd.close()
            throw IllegalArgumentException("File too large: $size bytes (max $MAX_FILE_SIZE_BYTES)")
        }

        val stream = resolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream for $uri")

        return try {
            val buffer = stream.use { it.readBytes() }
            validatePdfMagic(buffer)
            openFromBytes(buffer, "application/pdf", password)
        } finally {
            fd.close()
        }
    }

    private fun openFromBytes(buffer: ByteArray, magic: String, password: String?): MuPdfDocument {
        validatePdfMagic(buffer)

        // MuPDF JNI integration point:
        // val nativeDoc = com.artifex.mupdf.fitz.Document.openDocument(buffer, magic)
        // if (nativeDoc.needsPassword()) {
        //     if (password == null || !nativeDoc.authenticatePassword(password)) {
        //         throw PasswordRequiredException()
        //     }
        // }
        // return MuPdfDocument(nativeDoc, nativeDoc as? PDFDocument, mupdfDispatcher)

        // Stub implementation until MuPDF AAR is integrated
        return MuPdfDocument(mupdfDispatcher)
    }

    private fun validatePdfMagic(buffer: ByteArray) {
        if (buffer.size < 4) {
            throw IllegalArgumentException("File too small to be a valid PDF")
        }
        if (!buffer.copyOfRange(0, 4).contentEquals(PDF_MAGIC)) {
            throw IllegalArgumentException("File does not have valid PDF header")
        }
    }
}

class PasswordRequiredException : Exception("Document requires a password")

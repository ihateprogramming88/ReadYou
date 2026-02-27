package org.openpdf.core.model.document

data class PdfDocumentInfo(
    val uri: String,
    val title: String?,
    val author: String?,
    val subject: String?,
    val creator: String?,
    val producer: String?,
    val creationDate: Long?,
    val modificationDate: Long?,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val isEncrypted: Boolean,
    val hasAcroForm: Boolean,
    val pdfVersion: String?,
)

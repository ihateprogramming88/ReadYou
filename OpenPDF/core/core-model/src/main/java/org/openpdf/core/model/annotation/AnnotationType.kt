package org.openpdf.core.model.annotation

enum class AnnotationType(val mupdfType: Int) {
    TEXT(0),
    LINK(1),
    FREE_TEXT(2),
    LINE(3),
    SQUARE(4),
    CIRCLE(5),
    POLYGON(6),
    POLY_LINE(7),
    HIGHLIGHT(8),
    UNDERLINE(9),
    SQUIGGLY(10),
    STRIKE_OUT(11),
    REDACT(12),
    STAMP(13),
    CARET(14),
    INK(15),
    POPUP(16),
    FILE_ATTACHMENT(17),
    SOUND(18),
    MOVIE(19),
    RICH_MEDIA(20),
    WIDGET(21),
    SCREEN(22),
    PRINTER_MARK(23),
    TRAP_NET(24),
    WATERMARK(25),
    UNKNOWN(-1);

    companion object {
        fun fromMuPdfType(type: Int): AnnotationType =
            entries.find { it.mupdfType == type } ?: UNKNOWN
    }
}

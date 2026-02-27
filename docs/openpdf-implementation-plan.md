# OpenPDF: Modern Android PDF Reader — Implementation Plan

## Context

There is a clear gap in the Android ecosystem for a **modern, privacy-respecting, feature-rich PDF reader**. The best current option (Foxit PDF) bundles Google services and is proprietary. Open-source alternatives are either view-only (GrapheneOS PDF Viewer), have dated UIs (MuPDF Viewer, Document Viewer), or are cluttered (Librera). No existing app combines Material You design, full annotation tools, form filling, digital signatures, and a zero-permission security model.

**Goal**: Build a new standalone Android PDF reader **in its own fresh repository** (working title: **OpenPDF**) that matches Foxit's feature set while being fully open-source (AGPL v3), Google-free, and privacy-first.

**Inspiration**: [BentoPDF](https://github.com/alam00000/bentopdf) (11.7k stars) — a web-based privacy-first PDF toolkit with 50+ tools, AGPL-3.0 licensed. While BentoPDF is a browser app (React/TypeScript/WASM), its feature breadth (annotations, forms, signatures, redaction, security) and privacy-first philosophy directly inform our Android-native feature set.

**PDF Engine**: MuPDF (AGPL v3) — the most feature-complete open-source PDF library with native support for 25+ annotation types, form filling, digital signatures, and fast C-based rendering. (BentoPDF also uses MuPDF under the hood via PyMuPDF WASM.)

**Tech Stack**: Kotlin, Jetpack Compose, Material 3/Material You, Hilt DI, Room, DataStore, Clean Architecture (MVI + MVVM).

**Repository**: Brand new standalone repo (not part of ReadYou). ReadYou's codebase served as reference for Android architecture patterns only.

---

## Module Structure

```
OpenPDF/
├── app/                          # Application shell: MainActivity, NavGraph, Hilt setup
├── core/
│   ├── core-common/              # Coroutine dispatchers, Result type, extensions
│   ├── core-model/               # Pure Kotlin domain models (no Android deps)
│   ├── core-database/            # Room DB: recent files, bookmarks, annotation index
│   ├── core-datastore/           # DataStore preferences
│   ├── core-ui/                  # Material 3 theme, shared composables
│   └── core-security/            # EncryptedSharedPreferences, password handling
├── mupdf/
│   ├── mupdf-fitz/               # MuPDF AAR (prebuilt native .so files)
│   └── mupdf-wrapper/            # Kotlin-idiomatic wrapper around MuPDF JNI
└── feature/
    ├── feature-viewer/           # PDF rendering, zoom/pan, scroll modes, night mode
    ├── feature-annotations/      # All annotation tools + undo/redo
    ├── feature-forms/            # Interactive form filling
    ├── feature-signatures/       # Digital signature verify/create
    ├── feature-search/           # Full-text search
    ├── feature-settings/         # App settings screens
    └── feature-filemanager/      # Recent files, SAF file picker, file info
```

**Dependency flow**: `app → feature-* → core-* + mupdf-wrapper → mupdf-fitz`

---

## MuPDF Integration Strategy

### Wrapper Design
- `MuPdfEngine` (singleton) — opens documents, manages lifecycle
- `MuPdfDocument` — wraps `com.artifex.mupdf.fitz.Document` / `PDFDocument`
- `MuPdfPage` — renders pages to Bitmap, handles text extraction
- `MuPdfAnnotationManager` — CRUD for all 25+ annotation types using `PDFAnnotation` API
- `MuPdfFormManager` — form field read/write via `PDFWidget`
- `MuPdfSignatureManager` — verify/create signatures
- `MuPdfSearchEngine` — full-text search via `StructuredText`

### Thread Safety
MuPDF forbids concurrent access to the same Document/Page. All JNI calls go through a single-threaded coroutine dispatcher: `Dispatchers.IO.limitedParallelism(1)`.

### Rendering Pipeline
1. Check `PageBitmapCache` (LRU) for cached result
2. If miss: render on MuPDF dispatcher → `page.toPixmap(matrix, colorSpace)` → convert to Android Bitmap
3. At high zoom: tile-based rendering (256dp tiles, render only visible + 1-tile margin)
4. Low-res full-page bitmap as placeholder while tiles load

---

## Feature Set

### Core Viewing
- Fast MuPDF-based rendering
- Scroll modes: continuous vertical, single page, two-page spread (tablets)
- Pinch-to-zoom with smooth animation
- Text reflow / reading mode
- Night mode / dark mode / sepia / AMOLED black / custom colors
- Table of contents navigation
- Thumbnail page overview grid
- Page rotation, page slider, go-to-page
- Reading progress persistence

### Annotation Tools
- **Text markup**: highlight (multi-color), underline, strikeout, squiggly
- **Drawing**: freehand ink with pressure sensitivity, eraser
- **Text**: sticky notes, free text typed on page
- **Shapes**: rectangle, circle, line, arrow, polygon, polyline
- **Stamps**: standard + custom from image
- **Management**: undo/redo (via MuPDF journal), annotation list panel, properties editor
- **Import/Export**: XFDF format, save annotations into PDF (incremental save)

### Forms & Signatures
- Interactive form filling: text fields, checkboxes, radio buttons, dropdowns, list boxes
- Form data export/import
- Digital signature verification with certificate info display
- Handwritten signature pad (draw or import image)

### Search
- Full-text search with result highlighting on page
- Navigate between results, result count
- Case-sensitive / whole-word options

### File Management
- Open via SAF (Storage Access Framework) — zero storage permissions
- Open from other apps via VIEW intent filter
- Recent documents list with reading progress
- File info/properties display
- Print support via Android PrintManager

---

## Security Architecture

| Principle | Implementation |
|-----------|---------------|
| **Zero permissions** | No permissions in manifest. SAF provides content:// URIs for file access |
| **No network** | No INTERNET permission. Network security config blocks all traffic |
| **No analytics** | No Firebase, no tracking, no telemetry of any kind |
| **No Google deps** | No Google Play Services, Firebase, or Google Cloud APIs |
| **Encrypted storage** | EncryptedSharedPreferences (AES-256-GCM) for sensitive data like saved passwords |
| **Input validation** | PDF magic byte check, file size limits, JNI exception handling |
| **Process isolation** | (Phase 3) MuPDF rendering in isolated `android:process=":mupdf_sandbox"` |

### Manifest
```xml
<!-- ZERO permissions -->
<application android:allowBackup="false" android:usesCleartextTraffic="false">
    <activity android:name=".MainActivity" android:exported="true">
        <!-- SAF + VIEW intent for application/pdf -->
    </activity>
</application>
```

---

## Database Schema (Room)

| Table | Purpose |
|-------|---------|
| `recent_documents` | URI, title, author, page count, last opened, last page/zoom/scroll, thumbnail path |
| `bookmarks` | Document URI, page index, user title, sort order |
| `annotation_index` | Lightweight index for fast queries (actual data lives in the PDF itself) |

---

## Navigation Graph

```
RecentFilesScreen (start) → ViewerScreen → {
    TableOfContentsScreen,
    ThumbnailOverviewScreen,
    AnnotationListScreen,
    SearchScreen,
    FileInfoScreen,
}
SettingsScreen → {
    AppearanceSettings,
    ViewerSettings,
    SecuritySettings,
    LanguageSettings,
    AboutScreen → LicenseScreen,
}
```

---

## UI Architecture

**Pattern**: MVI (Model-View-Intent) with unidirectional data flow
- `ViewerIntent` (sealed interface) — user actions
- `ViewerUiState` (data class) — immutable state
- `ViewerSideEffect` (sealed interface) — one-shot events (errors, navigation)
- ViewModel exposes `StateFlow<UiState>` and `Flow<SideEffect>`

**ViewerScreen Layout**:
```
┌──────────────────────────────────────┐
│ TopAppBar: filename | page X/Y | ⋮  │  ← auto-hide on tap
├──────────────────────────────────────┤
│                                      │
│         PDF Page Content             │  ← Canvas with zoom/pan
│         (pinch-zoom, scroll)         │
│                                      │
├──────────────────────────────────────┤
│ [Annotation Toolbar] (floating)      │  ← conditional
├──────────────────────────────────────┤
│ [TOC][Thumb][Search][Annot][More]    │  ← auto-hide
│ [═══════○═══════════════════]        │  ← page slider
└──────────────────────────────────────┘
```

---

## Phased Implementation Roadmap

### Phase 1: Foundation — "Read a PDF" (Weeks 1–4)
1. Project scaffolding: multi-module Gradle, version catalog, Hilt setup
2. MuPDF AAR build + `MuPdfEngine`/`MuPdfDocument`/`MuPdfPage` wrappers
3. Basic `ViewerScreen`: single page + continuous scroll rendering
4. Pinch-to-zoom, pan, page navigation
5. SAF file picker + VIEW intent handling
6. `RecentFilesScreen` with Room persistence
7. Table of contents, password-protected PDF support
8. Night mode, dark mode, edge-to-edge display

### Phase 2: Annotations — "Mark up a PDF" (Weeks 5–8)
1. Text selection via `StructuredText`
2. Highlight, underline, strikeout, squiggly annotations
3. Freehand ink drawing with pressure sensitivity
4. Sticky notes, free text annotations
5. Shape tools (rectangle, circle, line, arrow, polygon)
6. Stamps (standard + custom)
7. Eraser, undo/redo (MuPDF journal)
8. Annotation list panel, XFDF export/import, incremental PDF save

### Phase 3: Forms, Signatures, Search (Weeks 9–12)
1. Interactive form filling (all widget types)
2. Form data export/import
3. Digital signature verification + certificate display
4. Handwritten signature pad + apply as stamp
5. Full-text search with highlighting and navigation
6. Security hardening: process isolation, encrypted storage, input validation

### Phase 4: Polish & Advanced Features (Weeks 13–16)
1. Two-page spread for tablets (`WindowSizeClass`)
2. Text reflow mode
3. Custom background colors, AMOLED dark mode
4. Page rotation, thumbnail overview grid
5. Keyboard shortcuts, accessibility (TalkBack)
6. Material 3 dynamic color theming
7. Print support, bookmark management, i18n
8. Tile-based rendering, Baseline Profile, R8 optimization, F-Droid metadata

---

## Build Configuration

- **Flavors**: `github` (GitHub releases), `fdroid` (F-Droid)
- **Min SDK**: 26 (Android 8.0)
- **Target/Compile SDK**: 35 (Android 15)
- **License**: AGPL v3 (required by MuPDF)
- **ProGuard**: Keep MuPDF JNI classes, Room entities, domain models

---

## Verification / Testing Plan

### Automated
- **Unit tests** (JUnit 5 + Mockito-Kotlin + Turbine): ViewModels, use cases, MuPDF wrapper (mocked)
- **Room tests**: In-memory DB, DAO queries, migration tests
- **Compose UI tests**: Navigation, annotation toolbar interactions, form filling
- **Target**: 80%+ coverage on domain logic

### Manual Smoke Tests
- Open PDF via SAF and via VIEW intent
- Open password-protected PDF
- Large PDF (1000+ pages) — smooth scrolling, <300MB memory
- Add/edit/delete each annotation type
- Fill a form, verify signatures
- Full-text search across document
- Save PDF with annotations, reopen to verify persistence
- Verify zero network activity (packet capture)
- Dark mode, tablet two-page layout, TalkBack

---

## Additional Features Inspired by BentoPDF

BentoPDF's feature set suggests several capabilities worth considering for future phases beyond the initial 16-week roadmap:

| BentoPDF Feature | OpenPDF Applicability | Priority |
|-----------------|----------------------|----------|
| PDF merge/split | Useful — combine or extract pages | Phase 5 |
| Page rotation/reorder | Already in Phase 4 | — |
| Watermarking | Add text/image watermarks | Phase 5 |
| PDF redaction | Security-sensitive content removal | Phase 5 |
| PDF metadata editing | Edit title/author/subject | Phase 5 |
| PDF compression | Reduce file size | Phase 5 |
| PDF/A conversion | Archival format compliance | Phase 5+ |
| OCR | Text recognition for scanned PDFs | Phase 5+ (requires Tesseract) |

These features align with BentoPDF's "toolkit" philosophy and could differentiate OpenPDF from simpler viewers.

---

## Key Sources

- [MuPDF](https://mupdf.com) — rendering engine
- [MuPDF Android Viewer](https://github.com/ArtifexSoftware/mupdf-android-viewer) — reference JNI integration
- [MuPDF PDFAnnotation API](https://mupdf.readthedocs.io/en/latest/reference/javascript/types/PDFAnnotation.html) — annotation types
- [GrapheneOS PdfViewer](https://github.com/GrapheneOS/PdfViewer) — security architecture reference
- [BentoPDF](https://github.com/alam00000/bentopdf) — feature breadth and privacy-first philosophy reference
- [AndroidX PDF](https://developer.android.com/jetpack/androidx/releases/pdf) — Jetpack PDF (reference only)
- [Foxit PDF Reader Android](https://www.foxit.com/pdf-reader/) — feature benchmark

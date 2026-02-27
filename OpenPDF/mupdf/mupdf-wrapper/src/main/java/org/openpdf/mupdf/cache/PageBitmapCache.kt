package org.openpdf.mupdf.cache

import android.graphics.Bitmap
import android.util.LruCache
import javax.inject.Inject
import javax.inject.Singleton

data class CacheKey(
    val pageIndex: Int,
    val zoom: Float,
    val colorInvert: Boolean = false,
)

@Singleton
class PageBitmapCache @Inject constructor() {

    companion object {
        private val MAX_CACHE_SIZE = (Runtime.getRuntime().maxMemory() / 8).toInt()
    }

    private val cache = object : LruCache<CacheKey, Bitmap>(MAX_CACHE_SIZE) {
        override fun sizeOf(key: CacheKey, value: Bitmap): Int = value.byteCount
    }

    fun get(key: CacheKey): Bitmap? = cache.get(key)

    fun put(key: CacheKey, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun evict(key: CacheKey) {
        cache.remove(key)
    }

    fun clear() {
        cache.evictAll()
    }
}

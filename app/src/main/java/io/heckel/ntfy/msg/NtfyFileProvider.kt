package io.heckel.ntfy.msg

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider

/**
 * Custom FileProvider that overrides getType() to return correct MIME types
 * for file extensions that Android's MimeTypeMap doesn't natively support
 * (e.g., .md → text/markdown, .html → text/html).
 *
 * This is necessary because Android's PackageManager, when resolving Intents
 * with content:// URIs, queries the ContentProvider's getType() rather than
 * using the explicitly set MIME type from Intent.setDataAndType().
 */
class NtfyFileProvider : FileProvider() {

    override fun getType(uri: Uri): String {
        val originalType = super.getType(uri)
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase()
        return when (extension) {
            "md", "markdown" -> "text/markdown"
            "html", "htm" -> "text/html"
            else -> originalType ?: "application/octet-stream"
        }
    }
}

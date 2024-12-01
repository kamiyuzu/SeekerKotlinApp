package com.seeker.sharemanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class ShareManager(
    private val context: Context,
) {
    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            flags += Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_TITLE, "Sharing Seeker:")
            type = MimeType.TEXT.toAndroidMimeType()
        }
        Log.println(Log.DEBUG, "shareText text", text)
        val choicer = Intent.createChooser(intent, null)
        context.startActivity(choicer)
    }

    fun shareFile(file: ShareFileModel) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, file.text)
            putExtra(Intent.EXTRA_TITLE, "Sharing Seeker asset:")
            putExtra(Intent.EXTRA_STREAM, file.uri)
            flags += Intent.FLAG_ACTIVITY_NEW_TASK
            flags += Intent.FLAG_GRANT_READ_URI_PERMISSION
            type = file.mime.toAndroidMimeType()
        }
        Log.println(Log.DEBUG, "shareFile uri", file.uri.toString())
        val choicer = Intent.createChooser(intent, null)
        context.startActivity(choicer)
    }
}

enum class MimeType {
    PDF,
    TEXT,
    IMAGE,
}

class ShareFileModel(
    val mime: MimeType = MimeType.IMAGE,
    val uri: Uri,
    val text: String,
)

private fun MimeType.toAndroidMimeType(): String = when (this) {
    MimeType.PDF -> "application/pdf"
    MimeType.TEXT -> "text/plain"
    MimeType.IMAGE -> "image/*"
}

@Composable
fun rememberShareManager(): ShareManager {
    val context = LocalContext.current
    return remember {
        ShareManager(context)
    }
}
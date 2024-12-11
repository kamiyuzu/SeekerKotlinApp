package com.seeker.views.details

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.seeker.R
import com.seeker.sharemanager.MimeType
import com.seeker.sharemanager.ShareFileModel
import com.seeker.sharemanager.rememberShareManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Random

@Composable
fun Share(text: String, context: Context, scope: CoroutineScope, imgBitmap: Bitmap?) {
    val randNo = Random().nextInt(100000)
    val cacheFile = File.createTempFile("IMG_$randNo", ".png", context.cacheDir)
    val shareManager = rememberShareManager()

    Button(onClick = {
        scope.launch {
            val stream = ByteArrayOutputStream()
            imgBitmap?.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            val imageByteArray = stream.toByteArray()
            cacheFile.writeBytes(imageByteArray)
            Log.println(Log.DEBUG, "Share", "Writing image into " + cacheFile.path + "...")
            val shared = ShareFileModel(
                uri = FileProvider.getUriForFile(context, context.packageName + ".provider", cacheFile),
                mime = MimeType.IMAGE,
                text = text,
            )
            shareManager.shareFile(shared)
        }
    }) {
        Icon(imageVector = Icons.Default.Share, contentDescription = null)
        Text(stringResource(R.string.share), modifier = Modifier.padding(start = 8.dp))
    }
}
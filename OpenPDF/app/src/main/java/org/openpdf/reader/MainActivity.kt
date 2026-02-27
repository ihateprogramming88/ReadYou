package org.openpdf.reader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import org.openpdf.core.ui.theme.OpenPdfTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var mInitialUri: Uri? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        mInitialUri = intent?.data
            ?: intent?.getParcelableExtra(Intent.EXTRA_STREAM)

        setContent {
            OpenPdfTheme {
                MainNavGraph(initialUri = mInitialUri)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        mInitialUri = intent.data
            ?: intent.getParcelableExtra(Intent.EXTRA_STREAM)
    }
}

package com.example.mindfulu.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.mindfulu.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [YoutubeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class YoutubeFragment : Fragment() {
    // Deklarasikan WebView di level class agar bisa diakses di onDestroyView
    private var webView: WebView? = null
    private var videoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ambil URL dari arguments
        arguments?.let {
            videoUrl = it.getString("video_url_key")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.fragment_youtube, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.youtube_webview)

        // Gunakan URL yang diterima, atau URL default jika tidak ada
        val urlToLoad = videoUrl ?: "http://www.youtube.com/watch?v=3Pd4LlFWypY"

        val htmlContent = """
            <html><body style="margin:0;padding:0;background-color:black;">
                <iframe width="100%" height="100%" src="$urlToLoad" frameborder="0" allowfullscreen></iframe>
            </body></html>
        """.trimIndent()

        webView?.apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            loadData(htmlContent, "text/html", "utf-8")
        }
    }

    // Praktik yang baik: Hancurkan WebView saat view dari fragment dihancurkan
    // untuk mencegah memory leak.
    override fun onDestroyView() {
        webView?.destroy()
        webView = null
        super.onDestroyView()
    }
}
package com.example.mindfulu.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.mindfulu.R

class YoutubeFragment : Fragment() {
    private var webView1: WebView? = null
    private var webView2: WebView? = null
    private var webView3: WebView? = null

    private var videoUrl1: String? = null
    private var videoUrl2: String? = null
    private var videoUrl3: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoUrl1 = it.getString("video_url_key_1")
            videoUrl2 = it.getString("video_url_key_2")
            videoUrl3 = it.getString("video_url_key_3")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_youtube, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView1 = view.findViewById(R.id.youtube_webview_1)
        webView2 = view.findViewById(R.id.youtube_webview_2)
        webView3 = view.findViewById(R.id.youtube_webview_3)

        // Gunakan URL yang diterima dari HomeFragment, atau URL default jika tidak ada
        setupWebView(webView1, videoUrl1 ?: "http://www.youtube.com/watch?v=3Pd4LlFWypY")
        setupWebView(webView2, videoUrl2 ?: "https://www.youtube.com/embed/$")
        setupWebView(webView3, videoUrl3 ?: "https://www.youtube.com/embed/dQw4w9WgXcQ")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView?, url: String) {
        webView?.apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            val htmlContent = """
                <html>
                <body style="margin:0;padding:0;background-color:black;">
                    <iframe width="100%" height="100%" src="$url" frameborder="0" allowfullscreen></iframe>
                </body>
                </html>
            """.trimIndent()
            loadData(htmlContent, "text/html", "utf-8")
        }
    }

    override fun onDestroyView() {
        webView1?.destroy()
        webView2?.destroy()
        webView3?.destroy()
        webView1 = null
        webView2 = null
        webView3 = null
        super.onDestroyView()
    }
}
package esos.MobiBench

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient


class Webview : Activity() {
    private var mWebView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mbwebview)
        setLayout()

        // 웹뷰에서 자바스크립트실행가능
        mWebView!!.settings.javaScriptEnabled = true
        if (DialogActivity.G_EXP_CHOICE == "Seq.Write") {
            mWebView!!.loadUrl("http://mobibench.dothome.co.kr/rank_seq_write.php?mysn=" + DialogActivity.dev_num)
        } else if (DialogActivity.G_EXP_CHOICE == "Seq.read") {
            mWebView!!.loadUrl("http://mobibench.dothome.co.kr/rank_seq_read.php?mysn=" + DialogActivity.dev_num)
        } else if (DialogActivity.G_EXP_CHOICE == "Rand.Write") {
            mWebView!!.loadUrl("http://mobibench.dothome.co.kr/rank_ran_write.php?mysn=" + DialogActivity.dev_num)
        } else if (DialogActivity.G_EXP_CHOICE == "Rand.Read") {
            mWebView!!.loadUrl("http://mobibench.dothome.co.kr/rank_ran_read.php?mysn=" + DialogActivity.dev_num)
        } else if (DialogActivity.G_EXP_CHOICE == "SQLite.Insert") {
            mWebView!!.loadUrl("http://mobibench.dothome.co.kr/rank_sqlite_insert.php?mysn=" + DialogActivity.dev_num)
        } else if (DialogActivity.G_EXP_CHOICE == "SQLite.Update") {
            mWebView!!.loadUrl("http://mobibench.dothome.co.kr/rank_sqlite_update.php?mysn=" + DialogActivity.dev_num)
        } else if (DialogActivity.G_EXP_CHOICE == "SQLite.Delete") {
            mWebView!!.loadUrl("http://mobibench.dothome.co.kr/rank_sqlite_delete.php?mysn=" + DialogActivity.dev_num)
        } else {
            mWebView!!.loadUrl("http://mobibench.co.kr/")
        }

        // WebViewClient 지정
        mWebView!!.webViewClient = WebViewClientClass()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView!!.canGoBack()) {
            mWebView!!.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private inner class WebViewClientClass : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

    /*
	 * Layout
	 */
    private fun setLayout() {
        mWebView = findViewById<View>(R.id.webview) as WebView
    }
}
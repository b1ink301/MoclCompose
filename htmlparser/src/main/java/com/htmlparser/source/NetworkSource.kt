package com.htmlparser.source

import androidx.annotation.WorkerThread
import org.jsoup.Jsoup
import org.jsoup.select.Elements

@WorkerThread
class NetworkSource(private val url: String) : Source {
    override fun get(): Elements =
        Jsoup.connect(url)
            .get()
            .body()
            .children()
}
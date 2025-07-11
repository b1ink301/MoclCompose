package com.htmlparser.source

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class StringSource(private val html: String) : Source {
    override fun get(): Elements =
        Jsoup.parse(html)
            .body()
            .children()
}
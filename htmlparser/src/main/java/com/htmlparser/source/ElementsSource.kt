package com.htmlparser.source

import org.jsoup.select.Elements

class ElementsSource(
    private val elements: Elements
): Source {
    override fun get(): Elements = elements
}
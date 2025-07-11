package com.htmlparser.extractor

import android.util.Log
import org.jsoup.nodes.Element

class ImageExtractor(val element: Element) : Extractor<Triple<String, Int, Int>>(element) {
    override fun extract(): Triple<String, Int, Int> {
        val url = element.attr("abs:src").ifBlank { element.attr("src") }
        val width = element.attr("width").toIntOrNull() ?: -1
        val height = element.attr("height").toIntOrNull() ?: -1

        Log.d("ImageExtractor", "Extracted image: $url ($width x $height)")
        return Triple(url, width, height)
    }
}
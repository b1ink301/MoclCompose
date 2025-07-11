package com.htmlparser.extractor

import org.jsoup.nodes.Element

class VideoExtractor(val element: Element) : Extractor<Triple<String, Int, Int>>(element) {
    override fun extract(): Triple<String, Int, Int> {
        val width = element.attr("width")
            .toIntOrNull() ?: -1
        val height = element.attr("height")
            .toIntOrNull() ?: -1

        println("VideoExtractor.extract() = width: $width, height: $height")

        val src = if (element.children()
                .isNotEmpty()
        )
            element
                .child(0)
                .attr("abs:src")
        else
            element.attr("abs:src")

        return Triple(src, width, height)
    }
}
package com.htmlparser

import com.htmlparser.elements.Element
import com.htmlparser.elements.ElementIdentifier
import com.htmlparser.source.Source

class HtmlParser private constructor() {
    data class Builder (
        private val source: Source,
        private val embeddedStyleEnabled: Boolean = true,
        private val callback: (list: List<Element>) -> Unit,
    ) {
        fun build() = HtmlParser()
            .also {
                val elements = source.get()
                val list = ArrayList<Element>()
                ElementIdentifier.extractData(
                    elementList = list,
                    elements = elements,
                    embeddedStyleEnabled = embeddedStyleEnabled
                )
                callback.invoke(list)
            }
    }
}
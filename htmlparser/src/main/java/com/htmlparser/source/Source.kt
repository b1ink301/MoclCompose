package com.htmlparser.source

import org.jsoup.select.Elements

interface Source {
    fun get(): Elements
}
package com.htmlparser.elements

import com.htmlparser.elements.Element.*
import com.htmlparser.extractor.AnchorLinkExtractor
import com.htmlparser.extractor.AudioExtractor
import com.htmlparser.extractor.DescriptionListExtractor
import com.htmlparser.extractor.FigureExtractor
import com.htmlparser.extractor.HeadingExtractor
import com.htmlparser.extractor.IFrameExtractor
import com.htmlparser.extractor.ImageExtractor
import com.htmlparser.extractor.ListExtractor
import com.htmlparser.extractor.TableExtractor
import com.htmlparser.extractor.VideoExtractor
import com.htmlparser.model.AnchorLink
import com.htmlparser.model.DescriptionList
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class ElementIdentifier(private val element: Element) {
    fun identify(): ElementType = when (element.tagName()) {
        "h1" -> ElementType.Heading1
        "h2" -> ElementType.Heading2
        "h3" -> ElementType.Heading3
        "h4" -> ElementType.Heading4
        "h5" -> ElementType.Heading5
        "h6" -> ElementType.Heading6
        "blockquote" -> ElementType.BlockQuote
        "p" -> ElementType.Paragraph
        "iframe" -> ElementType.IFrame
        "a" -> ElementType.AnchorLink
        "img" -> ElementType.Image
        "video" -> ElementType.Video
        "audio" -> ElementType.Audio
        "ol" -> ElementType.OrderedList
        "ul" -> ElementType.UnorderedList
        "dl" -> ElementType.DescriptionList
        "div" -> ElementType.Div
        "section" -> ElementType.Section
        "figure" -> ElementType.Figure
        "br" -> ElementType.Br
        "strong" -> ElementType.Strong
        "em" -> ElementType.Em
        "span" -> ElementType.Span
        "b" -> ElementType.B
        "table" -> ElementType.Table
        else -> ElementType.Unknown
    }

    companion object {
        @JvmStatic
        fun extractData(
            elementList: MutableList<com.htmlparser.elements.Element>,
            elements: Elements,
            data: String = "",
            embeddedStyleEnabled: Boolean = true
        ) {
            val wholeData = data
            elements.forEach { element ->
//                val element = it.selectFirst("video") ?: it
                when (ElementIdentifier(element).identify().also { type ->
                    println("extractData identify type=$type, html=${element.html()}")
                }) {
                    ElementType.Image -> {
                        val (url, with, height) = ImageExtractor(element).extract()
                        elementList.add(ImageElement(element.html(), url, with, height))
                    }
                    ElementType.Heading1 -> {
                        val heading = HeadingExtractor(element).extract()
                        elementList.add(Heading1Element(element.html(), heading))
                    }
                    ElementType.Heading2 -> {
                        val heading = HeadingExtractor(element).extract()
                        elementList.add(Heading2Element(element.html(), heading))
                    }
                    ElementType.Heading3 -> {
                        val heading = HeadingExtractor(element).extract()
                        elementList.add(Heading3Element(element.html(), heading))
                    }
                    ElementType.Heading4 -> {
                        val heading = HeadingExtractor(element).extract()
                        elementList.add(Heading4Element(element.html(), heading))
                    }
                    ElementType.Heading5 -> {
                        val heading = HeadingExtractor(element).extract()
                        elementList.add(Heading5Element(element.html(), heading))
                    }
                    ElementType.Heading6 -> {
                        val heading = HeadingExtractor(element).extract()
                        elementList.add(Heading6Element(element.html(), heading))
                    }
                    ElementType.UnorderedList -> {
                        val listExtractor = ListExtractor(element).extract()
                        elementList.add(UnOrderListElement(element.html(), listExtractor))
                    }
                    ElementType.OrderedList -> {
                        val listExtractor = ListExtractor(element).extract()
                        elementList.add(OrderListElement(element.html(), listExtractor))
                    }
                    ElementType.Video -> {
                        val (url, width, height) = VideoExtractor(element).extract()
                        elementList.add(VideoElement(html = element.html(), url = url, width = width, height = height))
                    }
                    ElementType.AnchorLink -> {
                        if (element.getElementsByTag("img").isNotEmpty()) {
                            extractData(elementList, element.children(), wholeData)
                        } else {
                            val anchorLink = AnchorLinkExtractor(element).extract()
                            elementList.add(
                                AnchorLinkElement(
                                    element.html(),
                                    AnchorLink(
                                        anchorLink.first,
                                        anchorLink.second
                                    )
                                )
                            )
                        }
                    }
                    ElementType.DescriptionList -> {
                        val extract = DescriptionListExtractor(element).extract()
                        val descriptionList = mutableListOf<DescriptionList>()
                        extract.forEach {
                            descriptionList.add(DescriptionList(it.first, it.second))
                        }
                        val descriptionListElement = DescriptionListElement(element.toString(), descriptionList)
                        elementList.add(descriptionListElement)
                    }
                    ElementType.Div -> {
                        val children = element.children()
                        if (children.isNotEmpty() &&
                            element.getElementsByClass("fb-post").isNotEmpty()
                        ) {
                            elementList.add(IFrameElement(element.toString(), element.toString()))
                        } else {
                            if (element.childrenSize() <= 0 && element.hasText()) {
                                if (!embeddedStyleEnabled)
                                    removeStyle(element)
                                elementList.add(ParagraphElement(element.toString()))
                            }
                            extractData(elementList, element.children(), wholeData)
                            // if has text need to be extracted!
                            // todo find a better approach
//                            if (it.hasText()) {
////                                val anchors = it.getElementsByTag("a")
////                                var divText = it.text()
////
////                                anchors.forEach {
////                                    val anchorLink = AnchorLinkExtractor(it).extract()
////                                    divText = divText.replace(anchorLink.first, it.toString(), true)
////                                }
////
////                                elementList.forEach {
////                                    if (it is ParagraphElement)
////                                        if (divText.contains(it.paragraph))
////                                            return
////                                }
////                                if (anchors.size > 0)
////                                    elementList.add(ParagraphElement(divText))
//                                if (!wholeData.contains(it.text(), true))
//                                    elementList.add(ParagraphElement(it.toString()))
//                                wholeData += it.toString()
//                            }
                        }
                    }
                    ElementType.Paragraph -> {
                        val children = element.children()

                        if (children.isNotEmpty() &&
                            element.getElementsByTag("img").isNotEmpty() ||
                            element.getElementsByTag("audio").isNotEmpty() ||
                            element.getElementsByTag("video").isNotEmpty() ||
                            element.getElementsByTag("iframe").isNotEmpty()
                        ) {
                            extractData(elementList, children)
                            if (element.hasText()) {
                                if (!embeddedStyleEnabled)
                                    removeStyle(element)
                                elementList.add(ParagraphElement(element.text()))
                            }
                        } else {
                            if (!embeddedStyleEnabled)
                                removeStyle(element)

                            if (element.html().isNotBlank())
                                elementList.add(ParagraphElement(element.html()))
                        }
                    }
                    ElementType.BlockQuote -> {
                        elementList.add(BlockQuoteElement(element.html(), element.text()))
                    }

                    ElementType.IFrame -> {
                        val iframe = IFrameExtractor(element).extract()
                        elementList.add(IFrameElement(element.html(), iframe))
                    }
                    ElementType.Audio -> {
                        val audio = AudioExtractor(element).extract()
                        elementList.add(AudioElement(element.html(), audio))
                    }
                    ElementType.Unknown -> {
                        val children = element.children()
                        if (children.isNotEmpty())
                            extractData(elementList, children, wholeData)
                        elementList.add(UnknownElement(element.html()))
                    }

                    ElementType.Section -> extractData(elementList, element.children(), wholeData)

                    ElementType.Figure -> {
                        if (element.getElementsByClass("wp-block-embed-youtube").isNotEmpty()) {
                            elementList.add(IFrameElement(element.html(), element.toString()))
                        } else {
                            val figure = FigureExtractor(element).extract()
                            elementList.add(FigureElement(element.html(), figure.first, figure.second))
                        }
                    }

                    ElementType.Br -> {
                        // bind it as Paragraph for now
                        if (element.hasText()) {
                            if (!embeddedStyleEnabled)
                                removeStyle(element)
//                            elementList.add(ParagraphElement(it.html()))
                            elementList.add(ParagraphElement("\n"))
                        }
                    }

                    ElementType.Youtube -> {
                        val (url, width, height) = VideoExtractor(element).extract()
                        elementList.add(VideoElement(html = element.html(), url = url, width = width, height = height))
                    }
                    ElementType.Table -> {
                        elementList.add(TableElement(element.html(), TableExtractor(element).extract()))
                    }

                    ElementType.Strong -> elementList.add(StrongElement(element.html()))
                    ElementType.Span -> {
                        val children = element.children()
                        if (children.isNotEmpty()) {
                            extractData(elementList, children)
                            if (children.hasText()) {
                                elementList.add(ParagraphElement(children.text()))
                            }
                        } else {
                            elementList.add(ParagraphElement(element.html()))
                        }
                    }
                    ElementType.B -> elementList.add(ParagraphElement(element.html()))
                    ElementType.Em -> {
                        val children = element.children()
                        if (children.isNotEmpty())
                            extractData(elementList, children, wholeData)
                        elementList.add(EmElement(element.html()))
                    }
                    ElementType.String -> elementList.add(ParagraphElement(element.html()))
                    else -> {
                        val children = element.children()
                        if (children.isNotEmpty())
                            extractData(elementList, children, wholeData)
                        elementList.add(UnknownElement(element.text()))
                    }
                }
            }
//            elementList.map { it::class.java.simpleName }
        }

        private fun removeStyle(it: Element?) {
            if (it!!.childrenSize() > 0) {
                for (child in it.children()) {
                    child.removeClass("style")
                    child.removeAttr("style")
                }
            }
            it.removeClass("style")
            it.removeAttr("style")

        }
    }
}
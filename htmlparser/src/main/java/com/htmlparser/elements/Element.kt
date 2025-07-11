package com.htmlparser.elements

import com.htmlparser.model.AnchorLink
import com.htmlparser.model.DescriptionList
import com.htmlparser.model.Table

sealed class Element(open val html: String, val type: ElementType) {
    data class DescriptionListElement(override val html: String, val descriptionList: List<DescriptionList>) :
        Element(html, ElementType.DescriptionList)

    data class OrderListElement(override val html: String, val list: Pair<String, List<String>>) :
        Element(html, ElementType.OrderedList)

    data class ImageElement(override val html: String, val imageUrl: String, val width: Int, val height: Int) :
        Element(html, ElementType.Image)

    data class ParagraphElement(override val html: String) : Element(html, ElementType.Paragraph)
    data class UnOrderListElement(override val html: String, val list: Pair<String, List<String>>) :
        Element(html, ElementType.UnorderedList)

    data class Heading1Element(override val html: String, val text: String) : Element(html, ElementType.Heading1)
    data class Heading2Element(override val html: String, val text: String) : Element(html, ElementType.Heading2)
    data class Heading3Element(override val html: String, val text: String) : Element(html, ElementType.Heading3)
    data class Heading4Element(override val html: String, val text: String) : Element(html, ElementType.Heading4)
    data class Heading5Element(override val html: String, val text: String) : Element(html, ElementType.Heading5)
    data class Heading6Element(override val html: String, val text: String) : Element(html, ElementType.Heading6)
    data class VideoElement(
        override val html: String, val url: String, val videoThumbnailUrl: String = "", val width: Int, val height: Int
    ) : Element(html, ElementType.Video)

    data class AudioElement(override val html: String, val url: String) : Element(html, ElementType.Audio)
    data class AnchorLinkElement(override val html: String, val anchorUrl: AnchorLink) :
        Element(html, ElementType.AnchorLink)

    data class BlockQuoteElement(override val html: String, val text: String) : Element(html, ElementType.BlockQuote)
    data class IFrameElement(override val html: String, val url: String) : Element(html, ElementType.IFrame)
    data class FigureElement(override val html: String, val caption: String, val url: String) :
        Element(html, ElementType.Figure)

    data class TableElement(override val html: String, val table: Table) : Element(html, ElementType.Table)
    data class UnknownElement(override val html: String) : Element(html, ElementType.Unknown)
    data class EmElement(override val html: String) : Element(html, ElementType.Em)
    data class StrongElement(override val html: String) : Element(html, ElementType.Strong)
}

//sealed class Paragraph {
//    data class Body(val bodyText: String) : Paragraph()
//    data class AnchorLinkInParagraph(val text: String, val url: String) : Paragraph()
//    data class UnderLine(val text: String) : Paragraph()
//    data class Bold(val text: String) : Paragraph()
//    data class Emphasizes(val text: String) : Paragraph()
//    data object Unknown : Paragraph()
//}

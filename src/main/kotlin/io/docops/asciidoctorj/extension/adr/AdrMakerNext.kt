package io.docops.asciidoctorj.extension.adr

import io.docops.asciidoctorj.extension.adr.model.Adr

class AdrMakerNext {
    private val xIndent = 66

    fun makeAdrSvg(adr: Adr, dropShadow: Boolean = true, config: AdrParserConfig) : String {
        val width = 550 + config.increaseWidthBy
        val sb = StringBuilder()

        sb.append(title(adr.title, width))
        sb.append(status(adr, config))
        sb.append("""<text x="14" y="85" style="font-weight: normal; font-size: 11px;">""")
        sb.append(context(adr,config))
        sb.append(decision(adr,config))
        sb.append(consequences(adr,config))
        sb.append(participants(adr,config))
        sb.append("""</text>""")
        var iHeight = 550
        val count = adr.lineCount() - 15
        if(count>0) {
            iHeight += (count * 15)
        }
        return svg(sb.toString(), iWidth = width,  iHeight = iHeight, adr = adr, config=config)
    }

    fun title(title: String, width: Int): String {
        return  """
    <text x="${width/2}" y="30" text-anchor="middle" fill="#000" opacity="0.25" style="font-weight: bold; font-size: 16px;">$title</text>
    <text x="${width/2}" y="27.75" text-anchor="middle" style="font-weight: bold; font-size: 16px;">$title</text>
        """.trimIndent()
    }

    fun status(adr: Adr, adrParserConfig: AdrParserConfig): String {
        //language=svg
        return """
            <text x="20" y="55" style="font-weight: bold; font-size: 12px;" fill="#000000">Status:</text>
            <text x="77" y="55" style="font-weight: normal; font-size: 12px;" fill="#000000">${adr.status}</text>
            <text x="200" y="55" style="font-weight: bold; font-size: 12px;" fill="#000000">Date:</text>
            <text x="245" y="55"  style="font-size: 12px;" fill="#000000">${adr.date}</text>
        """
    }

    fun context(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20" style="font-weight: bold; font-size: 12px;"  text-decoration="underline">Context</tspan>""")
        adr.context.forEach {  s ->
            sb.append("""<tspan x="14" dy="20">$s</tspan>""")
        }
        return sb
    }
    fun decision(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20" style="font-weight: bold; font-size: 12px;"  text-decoration="underline">Decision</tspan>""")
        adr.decision.forEach {  s ->
            sb.append("""<tspan x="14" dy="20">$s</tspan>""")
        }
        return sb
    }
    fun consequences(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20" style="font-weight: bold; font-size: 12px;"  text-decoration="underline">Consequences</tspan>""")
        adr.consequences.forEach {  s ->
            sb.append("""<tspan x="14" dy="20">$s</tspan>""")
        }
        return sb
    }
    fun participants(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20" style="font-weight: bold; font-size: 12px;"  text-decoration="underline">Participants</tspan>""")
        adr.participants.forEach {  s ->
            sb.append("""<tspan x="14" dy="20">$s</tspan>""")
        }
        return sb
    }
    fun svg(body: String, iHeight: Int = 550, iWidth: Int, adr: Adr, config: AdrParserConfig): String {
        val height = maxOf(iHeight, 500)
        val width = iWidth + config.increaseWidthBy
        //language=svg
        return """
<?xml version="1.0" standalone="no"?>
<svg id="adr" xmlns="http://www.w3.org/2000/svg"
     xmlns:xlink="http://www.w3.org/1999/xlink" font-family="arial" viewBox="0 0 $width $height"
     clip="rect(0, 5, 8, 2)">
    <defs>
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Proposed-gradient" x2="1" y2="1">
            <stop offset="0%" stop-color="#94c2e5"/>
            <stop offset="50%" stop-color="#5ea4d8"/>
            <stop offset="100%" stop-color="#2986cc"/>
        </linearGradient>
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Accepted-gradient" x2="1" y2="1">
            <stop offset="0%" stop-color="#9bba8e"/>
            <stop offset="50%" stop-color="#699855"/>
            <stop offset="100%" stop-color="#38761d"/>
        </linearGradient>
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Superseded-gradient" x2="1" y2="1">
            <stop offset="0%" stop-color="#fae1a1"/>
            <stop offset="50%" stop-color="#f7d272"/>
            <stop offset="100%" stop-color="#F5C344"/>
        </linearGradient>        
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Deprecated-gradient" x2="1" y2="1">
            <stop offset="0%" stop-color="#f4cccc"/>
            <stop offset="50%" stop-color="#efb2b2"/>
            <stop offset="100%" stop-color="#EA9999"/>
        </linearGradient>        
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Rejected-gradient" x2="1" y2="1">
            <stop offset="0%" stop-color="#e5a1a4"/>
            <stop offset="50%" stop-color="#d87277"/>
            <stop offset="100%" stop-color="#CB444A"/>
        </linearGradient>
        <filter id="dropshadow" height="130%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="3"/> <!-- stdDeviation is how much to blur -->
            <feOffset dx="2" dy="2" result="offsetblur"/> <!-- how much to offset -->
            <feComponentTransfer>
                <feFuncA type="linear" slope="0.5"/> <!-- slope is the opacity of the shadow -->
            </feComponentTransfer>
            <feMerge>
                <feMergeNode/> <!-- this contains the offset blurred image -->
                <feMergeNode in="SourceGraphic"/> <!-- this contains the element that the filter is applied to -->
            </feMerge>
        </filter>

    </defs>
    <style>
    .adrlink {
            fill: blue; /* Even for text, SVG uses fill over color */
            text-decoration: underline;
        }

        .adrlink:hover, .adrlink:active {
            outline: dotted 1px blue;
        }
    </style>
   
   <path d="${generateRectPathData(width.toFloat(), height.toFloat(), 22.0F, 22.0F,0.0F,0.0F)}" fill="#ffffff"  stroke="url(#${adr.status}-gradient)"/>

    <path d="${generateRectPathData(width.toFloat(), 70f, 22.0F, 22.0F,0.0F,0.0F)}" fill="url(#${adr.status}-gradient)" filter="url(#dropshadow)" />
   
    $body
</svg>
        """.trimIndent()
    }
}
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
    <text x="${width/2}" y="30" text-anchor="middle" fill="#fcfcfc"  class="glass"  style="font-weight: bold; font-size: 16px;">$title</text>
         """.trimIndent()
    }

    fun status(adr: Adr, adrParserConfig: AdrParserConfig): String {
        //language=svg
        return """
            <text x="20" y="55"  class="glass"  style="font-size: 12px;" fill="#000000">Status:</text>
            <text x="77" y="55"  style="font-weight: normal; font-size: 12px;" fill="#fcfcfc">${adr.status}</text>
            <text x="200" y="55"  class="glass"  style="font-size: 12px;" fill="#000000">Date:</text>
            <text x="245" y="55"  style="font-size: 12px;" fill="#fcfcfc">${adr.date}</text>
        """
    }

    fun context(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20"  class="glass" style="font-size: 12px;"  text-decoration="underline">Context</tspan>""")
        adr.context.forEach {  s ->
            if(s.isEmpty()) {
                sb.append("""<tspan x="14" dy="20">&#160;</tspan>""")
            } else {
                sb.append("""<tspan x="14" dy="20">$s</tspan>""")
            }
        }
        return sb
    }
    fun decision(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20" class="glass" style="font-size: 12px;"  text-decoration="underline">Decision</tspan>""")
        adr.decision.forEach {  s ->
            if(s.isEmpty()) {
                sb.append("""<tspan x="14" dy="20">&#160;</tspan>""")
            } else {
                sb.append("""<tspan x="14" dy="20">$s</tspan>""")
            }
        }
        return sb
    }
    fun consequences(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20" class="glass" style="font-size: 12px;"  text-decoration="underline">Consequences</tspan>""")
        adr.consequences.forEach {  s ->
            if(s.isEmpty()) {
                sb.append("""<tspan x="14" dy="20">&#160;</tspan>""")
            } else {
                sb.append("""<tspan x="14" dy="20">$s</tspan>""")
            }
        }
        return sb
    }
    fun participants(adr: Adr, config: AdrParserConfig): StringBuilder {
        val sb = StringBuilder("""<tspan x="14" dy="20" class="glass" style="font-size: 12px;"  text-decoration="underline">Participants</tspan>""")
        adr.participants.forEach {  s ->
            sb.append("""<tspan x="14" dy="20">$s</tspan>""")
        }
        return sb
    }
    fun svg(body: String, iHeight: Int = 550, iWidth: Int, adr: Adr, config: AdrParserConfig): String {
        val height = maxOf(iHeight, 500)
        val width = (iWidth + config.increaseWidthBy)
        //language=svg
        return """
<?xml version="1.0" standalone="no"?>
<svg id="adr" xmlns="http://www.w3.org/2000/svg" width='${(width+35)* config.scale}' height='${(height + 45)*config.scale}'
     xmlns:xlink="http://www.w3.org/1999/xlink" font-family="arial" viewBox="0 0 ${(width+10)* config.scale} ${height* config.scale}"
     >
    <defs>
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Proposed-gradient" x2="0%" y2="100%">
            <stop offset="100%" stop-color="#2986cc"/>
            <stop offset="50%" stop-color="#5ea4d8"/>
            <stop offset="0%" stop-color="#94c2e5"/>
        </linearGradient>
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Accepted-gradient" x2="0%" y2="100%">
            <stop offset="100%" stop-color="#38761d"/>
            <stop offset="50%" stop-color="#699855"/>
            <stop offset="0%" stop-color="#9bba8e"/>
        </linearGradient>
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Superseded-gradient" x2="0%" y2="100%">
            <stop offset="100%" stop-color="#F5C344"/>
            <stop offset="50%" stop-color="#f7d272"/>
            <stop offset="0%" stop-color="#fae1a1"/>
        </linearGradient>        
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Deprecated-gradient" x2="0%" y2="100%">
            <stop offset="100%" stop-color="#EA9999"/>
            <stop offset="50%" stop-color="#efb2b2"/>
            <stop offset="0%" stop-color="#f4cccc"/>
        </linearGradient>        
        <linearGradient xmlns="http://www.w3.org/2000/svg" id="Rejected-gradient" x2="0%" y2="100%">
            <stop offset="100%" stop-color="#CB444A"/>
            <stop offset="50%" stop-color="#d87277"/>
            <stop offset="0%" stop-color="#e5a1a4"/>
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
<filter xmlns="http://www.w3.org/2000/svg" id="MyFilter">
            <feGaussianBlur in="SourceAlpha" stdDeviation="4" result="blur"/>
            <feOffset in="blur" dx="4" dy="4" result="offsetBlur"/>
            <feSpecularLighting in="blur" surfaceScale="5" specularConstant="1" specularExponent="10"
                                lighting-color="white" result="specOut">
                <fePointLight x="-5000" y="-10000" z="20000"/>
            </feSpecularLighting>
            <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut"/>
            <feComposite in="SourceGraphic" in2="specOut" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                         result="litPaint"/>
            <feMerge>
                <feMergeNode in="offsetBlur"/>
                <feMergeNode in="litPaint"/>
            </feMerge>
        </filter>
        <filter id="buttonBlur">
            <feGaussianBlur in="SourceAlpha" stdDeviation="2" result="blur"/>
            <feOffset in="blur" dy="2" result="offsetBlur"/>
            <feMerge>
                <feMergeNode in="offsetBlur"/>
                <feMergeNode in="SourceGraphic"/>
            </feMerge>
        </filter>
        <linearGradient id="overlayGrad" gradientUnits="userSpaceOnUse" x1="95" y1="-20" x2="95" y2="70">
            <stop offset="0" stop-color="#000000" stop-opacity="0.5"/>
            <stop offset="1" stop-color="#000000" stop-opacity="0"/>
        </linearGradient>
        <filter id="topshineBlur">
            <feGaussianBlur stdDeviation="0.93"/>
        </filter>
        <linearGradient id="topshineGrad" gradientUnits="userSpaceOnUse" x1="95" y1="0" x2="95" y2="40">
            <stop offset="0" stop-color="#ffffff" stop-opacity="1"/>
            <stop offset="1" stop-color="#ffffff" stop-opacity="0"/>
        </linearGradient>
        <filter id="bottomshine">
            <feGaussianBlur stdDeviation="0.95"/>
        </filter>
        <filter id="Bevel2" filterUnits="objectBoundingBox" x="-10%" y="-10%" width="150%" height="150%">
            <feGaussianBlur in="SourceAlpha" stdDeviation="0.5" result="blur"/>
            <feSpecularLighting in="blur" surfaceScale="5" specularConstant="0.5" specularExponent="10" result="specOut"
                                lighting-color="white">
                <fePointLight x="-5000" y="-10000" z="0000"/>
            </feSpecularLighting>
            <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut2"/>
            <feComposite in="SourceGraphic" in2="specOut2" operator="arithmetic" k1="0" k2="1" k3="1" k4="0"
                         result="litPaint"/>
        </filter>
    </defs>
    <style>
    .adrlink { fill: blue; text-decoration: underline; }
    .adrlink:hover, .adrlink:active { outline: dotted 1px blue; }
        
    ${glassStyle()}
    </style>
    <g transform='translate(5,5),scale(${config.scale})'>
   <path d="${generateRectPathData(width.toFloat(), height.toFloat(), 22.0F, 22.0F,22.0F,22.0F)}" fill="#ffffff"  stroke="url(#${adr.status}-gradient)" stroke-width='3'/>
   <path d="${generateRectPathData(width.toFloat(), 70f, 22.0F, 22.0F,0.0F,0.0F)}" fill="url(#${adr.status}-gradient)" filter="url(#dropshadow)" />
   <path d="${generateRectPathData(width.toFloat(), 70f, 22.0F, 22.0F,0.0F,0.0F)}" fill="url(#${adr.status}-gradient)" filter="url(#buttonBlur)" />
   
    $body
    </g>
</svg>
        """.trimIndent()
    }

    fun glassStyle() = """
        .glass {
            overflow: hidden;
            color: white;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.7);
            background-image: radial-gradient(circle at center, rgba(0, 167, 225, 0.25), rgba(0, 110, 149, 0.5));
            box-shadow: 0 5px 10px rgba(0, 0, 0, 0.75), inset 0 0 0 2px rgba(0, 0, 0, 0.3), inset 0 -6px 6px -3px rgba(0, 129, 174, 0.2);
            position: relative;
        }

        .glass:after {
            content: "";
            background: rgba(0, 167, 225, 0.2);
            display: block;
            position: absolute;
            z-index: 0;
            height: 100%;
            width: 100%;
            top: 0;
            left: 0;
            backdrop-filter: blur(3px) saturate(400%);
            -webkit-backdrop-filter: blur(3px) saturate(400%);
        }

        .glass:before {
            content: "";
            display: block;
            position: absolute;
            width: calc(100% - 4px);
            height: 35px;
            background-image: linear-gradient(rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0));
            top: 2px;
            left: 2px;
            border-radius: 30px 30px 200px 200px;
            opacity: 0.7;
        }

        .glass:hover {
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.9);
        }

        .glass:hover:before {
            opacity: 1;
        }

        .glass:active {
            text-shadow: 0 0 2px rgba(0, 0, 0, 0.9);
            box-shadow: 0 3px 8px rgba(0, 0, 0, 0.75), inset 0 0 0 2px rgba(0, 0, 0, 0.3), inset 0 -6px 6px -3px rgba(0, 129, 174, 0.2);
        }

        .glass:active:before {
            height: 25px;
        }
    """.trimIndent()
}
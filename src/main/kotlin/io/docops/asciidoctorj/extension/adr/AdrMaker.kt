/*
 * Copyright 2020 The DocOps Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.docops.asciidoctorj.extension.adr

import io.docops.asciidoctorj.extension.adr.model.Adr
import io.docops.asciidoctorj.extension.adr.model.Status


class AdrMaker {

    private val xIndent = 32
    fun makeAdrSvg(adr: Adr, dropShadow: Boolean = true) : String {
        val sb = StringBuilder()
        sb.append(makeTitle(title = adr.title))
        sb.append(makeDateAndStatus(adr = adr))
        var startY = 140
        sb.append(makeContext(adr = adr, startY))
        startY += (adr.context.size * 30) + 10

        sb.append(makeDecision(adr = adr, startY))
        startY += (adr.decision.size * 30) + 10

        sb.append(makeConsequences(adr = adr, startY))
        startY += (adr.consequences.size * 30) + 10

        sb.append(makeParticipants(adr=adr, startY))
        startY += (adr.participants.size * 30) + 20

        return svg(sb.toString(), startY, dropShadow,adr)
    }

    private fun makeTitle(title: String): String {
        // language=svg
        return """<text id="adrTitle" x="485" y="40" text-anchor="middle">$title</text>""".trimIndent()
    }

    private fun makeDateAndStatus(adr: Adr): String {
        // language=svg
        return """
    <text x="$xIndent" y="80" class="contextline">
        <tspan class="status">Date: </tspan>
    <tspan class="content"> ${adr.date}</tspan>
    </text>
    <text x="$xIndent" y="110">
        <tspan class="status">Status: </tspan>
        <tspan fill="${adr.status.color(adr.status)}" font-weight="bold" font-size="14px" font-family="'Noto Sans', sans-serif">${adr.status}</tspan>
    </text>
        """
    }

    private fun makeContext(adr: Adr, startY: Int ) : String {
        // language=svg
        var text = """
             <text x="$xIndent" y="$startY">
             <tspan class="status">Context: </tspan>"""

        adr.context.forEachIndexed { index, s ->
            // language=svg
            if(index==0) {
                text += """<tspan class="content"> ${s}</tspan>"""
            }else{
           text += """<tspan x="$xIndent" dy="30" class="content"> ${s}</tspan>"""
            }
        }
        text += """</text>"""
        return text
    }

    private fun makeDecision(adr: Adr, startY: Int): String {
        // language=svg
        var text = """
            <text x="$xIndent" y="$startY">
            <tspan class="status">Decision: </tspan>
        """
        // language=svg
        adr.decision.forEachIndexed { index, s ->
            if(index==0) {
                text += """<tspan class="content"> $s</tspan>"""
            } else{
                text += """<tspan x="$xIndent" dy="30" class="content">$s</tspan>"""
            }
        }
        text += "</text>"
        return text
    }
    private fun makeConsequences(adr: Adr, startY: Int) : String {
        // language=svg
        var text = """
            <text x="$xIndent" y="$startY">
        <tspan class="status">Consequences: </tspan>
        """
        // language=svg
        adr.consequences.forEachIndexed { index, s ->
            if(index == 0) {
                text += """<tspan class="content">$s</tspan>"""
            } else {
                text += """<tspan x="$xIndent" dy="30" class="content">$s</tspan>"""
            }
        }
        text += """</text>"""
        return text
    }
    private fun makeParticipants(adr: Adr, startY: Int) : String{
        // language=svg
        var text = ""
        if (adr.participants.isNotEmpty()) {
            // language=svg
         text += """
            <text x="$xIndent" y="$startY">
        <tspan class="status">Participants: </tspan>
        """
        adr.participants.forEachIndexed { index, s ->
            if(index == 0) {
                text += """<tspan class="content">$s</tspan>"""
            }else {
                // language=svg
                text += """<tspan x="$xIndent" dy="30" class="content"> $s</tspan>"""
            }
        }
        text += """</text>"""
        }
        return text.trimIndent()
    }
    fun svg(body: String, iHeight: Int = 550, dropShadow: Boolean = true, adr: Adr): String {
        val startY = 100
        val statusHeight = 80
        val height = maxOf(iHeight, 500)
        var filter = ""
        if(dropShadow) {
            filter = """filter="url(#dropShadow)""""
        }
        // language=svg
        return """
<?xml version="1.0" standalone="no"?>
<svg xmlns="http://www.w3.org/2000/svg" width="970" height="$height"
    xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 1050 ${height+30}"
     >
    <defs>
         <filter id="dropShadow" height="112%">
             <feGaussianBlur in="SourceAlpha" stdDeviation="1"/>
             <feOffset dx="5" dy="3" result="offsetblur"/>
             <feComponentTransfer>
                 <feFuncA type="linear" slope="0.1"/>
             </feComponentTransfer>
             <feMerge>
                 <feMergeNode/>
                 <feMergeNode in="SourceGraphic"/>
             </feMerge>
         </filter>
     </defs>
    
    <style>
        #adrTitle {
            font: bold 18px "Noto Sans",sans-serif;
            fill: #25286A;
        }

        .status {
            font: bold 14px "Noto Sans",sans-serif;
        }
        .content {
            font: normal 14px "Noto Sans",sans-serif;
        }
        .context {
            font: bold 14px "Noto Sans",sans-serif;
        }
        .contextline {
            font: normal 14px "Noto Sans",sans-serif;
        }
        .subtitle {
            font: bold 14px "Noto Sans",sans-serif;
            fill: white;
        }
        .unselected {
            opacity: 0.4;
        }
        #adrlink:link, #adrlink:visited {
            cursor: pointer;
        }


        .adrlink {
            fill: blue; /* Even for text, SVG uses fill over color */
            text-decoration: underline;
        }

        .adrlink:hover, .adrlink:active {
            outline: dotted 1px blue;
        }
        #adrRect {
            fill: #ffffff;
        }
        .adrText {
            writing-mode: tb;
            font: bold 12px "Noto Sans",sans-serif;
            fill: #fffef7;
        }
    </style>
    <rect id="adrRect" x="21" y="20" width="970" height="97%" rx="5" ry="5" stroke="#16537e" $filter/>
    <rect id="Proposed" x="0" y="$startY" width="20" height="80" fill="${adr.status.determineStatusColor(adr.status, Status.Proposed)}" filter="url(#dropShadow)"/>
    <rect id="Accepted" x="0" y="${startY+statusHeight}" width="20" height="80" fill="${adr.status.determineStatusColor(adr.status, Status.Accepted)}" filter="url(#dropShadow)"/>
    <rect id="Superceded" x="0" y="${startY+statusHeight*2}" width="20" height="80" fill="${adr.status.determineStatusColor(adr.status, Status.Superseded)}" filter="url(#dropShadow)"/>
    <rect id="Deprecated" x="0" y="${startY+statusHeight*3}" width="20" height="80" fill="${adr.status.determineStatusColor(adr.status, Status.Deprecated)}" filter="url(#dropShadow)"/>
    <rect id="Rejected" x="0" y="${startY+statusHeight*4}" width="20" height="80" fill="${adr.status.determineStatusColor(adr.status, Status.Rejected)}" filter="url(#dropShadow)"/>

    <text x="10" y="${startY + 10}" class="adrText"  >Proposed</text>
    <text x="10" y="${startY+statusHeight + 10}" class="adrText" >Accepted</text>
    <text x="10" y="${startY+statusHeight*2 +5}" class="adrText">Superseded</text>
    <text x="10" y="${startY+statusHeight*3 + 5}" class="adrText">Deprecated</text>
    <text x="10" y="${startY+statusHeight*4 + 10}" class="adrText">Rejected</text>
    $body
    </svg>
    """.trimIndent()
    }
}
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
    fun makeAdrSvg(adr: Adr, dropShadow: Boolean = true, config: AdrParserConfig) : String {
        val sb = StringBuilder()
        sb.append(makeTitle(title = adr.title))
        sb.append(makeDateAndStatus(adr = adr, config))
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

    private fun makeDateAndStatus(adr: Adr, config: AdrParserConfig): String {
        var status = ""
        var img = ""
        if(config.isPdf) {
             status = """<tspan fill="${adr.status.color(adr.status)}" font-weight="bold" font-size="14px" font-family="'Noto Sans', sans-serif">${adr.status}</tspan>"""
        } else {
            img = """<g><image x="80" y="68" height="75" width="75" href="${makeButton(adr.status, true)}"/></g>"""
        }
        // language=svg
        return """
    <text x="$xIndent" y="80" class="contextline">
        <tspan class="status">Date: </tspan>
    <tspan class="content"> ${adr.date}</tspan>
    </text>
    <text x="$xIndent" y="110">
        <tspan class="status">Status: </tspan>
        $status
    </text>
        $img
        """
    }

    private fun makeContext(adr: Adr, startY: Int ) : String {
        // language=svg
        var text = """
             <text x="$xIndent" y="$startY">
             <tspan class="status">Context: </tspan>"""

        adr.context.forEachIndexed { index, s ->
            // language=svg
            text += if(index==0) {
                """<tspan class="content"> ${s}</tspan>"""
            }else{
                """<tspan x="$xIndent" dy="30" class="content"> ${s}</tspan>"""
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
            text += if(index==0) {
                """<tspan class="content"> $s</tspan>"""
            } else{
                """<tspan x="$xIndent" dy="30" class="content">$s</tspan>"""
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
            text += if(index == 0) {
                """<tspan class="content">$s</tspan>"""
            } else {
                """<tspan x="$xIndent" dy="30" class="content">$s</tspan>"""
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
            text += if(index == 0) {
                """<tspan class="content">$s</tspan>"""
            }else {
                // language=svg
                """<tspan x="$xIndent" dy="30" class="content"> $s</tspan>"""
            }
        }
        text += """</text>"""
        }
        return text.trimIndent()
    }
    fun svg(body: String, iHeight: Int = 550, dropShadow: Boolean = true, adr: Adr): String {
        val statusHeight = 80
        val height = maxOf(iHeight, 500)
        val startY = height - 50

        var filter = ""
        if(dropShadow) {
            filter = """filter="url(#dropShadow)""""
        }
        // language=svg
        return """
<?xml version="1.0" standalone="no"?>
<svg xmlns="http://www.w3.org/2000/svg" 
    xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 1100 ${height+120}"
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
         <filter id="inset-shadow" x="-50%" y="-50%" width="200%" height="200%">
            <feComponentTransfer in="SourceAlpha">
                <feFuncA type="table" tableValues="1 0" />
            </feComponentTransfer>
            <feGaussianBlur stdDeviation="1"/>
            <feOffset dx="0" dy="2" result="offsetblur"/>
            <feFlood flood-color="rgb(20, 0, 0)" result="color"/>
            <feComposite in2="offsetblur" operator="in"/>
            <feComposite in2="SourceAlpha" operator="in" />
            <feMerge>
                <feMergeNode in="SourceGraphic" />
                <feMergeNode />
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
        
        @keyframes draw {
            0% {
                stroke-dasharray: 140 560;
                stroke-dashoffset: -474;
                stroke-width: 3px;
            }
            100% {
                stroke-dasharray: 970;
                stroke-dashoffset: 0;
                stroke-width: 3px;
            }
        }

        .shape {
            stroke: #000000;
            filter: drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4))
        }
        
        .shape:hover {
            -webkit-animation: 1.5s draw linear forwards;
            animation: 1.5s draw linear forwards;

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
            font: bold 10px "Noto Sans",sans-serif;
            fill: #fffef7;
        }
        .yellowToBlack {
            fill: #000000;
        }
    </style>
    <rect id="adrRect" x="21" y="20" width="97%" height="85%" rx="5" ry="5" stroke="#16537e" $filter/>
    $body
    <g><image x="300" y="$startY" height="75" width="75" href="${makeGreyButton(adr.status, Status.Proposed)}"/></g>
    <g><image x="370" y="$startY" height="75" width="75" href="${makeGreyButton(adr.status, Status.Accepted)}"/></g>
    <g><image x="440" y="$startY" height="75" width="75" href="${makeGreyButton(adr.status, Status.Superseded)}"/></g>
    <g><image x="510" y="$startY" height="75" width="75" href="${makeGreyButton(adr.status, Status.Deprecated)}"/></g>
    <g><image x="580" y="$startY" height="75" width="75" href="${makeGreyButton(adr.status, Status.Rejected)}"/></g>
    </svg>
    """.trimIndent()
    }
}
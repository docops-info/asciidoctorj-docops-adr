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


class AdrMaker {

    fun makeAdrSvg(adr: Adr, dropShadow: Boolean = true) : String {
        val sb = StringBuilder()
        sb.append(makeTitle(title = adr.title))
        sb.append(makeButtons(adr = adr))
        sb.append(makeDateAndStatus(adr = adr))
        var startY = 220
        sb.append(makeContext(adr = adr, startY))
        startY += (adr.context.size * 30) + 40
        sb.append(makeDecision(adr = adr, startY))
        startY += (adr.decision.size * 30) + 40
        sb.append(makeConsequences(adr = adr, startY))
        startY += (adr.consequences.size * 30) + 50
        return svg(sb.toString(), startY, dropShadow)
    }

    private fun makeTitle(title: String): String {
        return """
     <text x="485" y="40" class="title" text-anchor="middle">$title</text>
     <line x1="20" y1="45" x2="970" y2="45" stroke="#8B8B90" />
    """.trimIndent()
    }
    private fun makeButtons(adr: Adr): String {

        return """
     <g>
        <rect x="90" y="55" fill="#568BBF" width="150" height="30" rx="5" ry="5" class="${adr.statusClass(adr.status,"Proposed")}"/>
        <text x="165" y="75" text-anchor="middle" class="subtitle">Proposed</text>
    </g>
    <g>
        <rect x="250" y="55" fill="#53A551" width="150" height="30" rx="5" ry="5" class="${adr.statusClass(adr.status,"Accepted")}"/>
        <text x="325" y="75" text-anchor="middle" class="subtitle">Accepted</text>
    </g>
    <g>
        <rect x="410" y="55" fill="#F5C344" width="150" height="30" rx="5" ry="5" class="${adr.statusClass(adr.status,"Superseded")}"/>
        <text x="485" y="75" text-anchor="middle" class="subtitle">Superseded</text>
    </g>
    <g>
        <rect x="570" y="55" fill="#F5C344" width="150" height="30" rx="5" ry="5" class="${adr.statusClass(adr.status,"Deprecated")}"/>
        <text x="645" y="75" text-anchor="middle" class="subtitle">Deprecated</text>
    </g>
    <g>
        <rect x="730" y="55" fill="#CB444A" width="150" height="30" rx="5" ry="5" class="${adr.statusClass(adr.status,"Rejected")}"/>
        <text x="805" y="75" text-anchor="middle" class="subtitle">Rejected</text>
    </g>
        """.trimIndent()
    }
    private fun makeDateAndStatus(adr: Adr): String {
        return """
    <text x="20" y="110" class="contextline">
        <tspan>Date: </tspan>
    <tspan class="content">${adr.date}</tspan>
    </text>
    <text x="20" y="140">
        <tspan class="status">Status </tspan>
        <tspan x="20" dy="30" class="content">${adr.status}</tspan>
    </text>
    <line x1="20" y1="145" x2="970" y2="145" stroke="#8B8B90" />
        """.trimIndent()
    }

    private fun makeContext(adr: Adr, startY: Int ) : String {
        var text = """
             <text x="20" y="$startY">
             <tspan class="status">Context</tspan>"""

        adr.context.forEach {
           text += """
               <tspan x="20" dy="30" class="content">${it.trim()}</tspan>
            """
        }
        text += """</text>
            <line x1="20" y1="${startY+5}" x2="970" y2="${startY+5}" stroke="#8B8B90" />
        """
        return text.trimIndent()
    }

    private fun makeDecision(adr: Adr, startY: Int): String {
        var text = """
            <text x="20" y="$startY">
            <tspan class="status">Decision</tspan>
        """
        adr.decision.forEach {
            text += """<tspan x="20" dy="30" class="content">$it</tspan>"""
        }
        text += """</text>
            <line x1="20" y1="${startY+5}" x2="970" y2="${startY+5}" stroke="#8B8B90" />
        """
        return text.trimIndent()
    }
    private fun makeConsequences(adr: Adr, startY: Int) : String {
        var text = """
            <text x="20" y="$startY">
        <tspan class="status">Consequences</tspan>
        """
        adr.consequences.forEach {
            text += """
                <tspan x="20" dy="30" class="content">$it</tspan>
            """
        }
        text += """</text>
            <line x1="20" y1="${startY+5}" x2="970" y2="${startY+5}" stroke="#8B8B90" />
        """
        return text.trimIndent()
    }
    fun svg(body: String, height: Int = 550, dropShadow: Boolean): String {
        var filter =  ""
        if(dropShadow) {
            filter = """filter="url(#dropShadow)""""
        }
        return """
<?xml version="1.0" standalone="no"?>
<svg xmlns="http://www.w3.org/2000/svg" width="970" height="$height"
     viewBox="0 0 1000 ${height+30}">
    <filter id="dropShadow" height="130%">
        <feGaussianBlur in="SourceAlpha" stdDeviation="6"/>
        <feOffset dx="5" dy="5" result="offsetblur"/>
        <feComponentTransfer>
            <feFuncA type="linear" slope="0.9"/>
        </feComponentTransfer>
        <feMerge>
            <feMergeNode/>
            <feMergeNode in="SourceGraphic"/>
        </feMerge>
    </filter>
    <style>
        .title {
            font: bold 36px "Noto Sans",sans-serif;
            fill: #28293D;
        }

        .status {
            font: bold 18px "Noto Sans",sans-serif;
        }
        .content {
            font: normal 14px "Noto Sans",sans-serif;
        }
        .context {
            font: bold 18px "Noto Sans",sans-serif;
        }
        .contextline {
            font: normal 14px "Noto Sans",sans-serif;
        }
        .subtitle {
            font: bold 18px "Noto Sans",sans-serif;
            fill: white;
        }
        .unselected {
            opacity: 0.4;
        }
    </style>
    <rect id="myRect" x="10" y="0" width="970" height="97%" rx="5" ry="5"  fill="#fffefa"  class="card" $filter/>
    $body
    </svg>
        """.trimIndent()
    }
}
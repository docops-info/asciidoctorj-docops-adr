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

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader
import java.io.File

@Name("adr")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class AdrBlockProcessor : BlockProcessor() {
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val filename = attributes.getOrDefault("2", "${System.currentTimeMillis()}_unk") as String
        val border = attributes.getOrDefault("border", "false") as String
        val content = reader.read()
        val parser = ADRParser()
        val config = AdrParserConfig()
        val imgSrc = try {
            val adr = parser.parse(content = content, config = config)
            val adrMaker = AdrMaker()
            var srcStr = adrMaker.makeAdrSvg(adr, border.toBoolean())
            adr.urlMap.forEach { (t, u) ->
                srcStr = srcStr.replace("_${t}_", u)
            }
            srcStr
        } catch (e: Exception) {
            errorReport(e.message, config = config)
        }
        val imgDir = parent.document.getAttribute("imagesdir")
        var target = "images/${filename}.svg"
        if (imgDir != null) {
            target = "${filename}.svg"
        }
        val svg = File("${reader.dir}/images/${filename}.svg")
        val p = svg.parentFile
        if(!p.exists()) {
            p.mkdirs()
        }
        svg.writeBytes(imgSrc.toByteArray())
        val blockAttrs = mutableMapOf<String, Any>(
            "role" to "docops.io.adr",
            "target" to target,
            "alt" to "IMG not available",
            "title" to "Figure. $filename",
            "interactive-option" to "",
            "format" to "svg"

        )
        return createBlock(parent, "image", ArrayList(), blockAttrs, HashMap())
    }

    private fun errorReport(msg: String?, config: AdrParserConfig): String {
        msg?.let {
            val lines = msg.addLinebreaks(config.lineSize)
            // language=svg
            var svg = """ 
<?xml version="1.0" standalone="no"?>
<svg xmlns="http://www.w3.org/2000/svg" width="970" height="550"
 viewBox="0 0 1000 550">
<style>
    .title {
        font: bold 36px "Noto Sans",sans-serif;
        fill: #ff0000;
    }
    .content {
        font: bold 14px "Noto Sans",sans-serif;
    }
</style>
<rect id="myRect" x="10" y="0" width="970" height="100%" rx="5" ry="5" class="card" stroke="#d2ddec" fill="#ffffff"/>
<text x="485" y="40" class="title" text-anchor="middle" >ADR Parse Failure</text>
<line x1="10" y1="45" x2="970" y2="45" stroke="#d2ddec" />
 <text y="60">
                """
            lines.forEach {
                // language=svg
                svg += """
<tspan x="20" dy="14" class="content">$it</tspan>
                    """
            }
            svg += """</text>
            </svg>
            """
            return svg.trimIndent()
        }
        return ""
    }
}
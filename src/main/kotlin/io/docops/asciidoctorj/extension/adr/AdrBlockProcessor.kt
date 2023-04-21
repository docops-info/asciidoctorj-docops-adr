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

import org.asciidoctor.ast.Block
import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader
import org.asciidoctor.log.LogRecord
import org.asciidoctor.log.Severity
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.text.DecimalFormat
import java.time.Duration
import java.util.*
import java.util.zip.GZIPOutputStream
import kotlin.collections.HashMap

@Name("adr")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class AdrBlockProcessor : BlockProcessor() {
    private var server = "http://localhost:8010/extension"
    private var webserver = "http://localhost:8010/extension"
    private var localDebug = false
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val remoteServer = parent.document.attributes["panel-server"]
        if (remoteServer != null) {
            remoteServer as String
            server = remoteServer
        }
        val remoteWebserver = parent.document.attributes["panel-webserver"]
        remoteWebserver?.let {
            webserver = it as String
        }
        val debug = parent.document.attributes["local-debug"]
        if (debug != null) {
            debug as String
            localDebug = debug.toBoolean()
        }
        val filename = attributes.getOrDefault("2", "${System.currentTimeMillis()}_unk") as String
        val border = attributes.getOrDefault("border", "false") as String
        val newWin = attributes.getOrDefault("newWin", defaultValue = "false") as String
        val content = reader.read()
        var width = attributes.getOrDefault("width", "") as String
        val boxWidthIncrease = attributes.getOrDefault("boxWidthIncrease", "50") as String
        val lineSize = attributes.getOrDefault("lineSize", "90") as String
        val role = attributes.getOrDefault("role", "center")
        val table = attributes.getOrDefault("table", "false") as String
        val backend = parent.document.getAttribute("backend") as String
        val isPdf = "pdf" == backend
        val config = AdrParserConfig(
            newWin = newWin.toBoolean(),
            isPdf = isPdf,
            lineSize = lineSize.toInt(),
            increaseWidthBy = boxWidthIncrease.toInt()
        )
        val idea = parent.document.getAttribute("env", "") as String

        if (serverPresent(server, parent, this, localDebug)) {
            val payload: String = try {
                compressString(content)
            } catch (e: Exception) {
                log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
                ""
            }
            var widthNum = 970
            if (width.isNotEmpty()) {
                val pct = BigDecimal(width.substring(0, width.length - 1))
                val fact = pct.divide(BigDecimal(100))
                widthNum = fact.multiply(BigDecimal(widthNum)).intValueExact()
            }
            val url = "$server/api/adr?type=${isPdf}&data=$payload&increaseWidth=$boxWidthIncrease&file=xyz.svg"
            val svgBlock = createBlock(parent, "open", "", HashMap(), HashMap<Any, Any>())
            if (table.toBoolean()) {
                val linesArray = mutableListOf<String>()
                // language=asciidoc
                linesArray.add("""[cols="1",role="$role",$width,frame="none"]""")
                linesArray.add("|===")
                linesArray.add("")
                linesArray.add("a|image::$url[format=svg,width=\"$widthNum\",role=\"$role\",opts=\"inline\",align=\"$role\"]")
                linesArray.add("")
                linesArray.add("|===")
                parseContent(svgBlock, linesArray)
            } else {
                val image = getContentFromServer(url, parent, this, debug = localDebug)
                val dataUri = "data:image/svg+xml;base64," + Base64.getEncoder()
                    .encodeToString(image.toByteArray())
                val imageBlock = produceBlock(dataUri, filename, parent, widthNum.toString(), role)
                svgBlock.blocks.add(imageBlock)
            }

            return svgBlock


        }



        return null
    }

    private fun produceBlock(
        dataSrc: String,
        filename: String,
        parent: StructuralNode,
        width: String,
        role: Any
    ): Block {

        val svgMap = mutableMapOf<String, Any>(
            "role" to "center",
            "align" to "$role",
            "width" to width,
            "target" to dataSrc,
            "alt" to "IMG not available",
            "title" to "Figure. $filename",
            "opts" to "interactive",
            "format" to "svg"
        )
        return this.createBlock(parent, "image", "", svgMap, HashMap())
    }

    private fun errorReport(msg: String?, config: AdrParserConfig): String {
        msg?.let {
            val lines = msg.addLinebreaks(config.lineSize)
            // language=svg
            var svg = """<?xml version="1.0" standalone="no"?>
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

fun serverPresent(server: String, parent: StructuralNode, pb: BlockProcessor, debug: Boolean = false): Boolean {
    if (debug) {
        println("Checking if server is present ${server}/api/ping")
    }
    val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(20))
        .build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create("$server/api/ping"))
        .timeout(Duration.ofMinutes(1))
        .build()
    return try {
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        (200 == response.statusCode())
    } catch (e: Exception) {
        pb.log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
        e.printStackTrace()
        false
    }
}

fun getContentFromServer(url: String, parent: StructuralNode, pb: BlockProcessor, debug: Boolean = false): String {
    if (debug) {
        println("getting image from url $url")
    }
    val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(20))
        .build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(Duration.ofMinutes(1))
        .build()
    return try {
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        response.body()
    } catch (e: Exception) {
        pb.log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
        e.printStackTrace()
        ""
    }
}

fun compressString(body: String): String {
    val baos = ByteArrayOutputStream()
    val zos = GZIPOutputStream(baos)
    zos.use { z ->
        z.write(body.toByteArray())
    }
    val bytes = baos.toByteArray()
    return Base64.getUrlEncoder().encodeToString(bytes)
}
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
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*
import java.util.zip.GZIPOutputStream

@Name("adr")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class AdrBlockProcessor : BlockProcessor() {
    private var server = "http://localhost:8010/extension"
    private var webserver = "http://localhost:8010/extension"
    private var localDebug = false
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val debug = parent.document.attributes["local-debug"]
        if (debug != null) {
            debug as String
            localDebug = debug.toBoolean()
        }
        val content = subContent(reader, parent, localDebug)
        val remoteServer = parent.document.attributes["panel-server"]
        remoteServer?.let {
            server = remoteServer as String
        }
        val remoteWebserver = parent.document.attributes["panel-webserver"]
        remoteWebserver?.let {
            webserver = it as String
        }
        val scale = attributes.getOrDefault("scale", "1.0") as String
        val block: Block = createBlock(parent, "open", null as String?)
        val title = attributes.getOrDefault("title", "") as String
        val backend = parent.document.getAttribute("backend") as String
        val role = attributes.getOrDefault("role", "center") as String
        val idea = parent.document.getAttribute("env", "") as String
        val ideaOn = "idea".equals(idea, true)
        val lineSize = attributes.getOrDefault("lineSize", "90") as String
        val increaseWidth = attributes.getOrDefault("increaseWidth", "80") as String

        val newWin = attributes.getOrDefault("newWin", defaultValue = "false") as String
        if (serverPresent(server, parent, this, localDebug)) {
            var type = "SVG"
            if ("pdf" == backend) {
                type = "PDF"
            }
            val payload: String = try {
                compressString(content)
            } catch (e: Exception) {
                log(LogRecord(Severity.ERROR, parent.sourceLocation, e.message))
                ""
            }
            var opts = "format=svg,opts=inline,align='$role'"
            if (ideaOn) {
                opts = ""
            }
            val lines = mutableListOf<String>()
            if (ideaOn) {
                val url = "$webserver/api/adr?type=SVG&data=$payload&increaseWidth=$increaseWidth&title=${title.encodeUrl()}&scale=$scale&file=xyz.svg"
                val image = getContentFromServer(url, parent, this, debug = localDebug)
                return createImageBlockFromString(parent, image, role, "970")
            } else {
                val url = "image::$webserver/api/adr?type=SVG&data=$payload&title=${title.encodeUrl()}&scale=$scale&increaseWidth=$increaseWidth&file=xyz.svg[$opts]"
                if (localDebug) {
                    println(url)
                }
                lines.addAll(url.lines())
                parseContent(block, lines)
            }
        }
        return block
    }

    private fun createImageBlockFromString(parent: StructuralNode, svg: String, role: String, width: String): Block {

        val align = mutableMapOf(
            "right" to "margin-left: auto; margin-right: 0;",
            "left" to "",
            "center" to "margin: auto;"
        )
        val center = align[role.lowercase()]
        val content: String = """
            <div class="openblock">
            <div class="content" style="width: $width;padding: 10px;$center">
            $svg
            </div>
            </div>
        """.trimIndent()
        return createBlock(parent, "pass", content)
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
    val client =
        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(20)).build()
    val request = HttpRequest.newBuilder().uri(URI.create("$server/api/ping")).timeout(Duration.ofMinutes(1)).build()
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
    val client =
        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(20)).build()
    val request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMinutes(1)).build()
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

fun subContent(reader: Reader, parent: StructuralNode, debug: Boolean = false): String {
    val content = reader.read()
    return subs(content, parent, debug)
}

fun subs(content: String, parent: StructuralNode, debug: Boolean = false): String {
    val pattern = """#\[.*?]""".toRegex()
    val res = pattern.findAll(content)
    var localContent = content
    res.forEach {
        val subValue = parent.document.attributes[it.value.replace("#[", "").replace("]", "").lowercase()]
        val key = it.value
        if (debug) {
            println("Text Substitution for $key & value to replace $subValue")
        }
        if (subValue != null) {
            subValue as String
            localContent = localContent.replace(key, subValue)
            if (debug) {
                println("content after substituting $key -> $localContent")
            }
        }
    }
    return localContent
}
fun String.encodeUrl(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
}
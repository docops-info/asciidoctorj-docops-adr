package io.docops.asciidoctorj.extension.adr

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Name("adrsummary")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class AdrSummaryBlockProcessor : BlockProcessor() {
    private var server = "http://localhost:8010/extension"
    private var webserver = "http://localhost:8010/extension"
    private var localDebug = false
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val debug = parent.document.attributes["local-debug"]
        if (debug != null) {
            debug as String
            localDebug = debug.toBoolean()
        }

        val content = reader.readLines()
        val lines = startTable()

        try {
            content.forEach {
                    item ->
                if (item.startsWith("http")) {
                    when(val result = parseAdrFromUrl(item)){
                        is Result.Success -> {
                            lines.addAll(getRow(result.data, parent))
                        }
                        is Result.Failure -> {
                            lines.addAll(result.error.lines())
                        }
                    }

                } else {
                    val f = File(reader.dir, item)
                    if (f.exists()) {
                        lines.addAll(getRow(f.readText(), parent))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        lines.addAll(closeTable())
        return createBlock(parent, "open", lines)
    }

    private fun getRow(str: String, parent: StructuralNode): MutableList<String> {
        val lines = mutableListOf<String>()
        val comp = compressString(str)
        val row = getAdrRowFromSource(comp, parent, this)
        val data = row.split("~")
        val img = """
.View ADR Details
[%collapsible%]
====
image::$webserver/api/docops/svg?kind=adr&payload=$comp&scale=1.0&outlineColor=&title=&numChars=&type=SVG&useDark=false&filename=docops.svg[format=svg,opts=inline,align='center']
====
        """.trimIndent()
        System.lineSeparator()
        lines.add("|${data[0]} ")
        lines.add("a|  ")
        lines.addAll(img.lines())
        lines.add("|${data[1]}")
        lines.add("|${data[2]}")
        return lines
    }
    private fun parseAdrFromUrl(url: String): Result<String, String> {
        val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(1))
            .build()
        try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            return if (200 == response.statusCode()) {
                Result.Success(response.body())
            } else {
                Result.Failure("4+|$url does not exist.")
            }
        } catch (e: Exception) {
            return Result.Failure("4+|$url does not exist")
        }
    }


    private fun startTable(): MutableList<String> {
        val lines = mutableListOf<String>()
        lines.add("[options=header,cols=\"1,1,1,1\"]")
        lines.add("|===")
        //title, status, participants and date
        lines.add("|Title |ADR Contents |Participants |Date")
        return lines
    }

    private fun closeTable(): MutableList<String> {
        val lines = mutableListOf<String>()
        lines.add("|===")
        return lines
    }

    private fun getAdrRowFromSource(source: String, parent: StructuralNode, pb: BlockProcessor): String {
        val url = "$server/api/adr/summary/table?payload=$source"
        val content = getContentFromServer(url,  parent, pb, localDebug)
        return content
    }
}

sealed interface Result<out T, out E> {
    data class Success <out T> (val data: T) : Result<T, Nothing>
    data class Failure <out E> (val error: E): Result<Nothing, E>
}

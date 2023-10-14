package io.docops.asciidoctorj.extension.adr

import io.docops.asciidoctorj.extension.adr.model.Adr
import io.docops.asciidoctorj.extension.adr.model.Status
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
import java.util.*

@Name("adrsummary")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class AdrSummaryBlockProcessor : BlockProcessor() {
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val backend = parent.document.getAttribute("backend") as String

        val content = reader.readLines()
        val lines = startTable()
        content.forEach { item ->
            if (item.startsWith("http")) {
                lines.addAll(adrFromUrl(item, backend))
            } else {
                val f = File(reader.dir, item)
                if (f.exists()) {
                    lines.addAll(adrFromFile(f, backend))
                }
            }
        }
        lines.addAll(closeTable())
        return createBlock(parent, "open", lines)
    }

    private fun adrFromFile(f: File, backend: String): MutableList<String> {
        val lines = mutableListOf<String>()

        val adr = ADRParser().parse(f.readText(), AdrParserConfig(lineSize = 75, increaseWidthBy = 10))
        val div = if("pdf" == backend) {
            "${adr.status}"
        } else {
            buildDiv(adr)
        }
        lines.add("a|${adr.title} |  +++ $div +++ |${adr.participantAsStr()} |${adr.date}")

        //lines.add("a|${adr.title} |${adr.status} |${adr.participantAsStr()} |${adr.date}")
        return lines
    }

    private fun startTable(): MutableList<String> {
        val lines = mutableListOf<String>()
        lines.add("[options=header,cols=\"1,1,1,1\"]")
        lines.add("|===")
        //title, status, participants and date
        lines.add("|Title |Status |Participants |Date")
        return lines
    }

    private fun closeTable(): MutableList<String> {
        val lines = mutableListOf<String>()
        lines.add("|===")
        return lines
    }

    private fun adrFromUrl(url: String, backend: String): MutableList<String> {
        val client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(20))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(1))
            .build()
        val lines = mutableListOf<String>()

        try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (200 == response.statusCode()) {
                val adr = ADRParser().parse(response.body(), AdrParserConfig(lineSize = 70, increaseWidthBy = 80))
                val div = if("pdf" == backend) {
                   adr.status
                } else {
                    buildDiv(adr)
                }
                lines.add("a|${adr.title} | +++ $div +++ |${adr.participantAsStr()} |${adr.date}")
            } else {
                lines.add("4+|$url does not exist.")
            }
        } catch (e: Exception) {
            lines.add("4+|server for $url does not exist. ${e.message}")
        }

        return lines
    }
    private fun buildDiv(adr: Adr): String {
        var svg = AdrMakerNext().makeAdrSvg(adr, config = AdrParserConfig(newWin = false, lineSize = 70, increaseWidthBy = 80))
        adr.urlMap.forEach { (t, u) ->
            svg = svg.replace("_${t}_", u)
        }
        val str = Base64.getEncoder().encodeToString(svg.toByteArray())
        val now = System.currentTimeMillis()
        //language=html
        val imageStr = """
        <object type="image/svg+xml" height="900" width="580" data="data:image/svg+xml;base64,$str"></object>
        """.trimIndent()
        return """
            <div id="mod$now">
                <input id='button$now' type='checkbox'>
                <label for='button$now' style='cursor: pointer; color: ${adr.status.color(adr.status)}; font-weight: bold;'>${makeButton((adr.status))}</label>
                <div class='modal'>
                    <div class='adrcontent'><img src='data:image/svg+xml;base64,$str' width='580'></div>
                </div>
            </div>
        """.trimIndent()
    }


}

fun makeButton(status: Status,asXml: Boolean = false) : String {
    //language=svg
    val svg =  """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" viewBox="0 0 300 100">
                <defs id="defs4">
                    <linearGradient id="linearGradient3159">
                        <stop id="stop3163" style="stop-color:#000000;stop-opacity:0" offset="0"/>
                        <stop id="stop3161" style="stop-color:#000000;stop-opacity:0.5" offset="1"/>
                    </linearGradient>
                    <linearGradient id="linearGradient3030">
                        <stop id="stop3032" style="stop-color:#ffffff;stop-opacity:1" offset="0"/>
                        <stop id="stop3034" style="stop-color:#ffffff;stop-opacity:0" offset="1"/>
                    </linearGradient>
                    <linearGradient x1="120" y1="10" x2="120" y2="50" id="linearGradient3113" xlink:href="#linearGradient3030" gradientUnits="userSpaceOnUse"/>
                    <radialGradient cx="120" cy="170" r="100" fx="120" fy="170" id="radialGradient3165" xlink:href="#linearGradient3159" gradientUnits="userSpaceOnUse" gradientTransform="matrix(0,-0.72727275,2,0,-220,170)"/>
                </defs>
                <style type="text/css">
                    rect[id="ButtonBase"] { fill: red; }
                    svg[aria-pressed="true"] rect[id="ButtonBase"] { fill: green; }
                    g[id="layer1"]:hover {cursor: pointer}
                    g[id="layer1"]:hover rect[id="ButtonGlow"] {opacity: 0; }
                </style>
                <g id="layer1">
                    <rect width="280" height="80" ry="40" x="10" y="10" id="ButtonBase" style="fill:${status.color(status)};stroke:none"/>
                    <rect width="280" height="80" ry="40" x="10" y="10" id="ButtonGlow" style="fill:url(#radialGradient3165);stroke:none"/>
                    <text x="150" y="66" id="text3194" text-anchor="middle" style="font-size:40px;fill:#000000;stroke:none;font-family:DejaVu Sans"><tspan x="150" y="66" text-anchor="middle" id="tspan3196">${status}</tspan></text>
                    <text x="150" y="64.5" id="text3198"  text-anchor="middle" style="font-size:40px;fill:#ffffff;stroke:none;font-family:DejaVu Sans"><tspan x="150" text-anchor="middle" y="64.5" id="tspan3200">${status}</tspan></text>
                    <path d="m 50,15 200,0 c 11.08,0 22.51667,10.914 20,20 C 208.16563,41.622482 201.08,40 190,40 L 50,40 C 38.92,40 31.834332,41.622512 30,35 27.483323,25.914 38.92,15 50,15 z" id="ButtonHighlight" style="fill:url(#linearGradient3113)"/>
                </g>
            </svg>
        """.trimIndent()
    val str = Base64.getEncoder().encodeToString(svg.toByteArray())
    if(asXml) {
        return "data:image/svg+xml;base64,$str"
    }
    return """
            <img src="data:image/svg+xml;base64,$str" height="75" width="75"></img>
        """.trimIndent()
}
fun makeGreyButton(status: Status, row: Status): String {
    var color = "#cccccc"
    if(status == row) {
        color =  status.color(status)
    }
    val svg =  """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" viewBox="0 0 300 100">
                <defs id="defs4">
                    <linearGradient id="linearGradient3159">
                        <stop id="stop3163" style="stop-color:#000000;stop-opacity:0" offset="0"/>
                        <stop id="stop3161" style="stop-color:#000000;stop-opacity:0.5" offset="1"/>
                    </linearGradient>
                    <linearGradient id="linearGradient3030">
                        <stop id="stop3032" style="stop-color:#ffffff;stop-opacity:1" offset="0"/>
                        <stop id="stop3034" style="stop-color:#ffffff;stop-opacity:0" offset="1"/>
                    </linearGradient>
                    <linearGradient x1="120" y1="10" x2="120" y2="50" id="linearGradient3113" xlink:href="#linearGradient3030" gradientUnits="userSpaceOnUse"/>
                    <radialGradient cx="120" cy="170" r="100" fx="120" fy="170" id="radialGradient3165" xlink:href="#linearGradient3159" gradientUnits="userSpaceOnUse" gradientTransform="matrix(0,-0.72727275,2,0,-220,170)"/>
                </defs>
                <style type="text/css">
                    rect[id="ButtonBase"] { fill: red; }
                    svg[aria-pressed="true"] rect[id="ButtonBase"] { fill: green; }
                    g[id="layer1"]:hover {cursor: pointer}
                    g[id="layer1"]:hover rect[id="ButtonGlow"] {opacity: 0; }
                </style>
                <g id="layer1">
                    <rect width="280" height="80" ry="40" x="10" y="10" id="ButtonBase" style="fill:$color;stroke:none"/>
                    <rect width="280" height="80" ry="40" x="10" y="10" id="ButtonGlow" style="fill:url(#radialGradient3165);stroke:none"/>
                    <text x="150" y="66" id="text3194" text-anchor="middle" style="font-size:40px;fill:#000000;stroke:none;font-family:DejaVu Sans"><tspan x="150" y="66" text-anchor="middle" id="tspan3196">${row}</tspan></text>
                    <text x="150" y="64.5" id="text3198"  text-anchor="middle" style="font-size:40px;fill:#ffffff;stroke:none;font-family:DejaVu Sans"><tspan x="150" text-anchor="middle" y="64.5" id="tspan3200">${row}</tspan></text>
                    <path d="m 50,15 200,0 c 11.08,0 22.51667,10.914 20,20 C 208.16563,41.622482 201.08,40 190,40 L 50,40 C 38.92,40 31.834332,41.622512 30,35 27.483323,25.914 38.92,15 50,15 z" id="ButtonHighlight" style="fill:url(#linearGradient3113)"/>
                </g>
            </svg>
        """.trimIndent()
    val str = Base64.getEncoder().encodeToString(svg.toByteArray())
    return "data:image/svg+xml;base64,$str"
}
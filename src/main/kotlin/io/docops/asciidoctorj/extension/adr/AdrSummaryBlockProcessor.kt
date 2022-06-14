package io.docops.asciidoctorj.extension.adr

import io.docops.asciidoctorj.extension.adr.model.Adr
import org.asciidoctor.ast.Block
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
        val content = reader.readLines()
        val lines = startTable()
        content.forEach { item ->
            if (item.startsWith("http")) {
                lines.addAll(adrFromUrl(item))
            } else {
                val f = File(reader.dir, item)
                if (f.exists()) {
                    lines.addAll(adrFromFile(f))
                }
            }
        }
        lines.addAll(closeTable())
        val argAttributes: MutableMap<String, Any> = HashMap()
        argAttributes["content_model"] = ":raw"
        val block: Block = createBlock(parent, "open", "", argAttributes, HashMap<Any, Any>())
        //language=html
        val jsFun = """
            <style>
            .modal {
            background: dodgerblue;
            height: 1px;
            overflow: hidden;
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            transition: width 0.5s ease 0.5s, height 0.5s ease;
            width: 0;
        }

        .adrcontent {
            color: transparent;
            font-family: "Consolas", arial, sans-serif;
            font-size: 2em;
            position: absolute;
            top: 50%;
            left: 10%;
            text-align: center;
            transform: translate3d(0, -50%, 0);
            transition: color 0.5s ease;
            width: 100%;
        }



        input {
            cursor: pointer;
            height: 0;
            opacity: 0;
            width: 0;
        }
        input:focus {
            outline: none;
        }

        input:checked {
            height: 40px;
            opacity: 1;
            position: fixed;
            right: 20px;
            top: 20px;
            z-index: 1;
            -webkit-appearance: none;
            width: 40px;
        }
        input:checked::after, input:checked:before {
            border-top: 1px solid #FFF;
            content: "";
            display: block;
            position: absolute;
            top: 50%;
            transform: rotate(45deg);
            width: 100%;
        }
        input:checked::after {
            transform: rotate(-45deg);
        }

        input:checked + label {
            color: #FFF;
            transition: color 0.5s ease;
        }

        input:checked ~ .modal {
            height: 100%;
            width: 100%;
            transition: width 0.5s ease, height 0.5s ease 0.5s;
        }
        input:checked ~ .modal .adrcontent {
            color: #FFF;
            transition: color 0.5s ease 0.5s;
        }
            </style>
            <script>
            function hideSVG(id) {
              var style = document.getElementById(id).style.display;
              if(style === "none")
                document.getElementById(id).style.display = "block";
              else
                document.getElementById(id).style.display = "none";
              //or to hide the all svg
              //document.getElementById("mySvg").style.display = "none";
            }
            </script>
        """.trimIndent()
        block.blocks.add(createBlock(parent, "pass","""
    $jsFun
        """.trimIndent()))

        val tblBlock = (createBlock(parent, "open", lines))
        parseContent(block, tblBlock.lines)
        return block
    }

    private fun adrFromFile(f: File): MutableList<String> {
        val lines = mutableListOf<String>()

        val adr = ADRParser().parse(f.readText(), AdrParserConfig())
        val div = buildDiv(adr)
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

    private fun adrFromUrl(url: String): MutableList<String> {
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
                val adr = ADRParser().parse(response.body(), AdrParserConfig())
                val div = buildDiv(adr)
                lines.add("a|${adr.title} |  +++ $div +++ |${adr.participantAsStr()} |${adr.date}")
            } else {
                lines.add("4+|$url does not exist.")
            }
        } catch (e: Exception) {
            lines.add("4+|server for $url does not exist. ${e.message}")
        }

        return lines
    }
    private fun buildDiv(adr: Adr): String {
        var svg = (AdrMaker().makeAdrSvg(adr))
        adr.urlMap.forEach { (t, u) ->
            svg = svg.replace("_${t}_", u)
        }
        val str = Base64.getEncoder().encodeToString(svg.toByteArray())
        val now = System.currentTimeMillis()
        val imageStr = """<img src="data:image/svg+xml;base64,$str"></img>"""
        return """
                    <div id="mod$now">
                        <input id='button$now' type='checkbox'>
                        <label for='button$now'>${adr.status}</label>
                        <div class='modal'>
                            <div class='adrcontent'>$imageStr</div>
                        </div>
                    </div>
                """.trimIndent()
    }
}
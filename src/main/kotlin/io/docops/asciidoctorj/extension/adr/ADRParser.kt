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
import io.docops.asciidoctorj.extension.adr.model.escapeXml
import java.io.File
import java.util.*

class ADRParser {

    fun parse(content: String, config: AdrParserConfig = AdrParserConfig()): Adr {
        val lines = content.lines()
        val m = mutableMapOf<String, MutableList<String>>()
        var value = mutableListOf<String>()
        val urlMap = mutableMapOf<Int, String>()
        var currKey = ""
        lines.forEach { aline ->
            val line = aline.makeUrl(urlMap, config)
            val key: String
            if (line.contains(":")) {
                if (currKey.isNotEmpty()) {
                    m[currKey.uppercase()] = value
                }
                value = mutableListOf<String>()
                val split = line.split(":", limit = 2)
                key = split[0].trim()
                currKey = key
                value.add(split[1])
            } else {
                value.add(line)
            }
        }
        if (!m.containsKey(currKey)) {
            m[currKey.uppercase()] = value
        }
        return mapToAdr(m, config, urlMap)
    }

    private fun mapToAdr(
        map: MutableMap<String, MutableList<String>>,
        config: AdrParserConfig,
        urlMap: MutableMap<Int, String>
    ): Adr {
        val title = mapTitle(map)
        val date = mapDate(map)
        val status = mapStatus(map)
        val context = mapContext(map, config)
        val decision = mapDecision(map, config)
        val consequences = mapConsequences(map, config)
        val participants = mapParticipants(map, config)
        return Adr(
            title = title,
            date = date,
            status = status,
            context = context,
            decision = decision,
            consequences = consequences,
            participants = participants,
            urlMap = urlMap
        )

    }


    private fun mapTitle(map: MutableMap<String, MutableList<String>>): String {
        val title = map["TITLE"]
        require(title != null) { "Invalid syntax title not found" }
        return title[0].trim().escapeXml()
    }

    private fun mapDate(map: MutableMap<String, MutableList<String>>): String {
        val date = map["DATE"]
        require(date != null) { "Invalid syntax date not found" }
        return date[0].trim()
    }

    private fun mapStatus(map: MutableMap<String, MutableList<String>>): Status {
        val status = map["STATUS"]
        require(status != null) { "Invalid syntax status not found" }
        require(enumContains<Status>(status[0].trim())) {
            "$status is not a valid status not found in: ${
                Status.values().contentToString()
            }"
        }
        return Status.valueOf(status[0].trim())
    }

    private fun mapContext(map: MutableMap<String, MutableList<String>>, config: AdrParserConfig): MutableList<String> {
        val context = map["CONTEXT"]
        require(context != null) { "Invalid syntax context not found" }

        val list = mutableListOf<String>()
        val sb = StringBuilder()

        context.forEach {
            sb.append(" ${it.trim()}")
        }
        list.addAll(sb.toString().addLinebreaks(config.lineSize))
        return list
    }

    private fun mapDecision(
        map: MutableMap<String, MutableList<String>>,
        config: AdrParserConfig
    ): MutableList<String> {
        val decision = map["DECISION"]
        require(decision != null) { "Invalid syntax decision not found" }
        val list = mutableListOf<String>()
        val sb = StringBuilder()
        decision.forEach {
            sb.append(" ${it.trim()}")
        }
        list.addAll(sb.toString().addLinebreaks(config.lineSize))
        return list
    }

    private fun mapConsequences(
        map: MutableMap<String, MutableList<String>>,
        config: AdrParserConfig
    ): MutableList<String> {
        val consequences = map["CONSEQUENCES"]
        require(consequences != null) { "Invalid syntax consequences not found" }
        val list = mutableListOf<String>()
        val sb = StringBuilder()
        consequences.forEach {
            sb.append(" ${it.trim()}")
        }
        list.addAll(sb.toString().addLinebreaks(config.lineSize))
        return list
    }

    private fun mapParticipants(
        map: MutableMap<String, MutableList<String>>,
        config: AdrParserConfig
    ): MutableList<String> {
        val parts = map["PARTICIPANTS"]
        val list = mutableListOf<String>()
        parts?.forEach {
            list.addAll(it.addLinebreaks(config.lineSize))
        }
        return list
    }
}

/**
 * Returns `true` if enum T contains an entry with the specified name.
 */
inline fun <reified T : Enum<T>> enumContains(name: String): Boolean {
    return enumValues<T>().any { it.name == name }
}

fun String.addLinebreaks(maxLineLength: Int): MutableList<String> {
    val list = mutableListOf<String>()
    val tok = StringTokenizer(this, " ")
    var output = String()
    var lineLen = 0
    while (tok.hasMoreTokens()) {
        val word = tok.nextToken()
        if (lineLen + word.length > maxLineLength) {
            list.add(output.escapeXml())
            output = String()
            lineLen = 0
        }
        output += ("$word ")
        lineLen += word.length
    }
    if (list.size == 0 || lineLen > 0) {
        list.add(output.escapeXml())
    }
    return list
}

fun String.makeUrl(urlMap: MutableMap<Int, String>, config: AdrParserConfig): String {
    var key = 0
    val maxEntry = urlMap.maxByOrNull { it.key }
    maxEntry?.let {
        key = it.key
    }
    var newWin = "_top"
    if(config.newWin) {
        newWin = "_blank"
    }
    var newStr = this
    if (this.contains("[[") && this.contains("]]")) {
        val regex = "(?<=\\[\\[)(.*?)(?=]])".toRegex()
        val matches = regex.findAll(this)
        val m = mutableMapOf<String, String>()
        matches.forEach {
            val sp = it.value.split(" ")
            val str = StringBuilder()
            for (i in 1 until sp.size) {
                str.append(sp[i])
                if (i < sp.size - 1) {
                    str.append(" ")
                }
            }
            val url = "<tspan><a href=\"${sp[0]}\" xlink:href=\"${sp[0]}\" class=\"adrlink\" target=\"$newWin\">${str}</a></tspan>"
            m[url] = it.value
        }
        var count = key + 1
        m.forEach { (k, v) ->
            urlMap[count] = k
            newStr = newStr.replace("[[${v}]]", "_${count++}_")
        }
        return newStr.escapeXml()
    }
    return this

}

fun generateRectPathData(width: Float, height: Float, topLetRound:Float, topRightRound:Float, bottomRightRound:Float, bottomLeftRound:Float): String {
    return """M 0 $topLetRound 
 A $topLetRound $topLetRound 0 0 1 $topLetRound 0
 L ${(width - topRightRound)} 0
 A $topRightRound $topRightRound 0 0 1 $width $topRightRound
 L $width ${(height - bottomRightRound)}
 A $bottomRightRound $bottomRightRound 0 0 1 ${(width - bottomRightRound)} $height
 L $bottomLeftRound $height
 A $bottomLeftRound $bottomLeftRound 0 0 1 0 ${(height - bottomLeftRound)}
 Z"""
}
fun main() {
    val adr = ADRParser().parse(
        // language=text
        """
        Title:Use Solr for Structured Data Search
Date: November 24th, 2010
Status: Superseded
Context:  Solr and Elasticsearch are both open source search engines. Both can be used to search
large amounts of data quickly and accurately. While Solr uses a SQL-like query language, Elasticsearch has a full-text search engine and is designed for distributed search and analytics. Elasticsearch also allows for faster indexing and more advanced search replicas. Both technologies have strengths and weaknesses and are often used in
combination for enterprise-level search. There is a need of having an API exposed which can be used to search structured 
data. The Data currently resides in RDBMS, it is difficult to expose micro-service directly querying out of RDBMS databases since the application runs out of the same environment.
There are options like [[https://www.elastic.co ElasticSearch]] and Solr where data can be replicated. These solutions provide out of the box capabilities
that can be leveraged by developers without needed to build RESTful or GraphQL type APIs.
Decision:Use [[https://solr.apache.org/ Solr]] for data indexing. This use is because Solr has high performance throughput with large volume of data.
Unstructured data can also be supported.
If this decision does not meet the need then additional PoC will be created.
Consequences:Data Needs to be replicated across the solr cloud instances.
This Solr cloud needs maintenance.
Near realtime data replication is required Additional Cost of maintaining the Solr Cloud environment.
Participants:Roach,Rose,Duffy
        """.trimIndent(),
        AdrParserConfig(newWin = false, lineSize = 90, increaseWidthBy = 50)
    )
    var svg = (AdrMakerNext().makeAdrSvg(adr, false, AdrParserConfig(newWin = false, lineSize = 90, increaseWidthBy = 50)))
    adr.urlMap.forEach { (t, u) ->
        svg = svg.replace("_${t}_", u)
    }
    val f = File("src/test/resources/test.svg")
    f.writeBytes(svg.toByteArray())
}
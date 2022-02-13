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
import java.io.File
import java.util.StringTokenizer

class ADRParser {

    fun parse(content: String, config: AdrParserConfig = AdrParserConfig()): Adr {
        val lines = content.lines()
        val m = mutableMapOf<String, MutableList<String>>()
        var value = mutableListOf<String>()
        var currKey = ""
        lines.forEach { line ->
            val key: String
            if (line.contains(":")) {
                if (currKey.isNotEmpty()) {
                    m[currKey.uppercase()] = value
                }
                value = mutableListOf<String>()
                val split = line.split(":")
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
        return mapToAdr(m, config)
    }

    private fun mapToAdr(map: MutableMap<String, MutableList<String>>, config: AdrParserConfig): Adr {
        val title = mapTitle(map)
        val date = mapDate(map)
        val status = mapStatus(map)
        val context = mapContext(map, config)
        val decision = mapDecision(map, config)
        val consequences = mapConsequences(map, config)
        return Adr(
            title = title,
            date = date,
            status = status,
            context = context,
            decision = decision,
            consequences = consequences
        )

    }

    private fun mapTitle(map: MutableMap<String, MutableList<String>>): String {
        val title = map["TITLE"]
        require(title != null) { "Invalid syntax title not found" }
        return title[0].trim()
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
        context.forEach {
            list.addAll(it.addLinebreaks(config.lineSize))
        }
        return list
    }

    private fun mapDecision(map: MutableMap<String, MutableList<String>>, config: AdrParserConfig): MutableList<String> {
        val decision = map["DECISION"]
        require(decision != null) { "Invalid syntax decision not found" }
        val list = mutableListOf<String>()
        decision.forEach {
            list.addAll(it.addLinebreaks(config.lineSize))
        }
        return list
    }

    private fun mapConsequences(map: MutableMap<String, MutableList<String>>, config: AdrParserConfig): MutableList<String> {
        val consequences = map["CONSEQUENCES"]
        require(consequences != null) { "Invalid syntax consequences not found" }
        val list = mutableListOf<String>()
        consequences.forEach {
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
            list.add(output)
            output = String()
            lineLen = 0
        }
        output += ("$word ")
        lineLen += word.length
    }
    if (list.size == 0 || lineLen > 0) {
        list.add(output)
    }
    return list
}

fun main() {
    val adr = ADRParser().parse(
        """
        Title:Use Solr for Structured Data Search
        Date: November 24th, 2010
        Status: Rejected
        Context: There is a need of having an API exposed which can be used to search structured data.
         The Data currently resides in RDBMS, it is difficult to expose micro-service directly 
         querying out of RDBMS databases since the application runs out of the same environment. 
         There are options like Elastic Search and Solr where data can be replicated. These solutions provide out of the box capabilities 
         that can be leveraged by developers without needed to build RESTful or GraphQL type APIs.
         Decision:Use Solr for data indexing. This use is because Solr has high performance throughput with large volume of data.
         Unstructured data can also be supported. 
         If this decision does not meet the need then additional PoC will be created.
         Consequences:Data Needs to be replicated across the solr cloud instances. 
         This Solr cloud needs maintenance. 
         Near realtime data replication is required Additional Cost of maintaining the Solr Cloud environment.
        """.trimIndent()
    )
    val svg =(AdrMaker().makeAdrSvg(adr))
    val f = File("src/test/resources/test.svg")
    f.writeBytes(svg.toByteArray())
}
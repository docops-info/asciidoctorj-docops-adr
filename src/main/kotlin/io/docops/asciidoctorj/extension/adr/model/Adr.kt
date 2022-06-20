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

package io.docops.asciidoctorj.extension.adr.model


data class Adr(
    val title: String = "",
    val date: String = "",
    var status: Status = Status.Rejected,
    var context: List<String> = emptyList(),
    var decision: List<String> = emptyList(),
    var consequences: List<String> = emptyList(),
    var participants: List<String> = emptyList(),
    val urlMap: MutableMap<Int, String>
)
{
    fun statusClass(status: Status, current: String): String {
        if(status == Status.valueOf(current)) {
            return "selected"
        } else {
            return "unselected"
        }
    }
    fun participantAsStr(): String {
        if(participants.isEmpty()) {
            return ""
        }
        return  participants.joinToString(",")
    }
}
enum class Status{
    Proposed, Accepted, Superseded, Deprecated, Rejected;

    fun color(status: Status) = when(status) {
        Proposed -> "#568BBF"
        Accepted -> "#53A551"
        Superseded -> "#F5C344"
        Deprecated -> "#F5C344"
        Rejected -> "#CB444A"
    }
}

fun String.escapeXml(): String {
    val sb = StringBuilder()
    for (element in this) {
        when (val c: Char = element) {
            '<' -> sb.append("&lt;")
            '>' -> sb.append("&gt;")
            '\"' -> sb.append("&quot;")
            '&' -> sb.append("&amp;")
            '\'' -> sb.append("&apos;")
            else -> if (c.code > 0x7e) {
                sb.append("&#" + c.code + ";")
            } else sb.append(c)
        }
    }
    return sb.toString()
}

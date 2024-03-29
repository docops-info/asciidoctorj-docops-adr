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

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.junit.jupiter.api.Test
import java.awt.Color
import java.io.File

internal class AdrBlockProcessorTest {
    fun genDoc(isHtml: Boolean = true) {
        val attrs = Attributes.builder()
            //.sourceHighlighter("coderay")
            .allowUriRead(true)
            .dataUri(true)
            .copyCss(true)
            .noFooter(true)
            //.attribute("coderay-css", "class")
            //.attribute("coderay-linenums-mode", "inline")
            //.attribute("feedback")
            .attribute("local-debug", "true")
            .attribute("panel-webserver", "http://localhost:8010/extension")
            .attribute("tocbot")
            .build()

        val asciidoctor = Asciidoctor.Factory.create()

        val src = File("src/main/docs/adr.adoc")
        val build = File("docs/")
        build.mkdirs()
        var ext = "html"
        if(!isHtml) {
            ext = "pdf"
        }
        val target = File(build, "adr.$ext")
        if (target.exists()) {
            target.delete()
        }
        if(isHtml) {
            val options = Options.builder()
                .backend("html5")
                .toDir(build)
                .attributes(attrs)
                .safe(SafeMode.UNSAFE)
                .build()
            asciidoctor.convertFile(src, options)
        } else {
            val options = Options.builder()
                .backend("pdf")
                .toDir(build)
                .attributes(attrs)
                .safe(SafeMode.UNSAFE)
                .build()
            asciidoctor.convertFile(src, options)
        }

        assert(target.exists())
        //val images = File(src.parent, "images")
        //images.deleteRecursively()
    }

    @Test
    fun genHtml() {
        genDoc(true)
    }

    @Test
    fun genPdf() {
        genDoc(false)
    }

    @Test
    fun getColor() {
        val clr = gradientFromColor("#CB444A")
        println(clr)
    }
    private fun gradientFromColor(color: String): Map<String, String> {
        val decoded = Color.decode(color)
        val tinted1 = tint(decoded, 0.5)
        val tinted2 = tint(decoded, 0.25)
        return mapOf("color1" to tinted1, "color2" to tinted2, "color3" to color)
    }
    private fun tint(color: Color, factor: Double): String {
        val rs = color.red + (factor * (255 - color.red))
        val gs = color.green + (factor * (255 - color.green))
        val bs = color.blue + (factor * (255 - color.blue))
        return  "#${rs.toInt().toString(16)}${gs.toInt().toString(16)}${bs.toInt().toString(16)}"
    }
}
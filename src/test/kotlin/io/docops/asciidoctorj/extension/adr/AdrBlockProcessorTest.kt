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
import java.io.File

internal class AdrBlockProcessorTest {
    @Test
    fun genDoc() {
        val attrs = Attributes.builder()
            .sourceHighlighter("highlightjs")
            .allowUriRead(true)
            .dataUri(true)
            .copyCss(true)
            .noFooter(true)
            .build()

        val asciidoctor = Asciidoctor.Factory.create()

        val src = File("src/main/docs/adr.adoc")
        val build = File("build/docs/")
        build.mkdirs()
        val target = File(build, "adr.html")
        if (target.exists()) {
            target.delete()
        }
        val options = Options.builder()
            .backend("html5")
            .toDir(build)
            .attributes(attrs)
            .safe(SafeMode.UNSAFE)
            .build()
        asciidoctor.convertFile(src, options)

        assert(target.exists())
        //val images = File(src.parent, "images")
        //images.deleteRecursively()
    }
}
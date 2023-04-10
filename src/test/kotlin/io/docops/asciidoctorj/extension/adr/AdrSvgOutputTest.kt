package io.docops.asciidoctorj.extension.adr

import org.junit.jupiter.api.Test
import java.io.File

class AdrSvgOutputTest {

    private val requiredSuffixes = listOf("adr")
    @Test
    fun genAll() {
        val parent = File("build/images")
        if(!parent.exists()) {
            parent.mkdirs()
        }
       val f = File("src/main/docs").walkTopDown().filter {
            file -> file.isFile && hasRequiredSuffix(file)
        }.forEach {
            val str = it.readText()
           val adr =ADRParser().parse(str, AdrParserConfig(newWin = false, lineSize = 90, increaseWidthBy = 50))
           var svg = (AdrMakerNext().makeAdrSvg(adr, false, AdrParserConfig(newWin = false, lineSize = 90, increaseWidthBy = 50)))
           adr.urlMap.forEach { (t, u) ->
               svg = svg.replace("_${t}_", u)
           }
           svg = svg.replace("_nbsp;_","<tspan x=\"14\" dy=\"20\">&#160;</tspan>")
           val f = File(parent,"${it.name}.svg")
           f.writeBytes(svg.toByteArray())
       }

    }

    private fun hasRequiredSuffix(file: File): Boolean {
        return requiredSuffixes.contains(file.extension)
    }
}
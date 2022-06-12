package io.docops.asciidoctorj.extension.adr

import org.asciidoctor.ast.ContentModel
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Contexts
import org.asciidoctor.extension.Name
import org.asciidoctor.extension.Reader
import java.io.File
import java.util.StringJoiner

@Name("adrsummary")
@Contexts(Contexts.LISTING)
@ContentModel(ContentModel.COMPOUND)
class AdrSummaryBlockProcessor : BlockProcessor() {
    override fun process(parent: StructuralNode, reader: Reader, attributes: MutableMap<String, Any>): Any? {
        val content = reader.readLines()
        val lines = startTable()
        content.forEach { item ->
            val f = File(reader.dir, item)
            if(f.exists()) {
                lines.addAll(adrFromFile(f))
            }
        }
        lines.addAll(closeTable())
        return createBlock(parent, "open", lines)
    }

    private fun adrFromFile(f: File): MutableList<String> {
        val adr = ADRParser().parse(f.readText(), AdrParserConfig())
        val lines = mutableListOf<String>()
        lines.add("a|${adr.title} |${adr.status} |${adr.participantAsStr()} |${adr.date}")
        return lines
    }

    private fun startTable(): MutableList<String> {
        val lines = mutableListOf<String>()
        lines.add("[options=header]")
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

}
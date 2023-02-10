package io.docops.asciidoctorj.extension.adr

import org.asciidoctor.ast.Document
import org.asciidoctor.extension.DocinfoProcessor
import org.asciidoctor.extension.Location
import org.asciidoctor.extension.LocationType

@Location(LocationType.HEADER)
class AdrDocinfoProcessor: DocinfoProcessor() {
    override fun process(document: Document): String {
        //language=html
        return """
            <style>
            .modal {
            background: rgb(181,189,200);
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
            color: #ea0606;
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
        """.trimIndent()
    }
}
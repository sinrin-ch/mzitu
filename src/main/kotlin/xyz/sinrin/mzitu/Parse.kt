package xyz.sinrin.mzitu

import org.jsoup.nodes.Document


fun Document.parse(handleDocument: Document.() -> Unit) {
    this.apply(handleDocument)
}
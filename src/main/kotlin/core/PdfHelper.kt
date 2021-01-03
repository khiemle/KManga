package core

import id.jasoet.funpdf.HtmlToPdf
import id.jasoet.funpdf.PageOrientation
import java.nio.file.Paths

val pdf by lazy {
    HtmlToPdf(executable = "/usr/bin/wkhtmltopdf") {
        orientation(PageOrientation.PORTRAIT)
        pageSize("A5")
        marginTop("0in")
        marginBottom("0in")
        marginLeft("0in")
        marginRight("0in")
    }
}

fun createPdfByLib(lstImages: List<String>, pdfName: String) {
    print("Wrapping image urls and creating $pdfName file ... ")
    val imageUrls = lstImages.joinToString(separator = "") { imageUrl ->
        "<img style=\"width:100%; max-height:960px;\" src=\"$imageUrl\" /><br/>"
    }
    val htmlString = "<html><body>${imageUrls}</body></html>"
    val outputFile = Paths.get(pdfName).toFile()
    pdf.convert(input = htmlString, output = outputFile) // will always return null if output is redirected
    println("Done")
}
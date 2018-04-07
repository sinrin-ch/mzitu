package xyz.sinrin.mzitu

import net.dongliu.requests.Requests
import net.dongliu.requests.exception.RequestsException
import org.jsoup.HttpStatusException
import org.jsoup.nodes.Document
import xyz.sinrin.mzitu.util.SoapUtil
import java.io.File
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.time.LocalDateTime


fun main(args: Array<String>) {

    var nextPageUrl = "http://www.mzitu.com/xinggan/page/46/"
    do {
        nextPageUrl = handlePage(nextPageUrl)
    } while (!nextPageUrl.isNullOrBlank())
//    handleSections(getSectionUrlInPage("http://www.mzitu.com/xinggan/page/45/").takeLast(6))


}

private fun handlePage(pageUrl: String): String {
    println("开始处理页面: $pageUrl =================================================================================")
    val document: Document = SoapUtil.soup(pageUrl)
    val sections = getSectionUrlInPage(document)
    handleSections(sections)
    return getNextPageUrl(document)
}

private fun getNextPageUrl(mainPageUrl: String): String {
    val document: Document = SoapUtil.soup(mainPageUrl)
    return getNextPageUrl(document)
}

private fun getNextPageUrl(document: Document): String {
    val aUrls = document.select("a.next.page-numbers")
    if (aUrls.isEmpty()) {
        return ""
    }
    return aUrls.attr("href")
}

private fun handleSection(sectionMainUrl: String) {
    val sectionPages = getAllPageUrlInSection(sectionMainUrl)
    sectionPages.forEach(::getSectionPageImage)
}

private fun handleSections(sectionMainUrls: Iterable<String>) {
    sectionMainUrls.forEach(::handleSection)
}

private fun getAllPageUrlInSection(sectionPageUrl: String): List<String> {
    val document: Document = SoapUtil.soup(sectionPageUrl)
    document.select(".main-title").text().also{println("开始处理section: $it  url:  $sectionPageUrl   ======================================")}
    val aUrls = document.select("div.pagenavi a").eachAttr("href")
    val lastPage = aUrls[aUrls.size - 2].split("/").last().toInt()


    val list = mutableListOf(sectionPageUrl)
    list.addAll((2..lastPage).map { "$sectionPageUrl/$it" })
    return list
}

private fun String.isNumber(): Boolean {
    return "\\d+".toRegex().matches(this)
}

private fun getSectionPageImage(sectionPageUrl: String) {
    var document: Document

    while (true) {
        try {
            document = SoapUtil.soup(sectionPageUrl)
            break
        } catch (e: HttpStatusException) {
            if (e.statusCode == 502) {
                e.printStackTrace()
                println("发生异常,重试========================")
                Thread.sleep(10000)  // 10秒后重试
            } else {
                throw e
            }
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            println("超时,10秒后重试========================")
            Thread.sleep(10000)  // 10秒后重试
        } catch(e: MalformedURLException){
            println("地址格式错误: url:  $sectionPageUrl  ==============================")
            return
        }
    }
    val img = document.select("div.main-image img")
    val src = img.attr("src")
    val subPath = img.attr("alt")
    val ext = src.split(".").last()  // 扩展名
    val title = document.select(".main-title").text()  // 文件名

    saveImg(src, "$title.$ext", subPath)
}

private fun saveImg(imgSrc: String, fileName: String, subPath: String) {
    val filePath = "/Volumes/仓库/imgs/xinggan/$subPath"
    File(filePath).mkdirs()
    try {
        Requests.get(imgSrc).apply {
            headers(mapOf("User-Agent" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3298.3 Safari/537.36",
                    "Host" to "www.mzitu.com",
                    "referer" to "http://www.mzitu.com"))
        }
                .send().toFileResponse(File("$filePath/$fileName").toPath())
    } catch (e: RequestsException) {
        e.printStackTrace()
        println("地址格式错误: url:  $imgSrc  ==============================")
        return
    }
    println("${LocalDateTime.now()} 文件名:$fileName   url:$imgSrc")

}

private fun getSectionUrlInPage(document: Document): List<String> {
    return document.select("#pins li span a").map { it.attr("href") }
}

private fun getSectionUrlInPage(pageUrl: String): List<String> {
    val document: Document = SoapUtil.soup(pageUrl)
    return getSectionUrlInPage(document)
}
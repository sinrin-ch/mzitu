package xyz.sinrin.mzitu.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class SoapUtil {
    companion object {
        /**
         * 获得soup对象
         */


        fun soup(url: String, host: String = url.removePrefix("http://").removePrefix("https://").split("/")[0]): Document {

            val headers: Map<String, String> = mapOf("User-Agent" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3298.3 Safari/537.36",
                    "Host" to host
            )
            /**
             * 利用指定的Header链接到URL ,并拉取资源
             */
            return Jsoup.connect(url)
                    .headers(headers).get()
        }
    }
}

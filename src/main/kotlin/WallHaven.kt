package de.leo160905

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.awt.Dimension
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import kotlin.math.roundToInt

class WallHaven(val token: String) {
    private val baseURL = "https://wallhaven.cc/api/v1"
    private val searchURL = "$baseURL/search"

    fun search(wallHavenQuery: wallHavenQuery, page: Int): JSONArray {
        val response =
            makeARequest("$searchURL?apikey=$token&q=${wallHavenQuery.query}&page=$page&purity=${wallHavenQuery.purity}&sorting=${wallHavenQuery.sorting.label}")
        println(response)
        if (!response.startsWith("<!DOCTYPE")) {
            val responseObject = JSONObject(response)
            val responseArray = responseObject.getJSONArray("data")
            return responseArray
        }
        throw Exception("To many Requests")
    }

    fun getNumberOfPagesOfQuery(query: wallHavenQuery): Int {
        val response = makeARequest("$searchURL?apikey=$token&q=${query.query}&purity=${query.purity}")

        if (!response.startsWith("<!DOCTYPE")) {
            val responseObject = JSONObject(response)
            return responseObject.getJSONObject("meta").getInt("last_page")
        }

        return -1
    }

    fun makeARequest(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        val response = client.newCall(request).execute()

        if (response.body != null) {
            val dataStream = response.body!!.byteStream()
            val data = dataStream.readBytes()
            return data.toString(Charsets.UTF_8)
        } else throw Exception("Response body is null")
    }

    fun getImage(url: String): ByteArray {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        return response.body!!.byteStream().readBytes()
    }

    fun loadScaledThumbnail(item: Wallpaper, thumbnailWidth: Int): ImageIcon {
        val icon = ImageIcon(getImage(item.thumbnailURL))
        val heigh = icon.image.getHeight(null) / (icon.image.getWidth(null) / thumbnailWidth.toFloat())
        val scaledImage = BufferedImage(thumbnailWidth, heigh.roundToInt(), BufferedImage.TYPE_INT_RGB)
        item.thumbnailDimension = Dimension(thumbnailWidth, heigh.roundToInt())
        scaledImage.graphics.drawImage(icon.image, 0, 0, null)
        return ImageIcon(scaledImage)
    }
}

data class wallHavenQuery(val query: String, val purity: String = "100", val sorting: SortType = SortType.TOPLIST) {
    enum class SortType(val label: String) {
        RELEVANCE("relevance"),
        RANDOM("random"),
        VIEWS("views"),
        FAVOURITES("favourites"),
        TOPLIST("toplist")
    }
}
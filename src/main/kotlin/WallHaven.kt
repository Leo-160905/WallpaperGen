package de.leo160905

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class WallHaven(val token: String) {
    private val baseURL = "https://wallhaven.cc/api/v1"
    private val searchURL = "$baseURL/search"
    private val infoURL = "$baseURL/w"

    fun search(query: String, page: Int): JSONArray {
        val response = makeARequest("$searchURL?apikey=$token&q=$query&page=$page&purity=111")
        println(response)
        if(!response.startsWith("<!DOCTYPE")) {
            val responseObject = JSONObject(response)
            val responseArray = responseObject.getJSONArray("data")
            return responseArray
        }
        throw Exception("To many Requests")
    }

    fun getNumberOfPagesOfQuery(query: String): Int {
        val response = makeARequest("$searchURL?apikey=$token&q=$query")

        if(!response.startsWith("<!DOCTYPE")) {
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
}
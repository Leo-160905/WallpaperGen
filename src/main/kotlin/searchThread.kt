package de.leo160905

import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.LinkedBlockingQueue

class searchThread(val query: wallHavenQuery, val workQueue: LinkedBlockingQueue<Worksignal>, val wallHaven: WallHaven, var minMaxPage: Pair<Int, Int>) : Thread() {
    var numberOfPages = 0
    var stopThread = false
    override fun run() {
        try {
            println("Starts to collect all pages")
            do {
                numberOfPages = wallHaven.getNumberOfPagesOfQuery(query)
                println("numbers of pages: ${numberOfPages}")
                if (numberOfPages == -1) {
                    println("To many requests: Waiting 5s")
                    repeat(10) {
                        sleep(500)
                        if (stopThread) break
                    }
                }
            } while (numberOfPages == -1)

            minMaxPage = Pair(minMaxPage.first, if(numberOfPages > minMaxPage.second && minMaxPage.second != -1) minMaxPage.second else numberOfPages)
            if(minMaxPage.first > minMaxPage.second) stopThread = true

            if (!stopThread) {
                for (page in minMaxPage.first..minMaxPage.second) {
                    var response = JSONArray()
                    var isSuccessfull: Boolean
                    do {
                        try {
                            response = wallHaven.search(query, page)
                            isSuccessfull = true
                        } catch (_: Exception) {
                            isSuccessfull = false
                            repeat(10) {
                                sleep(500)
                                if (stopThread) break
                            }
                        }
                    } while (!isSuccessfull && !stopThread)

                    for (item in response) {
                        if (stopThread) {
                            workQueue.clear()
                            currentThread().interrupt()
                            break
                        }

                        if (item is JSONObject) {
                            val workSignal: Worksignal = Wallpaper(
                                item.getString("id"),
                                item.getString("path"),
                                item.getJSONObject("thumbs").getString("large")
                            )
                            workQueue.put(workSignal)
                        }
                    }

                    if (stopThread) break

                    sleep(1000)
                }
            }
        } catch (_: Exception) {}
        println("finished loading all pages")
    }
}
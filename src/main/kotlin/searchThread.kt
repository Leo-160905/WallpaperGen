package de.leo160905

import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.LinkedBlockingQueue

class searchThread(val query: String, val workQueue: LinkedBlockingQueue<Worksignal>, val wallHaven: WallHaven) : Thread() {
    var numberOfPages = 0
    var stopThread = false
    override fun run() {
        try {
            println("Starts to collect all pages")
            do {
                numberOfPages = wallHaven.getNumberOfPagesOfQuery(query)
                if (numberOfPages == -1) {
                    println("To many requests: Waiting 5s")
                    for (i in 0..10) {
                        sleep(500)
                        if (stopThread) break
                    }
                }
            } while (numberOfPages == -1)

            if (!stopThread) {
                for (page in 1..numberOfPages) {
                    var response = JSONArray()
                    var isSuccessfull: Boolean
                    do {
                        try {
                            response = wallHaven.search(query, page)
                            isSuccessfull = true
                        } catch (_: Exception) {
                            isSuccessfull = false
                            for (i in 0..10) {
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
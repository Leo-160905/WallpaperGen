package de.leo160905

import org.json.JSONObject
import java.util.concurrent.LinkedBlockingQueue

class SearchRunnable(val query: String, val workQueue: LinkedBlockingQueue<Worksignal>, val wallHaven: WallHaven) :
    Thread() {
    var numberOfPages = 0
    var stopThread = false
    override fun run() {
        try {

            println("Starts to collect all pages")
            do {
                numberOfPages = wallHaven.getNumberOfPagesOfQuery(query)
                if(numberOfPages == -1) {
                    println("To many requests: Waiting 5s")
                    for(i in 0..10) {
                        sleep(500)
                    }
                }
            }while (numberOfPages == -1)

            for (page in 1..numberOfPages) {
                for (i in 1..5) {
                    try {
                        val response = wallHaven.search(query, page)
                        for (item in response) {
                            if(stopThread) {
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
                        break
                    }
                    catch (e: Exception) {
                        println("$i. attempt: $e")
                        println("try again in 5s")
                        for (i in 0..10) {
                            if(stopThread) {
                                workQueue.clear()
                                currentThread().interrupt()
                                break
                            }
                            sleep(500)
                        }
                    }
                    if(currentThread().isInterrupted) break
                }
                if(currentThread().isInterrupted) break
            }
            workQueue.put(Done)
        }
        catch (_: Exception) {}
        println("finished loading all pages")
    }
}
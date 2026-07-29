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
            numberOfPages = wallHaven.getNumberOfPagesOfQuery(query)
            for (page in 1..numberOfPages) {
                for (i in 1..5) {
                    try {
                        val response = wallHaven.search(query, page)
                        for (item in response) {
                            if(stopThread) {
                                workQueue.clear()
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
                        println("try again in 3s")
                        Thread.sleep(3000)
                    }
                    if(Thread.currentThread().isInterrupted) break;
                }
                if(Thread.currentThread().isInterrupted) break;
            }
            workQueue.put(Done)
        }
        catch (_: Exception) {}
        println("finished loading all pages")
    }
}
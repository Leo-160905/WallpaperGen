package de.leo160905

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

class ThumbnailLoadingThread(
    val threadId: String,
    val wallHaven: WallHaven,
    val finishedWallpaperHasmap: ConcurrentHashMap<String, Wallpaper>,
    val workQueue: LinkedBlockingQueue<Worksignal>,
    val thumbnailWidth: Int,
    val controller: Controller
) : Thread() {
    override fun run() {
        println("Starting Thread $threadId")
        try {
            while (true) {
                if (currentThread().isInterrupted) break
                val item = workQueue.take()
//                println("Thread $threadId is doing $item")
                when (item) {
                    is Done -> {
                        workQueue.put(Done); break
                    }

                    is Wallpaper -> {
                        val thumbnail = wallHaven.loadScaledThumbnail(item, thumbnailWidth)
//                        println("Thread $threadId is now handling ${item.id}")
                        finishedWallpaperHasmap[item.id] = item
                        controller.setThumbnailOnGui(finishedWallpaperHasmap[item.id]!!, thumbnail)
                    }
                }
            }
        } catch (_: Exception) {
            println("Thread $threadId didn't start correctly")
        }
        println("Closing Thread $threadId")
    }
}
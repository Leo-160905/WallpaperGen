package de.leo160905

import java.awt.Image
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import javax.swing.ImageIcon

class ThumbnailLoadingRunnable(val threadId:String, val wallhaven: WallHaven, val finishedWallpaperhasmap: ConcurrentHashMap<String, Wallpaper>, val workQueue: LinkedBlockingQueue<Worksignal>, val thumbnailWidth:  Int, val controller: Controller) : Thread() {
    override fun run() {
        println("Starting Thread $threadId")
        try {
            while (true) {
                when (val item = workQueue.take()) {
                    is Done -> {workQueue.put(Done); break}
                    is Wallpaper -> {
//                        println("Thread $threadId starts to load wallpaper")
                        val icon = ImageIcon(wallhaven.getImage(item.thumbnailURL))
                        val scaledimage = icon.image.getScaledInstance(thumbnailWidth, -1, Image.SCALE_SMOOTH)
                        item.thumbnail = ImageIcon(scaledimage)
                        finishedWallpaperhasmap[item.id] = item
                        controller.gui.fillPic(item.thumbnail, item.id)
                    }
                }
            }
        }
        catch (_: Exception) {}
        println("Closing Thread $threadId")
    }
}
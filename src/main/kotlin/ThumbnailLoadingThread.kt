package de.leo160905

import java.awt.image.BufferedImage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import javax.swing.ImageIcon
import kotlin.math.roundToInt

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
                when (val item = workQueue.take()) {
                    is Done -> {
                        workQueue.put(Done); break
                    }

                    is Wallpaper -> {
                        val icon = ImageIcon(wallHaven.getImage(item.thumbnailURL))
                        val heigh = icon.image.getHeight(null) / (icon.image.getWidth(null) / thumbnailWidth.toFloat())
                        val scaledImage = BufferedImage(thumbnailWidth, heigh.roundToInt(), BufferedImage.TYPE_INT_RGB)
                        scaledImage.graphics.drawImage(icon.image, 0, 0, null)
//                        val scaledImage = icon.image.getScaledInstance(thumbnailWidth, heigh.roundToInt(), Image.SCALE_SMOOTH)
                        item.thumbnail = ImageIcon(scaledImage)

                        finishedWallpaperHasmap[item.id] = item
                        controller.setThumbnailOnGui(finishedWallpaperHasmap[item.id]!!)
                    }
                }
            }
        } catch (_: Exception) {
        }
        println("Closing Thread $threadId")
    }
}
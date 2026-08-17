package de.leo160905

import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import javax.imageio.ImageIO

class Controller {
    val wallHaven = WallHaven(configHashmap["apiKey"].toString())
    val thumbnailWorkQueue = LinkedBlockingQueue<Worksignal>()
    val finishedWallpaperHashmap = ConcurrentHashMap<String, Wallpaper>()
    val backgroundSearchThreads: ArrayList<Thread> = ArrayList()
    lateinit var searchThread: SearchRunnable
    var gui = SearchGUI(this)
    var thumbnailWidth = -1

    fun search(query: String) {
        println("Now Searching for: $query")
        if (backgroundSearchThreads.isNotEmpty()) {
            searchThread.stopThread = true
            backgroundSearchThreads.forEach { it.interrupt() }
            backgroundSearchThreads.forEach { it.join() }
            searchThread.join()
            backgroundSearchThreads.clear()
        }

        thumbnailWorkQueue.clear()
        finishedWallpaperHashmap.clear()

        gui.removePictures()

        println(backgroundSearchThreads.size)
        searchThread = SearchRunnable(query, thumbnailWorkQueue, wallHaven)

        for (i in 0..<4) {
            backgroundSearchThreads.add(
                Thread(
                    ThumbnailLoadingRunnable(
                        "${i + 1}",
                        wallHaven,
                        finishedWallpaperHashmap,
                        thumbnailWorkQueue,
                        thumbnailWidth,
                        this
                    )
                )
            )
        }
        searchThread.start()
        backgroundSearchThreads.forEach { it.start() }
    }

    fun handelSelectedPicture(id: String) {
        val wallpaperItem = finishedWallpaperHashmap[id]
        if (wallpaperItem != null) {
            println("loads Picture")
            val bytes = wallHaven.getImage(wallpaperItem.imageURL)
            saveImage(bytes, id)

            saveThumbnail(wallpaperItem)
        }
    }

    fun saveImage(bytes: ByteArray, id: String) {
        val wallpaper = File("${configHashmap["downloadFolder"]}/$id.jpg")
        val fos = FileOutputStream(wallpaper)
        fos.write(bytes)
        fos.close()
    }

    fun saveThumbnail(wallpaperItem: Wallpaper) {
        val thumbnailImage = wallpaperItem.thumbnail.image
        val bi = BufferedImage(
            thumbnailImage.getWidth(null),
            thumbnailImage.getHeight(null),
            BufferedImage.TYPE_INT_RGB
        )

        val g2d = bi.createGraphics()
        g2d.drawImage(thumbnailImage, 0, 0, null)
        g2d.dispose()

        println(configHashmap["thumbnailFolder"])
        ImageIO.write(bi, "PNG", File("${configHashmap["thumbnailFolder"]}/${wallpaperItem.id}.png"))
    }

    fun openPictureLibrary() {

    }
}
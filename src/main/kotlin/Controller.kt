package de.leo160905

import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.UIManager

class Controller {
    val wallHaven = WallHaven(configHashmap["apiKey"].toString())
    val thumbnailWorkQueue = LinkedBlockingQueue<Worksignal>()
    val finishedWallpaperHashmap = ConcurrentHashMap<String, Wallpaper>()
    val backgroundSearchThreads: ArrayList<Thread> = ArrayList()
    lateinit var searchThread: searchThread
    var gui = SearchGUI(this)

    fun search(query: wallHavenQuery, pageFilter: String) {
        val minMaxPage: Pair<Int, Int>
        if(checkPageFilter(pageFilter)) {
            minMaxPage = pageFilter.split("..").map{it.toInt()}.toPair()
        }
        else throw(Exception("Regex wasn't correct exception"))

        println("Now Searching for: ${query.query}")
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
        searchThread = searchThread(query, thumbnailWorkQueue, wallHaven, minMaxPage)
        val scrollbarWidth = UIManager.getInt("ScrollBar.width").let { if (it > 0) it else 16 }
        val thumbnailWidth = (gui.contentPanel.width - scrollbarWidth - 10) / 3
        println(thumbnailWidth)
        for (i in 0..<4) {
            backgroundSearchThreads.add(
                Thread(
                    ThumbnailLoadingThread(
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
        println("Now")
        backgroundSearchThreads.forEach { it.start() }
        println("----------------------------------------------------------------------------------------------------------------")
    }

    fun handelSelectedPicture(id: String) {
        gui.setCursorToLoading()
        val wallpaperItem = finishedWallpaperHashmap[id]
        if (wallpaperItem != null) {
            println("loads Picture")
            val bytes = wallHaven.getImage(wallpaperItem.imageURL)
            wallpaperItem.imageBytes = bytes
            wallpaperItem.image = ImageIO.read(ByteArrayInputStream(bytes))
            wallpaperItem.size = Dimension(wallpaperItem.image.width, wallpaperItem.image.height)
            println(wallpaperItem.size.toString())
            PreviewGUI(wallpaperItem, this)

        }
    }

    fun saveWallpaper(wallpaper: Wallpaper) {
            saveImage(wallpaper.imageBytes, wallpaper.id)
            saveThumbnail(wallpaper)
    }

    fun saveImage(bytes: ByteArray, id: String) {
        val wallpaper = File("${configHashmap["downloadFolder"]}/$id.jpg")
        val fos = FileOutputStream(wallpaper)
        fos.write(bytes)
        fos.close()
    }

    fun saveThumbnail(wallpaperItem: Wallpaper) {
        val thumbnailImage = wallHaven.loadScaledThumbnail(wallpaperItem, wallpaperItem.thumbnailDimension.width).image
        val bi = BufferedImage(
            thumbnailImage.getWidth(null),
            thumbnailImage.getHeight(null),
            BufferedImage.TYPE_INT_RGB
        )

        val g2d = bi.createGraphics()
        g2d.drawImage(thumbnailImage, 0, 0, null)
        g2d.dispose()
        ImageIO.write(bi, "PNG", File("${configHashmap["thumbnailFolder"]}/${wallpaperItem.id}.png"))
    }

    fun setThumbnailOnGui(item: Wallpaper, thumbnail: ImageIcon) {
        gui.CreatePictureButton(thumbnail, item.id)
    }

    fun openPictureLibrary() {

    }

    fun <T> List<T>.toPair(): Pair<T,T> {
        if(this.size >= 2) return Pair(this[0], this[1])
        else throw(Exception("not right pattern exception (Regex didn't work)"))
    }

    fun checkPageFilter(filter: String): Boolean {
        val regex = Regex("""\d+\.\.-?\d+""")
        if(filter.matches(regex)) {
            val pages: Pair<Int, Int> = filter.split("..").map { it.toInt() }.toPair()
            println("${pages.first} : ${pages.second}")
            return ((pages.first <= pages.second) xor (pages.second == -1)) && pages.first > 0
        }
        return false
    }

    fun getMaxPageOfQuery(query: wallHavenQuery): Int {
        return wallHaven.getNumberOfPagesOfQuery(query)
    }
}
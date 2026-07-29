package de.leo160905

import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

class Controller {
    val wallHaven = WallHaven(propertyHashmap["apiKey"].toString())
    val thumbnailWorkQueue = LinkedBlockingQueue<Worksignal>()
    val finishedWallpaperHashmap = ConcurrentHashMap<String, Wallpaper>()
    val backgroundSearchThreads: ArrayList<Thread> = ArrayList()
    lateinit var searchThread: SearchRunnable
    var gui = GUI(this)
    var thumbnailWidth = -1

    fun search(query: String) {
        println("Now Searching for: $query")
        if(backgroundSearchThreads.isNotEmpty()) {
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
        searchThread = SearchRunnable(query,thumbnailWorkQueue, wallHaven)

        for (i in 0..<4) {
            backgroundSearchThreads.add(Thread(ThumbnailLoadingRunnable("${i+1}", wallHaven, finishedWallpaperHashmap, thumbnailWorkQueue, thumbnailWidth, this)))
        }
        searchThread.start()
        backgroundSearchThreads.forEach { it.start() }
    }

    fun loadPicture(id: String) {
        if(finishedWallpaperHashmap[id] != null) {
            println("loads Picture")
            val bytes = wallHaven.getImage(finishedWallpaperHashmap[id]!!.imageURL)
            val file = File("${propertyHashmap["downloadFolder"]}/$id.jpg")
            val fos = FileOutputStream(file)
            fos.write(bytes)
            fos.close()
        }
    }
}
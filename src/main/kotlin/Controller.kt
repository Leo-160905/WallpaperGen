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
import javax.swing.SwingUtilities
import javax.swing.UIManager

class Controller {
    val wallHaven = WallHaven(configHashmap["apiKey"].toString())
    val thumbnailWorkQueue = LinkedBlockingQueue<Worksignal>()
    val finishedWallpaperHashmap = ConcurrentHashMap<String, Wallpaper>()
    val backgroundSearchThreads: ArrayList<Thread> = ArrayList()
    lateinit var searchThread: searchThread
    var searchGUI: SearchGUI
    var libraryGUI: LibraryGUI
    var thumbnailWidth = 0


    var savedWallpapers: ArrayList<Wallpaper> = ArrayList()

    init {
        if (configHashmap["startupMode"] == StartupMode.LIBRARY.name) {
            println("Library")
            searchGUI = SearchGUI(this, false)
            libraryGUI = LibraryGUI(this, true)
            distributeSavedPicturesToLibraryPanel()
        } else {
            println("search")
            searchGUI = SearchGUI(this, true)
            libraryGUI = LibraryGUI(this, false)
            distributeSavedPicturesToLibraryPanel()
        }

    }

    fun search(query: wallHavenQuery, pageFilter: String) {
        val minMaxPage: Pair<Int, Int>
        if (checkPageFilter(pageFilter)) {
            minMaxPage = pageFilter.split("..").map { it.toInt() }.toPair()
        } else throw (Exception("Regex wasn't correct exception"))

        println("Now Searching for: ${query.query}")

        reset()

        println(backgroundSearchThreads.size)
        searchThread = searchThread(query, thumbnailWorkQueue, wallHaven, minMaxPage)
        val scrollbarWidth = UIManager.getInt("ScrollBar.width").let { if (it > 0) it else 16 }
        val thumbnailWidth = (searchGUI.contentPanel.width - scrollbarWidth - 10) / 3
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
        backgroundSearchThreads.forEach { it.start() }
        println("----------------------------------------------------------------------------------------------------------------")
    }

    fun reset() {
        if (backgroundSearchThreads.isNotEmpty()) {
            searchThread.stopThread = true
            backgroundSearchThreads.forEach { it.interrupt() }
            backgroundSearchThreads.forEach { it.join() }
            searchThread.join()
            backgroundSearchThreads.clear()
        }

        println("Exsisting Threads: ${backgroundSearchThreads.size}")

        thumbnailWorkQueue.clear()
        finishedWallpaperHashmap.clear()
        searchGUI.removePictures()
    }

    fun handelSelectedPicture(id: String) {
        searchGUI.setCursorToLoading()
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
        wallpaper.thumbnail = saveThumbnail(wallpaper)
        addSavedWallpaperToLibrary(wallpaper)
    }

    fun saveImage(bytes: ByteArray, id: String) {
        val wallpaper = File("${configHashmap["downloadFolder"]}/$id.jpg")
        val fos = FileOutputStream(wallpaper)
        fos.write(bytes)
        fos.close()
    }

    fun saveThumbnail(wallpaperItem: Wallpaper): BufferedImage {
        val thumbnailImage = wallHaven.loadScaledThumbnail(wallpaperItem, thumbnailWidth).image
        val bi = BufferedImage(
            thumbnailImage.getWidth(null),
            thumbnailImage.getHeight(null),
            BufferedImage.TYPE_INT_RGB
        )

        val g2d = bi.createGraphics()
        g2d.drawImage(thumbnailImage, 0, 0, null)
        g2d.dispose()
        ImageIO.write(bi, "PNG", File("${configHashmap["thumbnailFolder"]}/${wallpaperItem.id}.png"))
        return bi
    }

    fun setThumbnailOnGui(item: Wallpaper, thumbnail: ImageIcon) {
        searchGUI.createPictureButton(thumbnail, item.id)
    }

    fun openPictureLibrary() {
        searchGUI.isVisible = false
        libraryGUI.isVisible = true
    }

    fun openSeachFrame() {
        searchGUI.isVisible = true
        libraryGUI.isVisible = false
    }

    fun <T> List<T>.toPair(): Pair<T, T> {
        if (this.size >= 2) return Pair(this[0], this[1])
        else throw (Exception("not enough elements for creating pair"))
    }

    fun checkPageFilter(filter: String): Boolean {
        val regex = Regex("""\d+\.\.-?\d+""")
        if (filter.matches(regex)) {
            val pages: Pair<Int, Int> = filter.split("..").map { it.toInt() }.toPair()
//            println("${pages.first} : ${pages.second}")
            return ((pages.first <= pages.second) xor (pages.second == -1)) && pages.first > 0
        }
        return false
    }

    fun getMaxPageOfQuery(query: wallHavenQuery): Int {
        return wallHaven.getNumberOfPagesOfQuery(query)
    }

    fun loadAllSavedPictures() {
        var downloadsFolder = File(configHashmap["downloadFolder"].toString()).listFiles()
        var thumbnailsFolder = File(configHashmap["thumbnailFolder"].toString()).listFiles()
        val thumbnailHashMap: HashMap<String, File> = HashMap()

        if (thumbnailsFolder == null) throw Exception("Thumbnails folder is null")
        if (downloadsFolder == null) throw Exception("Downloads folder is null")

        if (thumbnailsFolder.size != downloadsFolder.size) {
            hanleMissingImagaesOrThumbnails(downloadsFolder, thumbnailsFolder)
            downloadsFolder = File(configHashmap["downloadFolder"].toString()).listFiles()
            thumbnailsFolder = File(configHashmap["thumbnailFolder"].toString()).listFiles()

            // if files are still missing the program just gives up xD
            if (thumbnailsFolder == null) throw Exception("Thumbnails folder is null")
            if (downloadsFolder == null) throw Exception("Downloads folder is null")
            if(downloadsFolder.size != thumbnailsFolder.size) throw Exception("Couldn't load missing Files")
        }

        if (thumbnailsFolder.isEmpty()) throw Exception("Thumbnails folder is empty")
        if (downloadsFolder.isEmpty()) throw Exception("Thumbnails folder is empty")

        thumbnailsFolder.forEach {
            val id = it.name.split(".")[0]
            thumbnailHashMap[id] = it
        }

        for (file in downloadsFolder) {
            println("now loading ${file.name}")

            val id = file.name.split(".")[0]
            val wallpaper = Wallpaper(id, "", "")
            wallpaper.image = ImageIO.read(file)
            if (thumbnailHashMap[id] == null) throw (Exception("hashmap is null $id"))
            wallpaper.thumbnail = ImageIO.read(thumbnailHashMap[id])
            wallpaper.thumbnailDimension = Dimension(wallpaper.thumbnail.width, wallpaper.thumbnail.height)
            addSavedWallpaperToLibrary(wallpaper)
        }
    }

    fun hanleMissingImagaesOrThumbnails(downloadsFolder: Array<File>, thumbnailsFolder:  Array<File>) {
        val imageIDs: MutableList<String> = mutableListOf()
        downloadsFolder.forEach {
            imageIDs.add(it.name.split(".")[0])
        }



        thumbnailsFolder.forEach { thumb ->
            var checked = false
            for (imageId in imageIDs) {
                if(thumb.name.startsWith(imageId)) {
                    imageIDs.remove(imageId)
                    checked = true
                    break
                }
            }
            if(!checked) {
                val imageID =  thumb.name.split(".")[0]
                val imageUrl = wallHaven.getWallpaperInformation(imageID).getString("path")
                val imageBytes = wallHaven.getImage(imageUrl)

                saveImage(imageBytes, imageID)
            }
        }

        for (imageID in imageIDs) {
            val imageInfo = wallHaven.getWallpaperInformation(imageID)
            val thumbnailUrl = imageInfo.getJSONObject("data").getJSONObject("thumbs").getString("large")
            val wallpaper = Wallpaper(imageID, "", thumbnailUrl)
            saveThumbnail(wallpaper)
        }

    }

    fun addSavedWallpaperToLibrary(wallpaper: Wallpaper) {

        val resizedThumbnail = wallHaven.scaleImageIcon(ImageIcon(wallpaper.thumbnail), thumbnailWidth, wallpaper)
        libraryGUI.createPictureButton(resizedThumbnail, wallpaper.id)

        savedWallpapers.add(wallpaper)
    }

    fun distributeSavedPicturesToLibraryPanel() {
        val thread = Thread {
            println("Started Thread")
            SwingUtilities.invokeLater { libraryGUI.setCursorToLoading() }

            val scrollbarWidth = UIManager.getInt("ScrollBar.width").let { if (it > 0) it else 16 }
            thumbnailWidth = (libraryGUI.contentPanel.width - scrollbarWidth - 10) / 3

            loadAllSavedPictures()

            SwingUtilities.invokeLater { libraryGUI.setCursorToPointer()}
        }

        thread.start()
    }
}
package de.leo160905

import java.awt.Dimension
import java.awt.image.BufferedImage
import javax.swing.ImageIcon


sealed class Worksignal
data class Wallpaper(val id: String, val imageURL: String, val thumbnailURL: String) : Worksignal(){
    lateinit var thumbnail: ImageIcon
    lateinit var image: BufferedImage
    lateinit var size: Dimension
    var isAlreadyPainted = false
    var isInUse = false
}

object Done : Worksignal()
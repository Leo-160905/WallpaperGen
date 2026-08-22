package de.leo160905

import java.awt.Dimension
import java.awt.image.BufferedImage


sealed class Worksignal
data class Wallpaper(val id: String, val imageURL: String, val thumbnailURL: String) : Worksignal(){
    lateinit var thumbnailDimension: Dimension
    lateinit var image: BufferedImage
    lateinit var imageBytes: ByteArray
    lateinit var size: Dimension
}

object Done : Worksignal()
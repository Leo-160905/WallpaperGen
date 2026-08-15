package de.leo160905

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


val homeDir: String = System.getProperty("user.home")
val pixoraBaseDir: String = "$homeDir/.pixora"

val configHashmap = HashMap<String, String>()

fun main() {


    if (!File("$pixoraBaseDir/resource/").exists()) {
        println("making resource dir")
        File("$pixoraBaseDir/resource").mkdirs()
    }
    if (!File("$pixoraBaseDir/downloads").exists()) {
        println("making download dir")
        File("$pixoraBaseDir/downloads").mkdir()
    }
    if (!File("$pixoraBaseDir/thumbnails").exists()) {
        println("making thumbnail dir")
        File("$pixoraBaseDir/thumbnails").mkdir()
    }

    val file = File("$pixoraBaseDir/resource/settings.conf")

    if (!file.exists()) {

        print("enter api key: ")
        configHashmap["apiKey"] = readln()

        print("enter preferred Download folder (leave empty for default: ")
        var input = readln()
        configHashmap["downloadFolder"] = if (input != "") input else "$pixoraBaseDir/downloads"


        print("enter preferred Thumbnail folder (leave empty for default: ")
        input = readln()
        configHashmap["thumbnailFolder"] = if (input != "") input else "$pixoraBaseDir/thumbnails"

        val fos = FileOutputStream(file)
        configHashmap.forEach { (key, value) -> fos.write("$key=$value\n".toByteArray()) }
        fos.close()
    }
    else {
        val fis = FileInputStream(file)
        val content = fis.readBytes().toString(Charsets.UTF_8)
        println(content)
        getHashmapFromConfig(content).forEach { (k, v) -> configHashmap[k] = v }
        println(configHashmap.toString())
    }
    Controller()
}

fun getHashmapFromConfig(content: String): HashMap<String, String> {
    val hashmap = HashMap<String, String>()
    content.split("\n").forEach {
        if (it != "") {
            hashmap[it.split("=").first()] = it.split("=").last()
        }
    }
    return hashmap
}
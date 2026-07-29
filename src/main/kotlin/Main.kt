package de.leo160905

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64
import jdk.nashorn.internal.ir.BaseNode
import sun.print.resources.serviceui_pt_BR
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


val homeDir = System.getProperty("user.home")
val pixoraBaseDir = "$homeDir/.pixora"

val propertyHashmap = HashMap<String, String>()

fun main() {

    val file = File("$pixoraBaseDir/resource/settings.conf")
    if(!file.exists()) {
        if(!File("$pixoraBaseDir/resource/").exists()) {
            println("making resource dir")
            File("$pixoraBaseDir/resource").mkdirs()
        }
        if(!File("$pixoraBaseDir/downloads").exists()) {
            println("making download dir")
            File("$pixoraBaseDir/downloads").mkdir()
        }

        print("enter api key: ")
        propertyHashmap["apiKey"] = readln()

        print("enter preferred Download folder (leave empty for default: ")
        val input = readln()
        propertyHashmap["downloadFolder"] = if(input != "") input else "$pixoraBaseDir/downloads"
        
        val fos = FileOutputStream(file)
        propertyHashmap.forEach { (key, value) -> fos.write("$key=$value\n".toByteArray()) }
        fos.close()
    }
    else {
        val fis = FileInputStream(file)
        val content = fis.readBytes().toString(Charsets.UTF_8)
        println(content)
        getHashmapFromConf(content).forEach { (k, v) -> propertyHashmap[k] = v }
        println(propertyHashmap.toString())
    }
    Controller()
}

fun getHashmapFromConf(content: String): HashMap<String, String> {
    val hashmap = HashMap<String, String>()
    content.split("\n").forEach {
        if(it != "") {
            hashmap[it.split("=").first()] = it.split("=").last()
        }
    }
    return hashmap
}
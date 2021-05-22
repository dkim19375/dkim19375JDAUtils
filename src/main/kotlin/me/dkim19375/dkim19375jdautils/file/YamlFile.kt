package me.dkim19375.dkim19375jdautils.file

import me.dkim19375.dkim19375jdautils.annotation.API
import me.mattstudios.config.SettingsHolder
import me.mattstudios.config.SettingsManager
import me.mattstudios.config.properties.Property
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.notExists

@API
open class YamlFile(@API val properties: SettingsHolder, @API val fileName: String) {
    @API
    val path: Path
    @API
    val file: File
        get() = path.toFile()
    @API
    val manager: SettingsManager

    init {
        val array = fileName.replace("\\", "/").split("/").toTypedArray()
        if (array.isEmpty()) {
            throw IllegalArgumentException("The file path cannot be empty!")
        }
        val first = array[0]
        val rest = array.drop(1)
        val path = Paths.get(first, *rest.toTypedArray())
        this.path = path
        if (path.notExists()) {
            path.parent.createDirectories()
            path.createFile()
        }
        manager = SettingsManager.from(file).configurationData(properties.javaClass).create()
    }

    @API
    fun <T : Any> get(property: Property<T>): T = manager.get(property)

    @API
    fun <T : Any> set(property: Property<T>, value: T) = manager.set(property, value)

    @API
    fun reload() {
        if (path.notExists()) {
            path.parent.createDirectories()
            path.createFile()
        }
        manager.reload()
    }

    @API
    fun save() {
        if (path.notExists()) {
            path.parent.createDirectories()
            path.createFile()
        }
        manager.save()
    }
}
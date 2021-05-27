/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
package com.rk.file

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale

class FileWrapper(var file: File) : FileObject {
    override fun listFiles(): List<FileObject> {
        val list = file.listFiles()
        if (list.isNullOrEmpty()) {
            return emptyList()
        }
        return list.map { f -> FileWrapper(f) }
    }

    override fun isDirectory(): Boolean {
        return file.isDirectory
    }

    override fun isFile(): Boolean {
        return file.isFile
    }

    override fun getName(): String {
        return file.name
    }

    override fun getParentFile(): FileObject? {
        return file.parentFile?.let { FileWrapper(it) }
    }

    override fun exists(): Boolean {
        return file.exists()
    }

    override fun createNewFile(): Boolean {
        return file.createNewFile()
    }

    override fun mkdir(): Boolean {
        return file.mkdir()
    }

    override fun mkdirs(): Boolean {
        return file.mkdirs()
    }

    override fun writeText(text: String) {
        file.writeText(text)
    }

    override fun getInputStream(): InputStream {
        return FileInputStream(file)
    }

    override fun getOutPutStream(append: Boolean): OutputStream {
        return FileOutputStream(file, append)
    }

    override fun getAbsolutePath(): String {
        return file.absolutePath
    }

    override fun length(): Long {
        return file.length()
    }

    override fun delete(): Boolean {
        return if (isDirectory()) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    override fun toUri(): Uri {
        return file.toUri()
    }

    override fun getMimeType(context: Context): String? {
        val uri: Uri = Uri.fromFile(file)
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return if (extension != null) {
            MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension.lowercase(Locale.getDefault()))
        } else {
            context.contentResolver.getType(uri)
        }
    }

    override fun renameTo(string: String): Boolean {
        val newFile = File(file.parentFile, string)
        return file.renameTo(newFile).also { this.file = newFile }
    }

    override fun hasChild(name: String): Boolean {
        return File(file, name).exists()
    }

    override fun createChild(createFile: Boolean, name: String): FileObject? {
        if (createFile) {
            File(file, name).apply {
                createNewFile()
                return FileWrapper(this)
            }
        } else {
            File(file, name).apply {
                mkdir()
                return FileWrapper(this)
            }
        }
    }

    override fun canWrite(): Boolean {
        return file.canWrite()
    }

    override fun canRead(): Boolean {
        return file.canRead()
    }

    override fun hashCode(): Int {
        return getAbsolutePath().hashCode()
    }

    override fun toString(): String {
        return getAbsolutePath()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is FileWrapper) {
            return false
        }
        return other.getAbsolutePath() == getAbsolutePath()
    }
}
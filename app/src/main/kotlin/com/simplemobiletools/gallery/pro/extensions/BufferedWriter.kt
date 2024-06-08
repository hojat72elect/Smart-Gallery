package com.simplemobiletools.gallery.pro.extensions

import java.io.BufferedWriter

fun BufferedWriter.writeLn(line: String) {
    write(line)
    newLine()
}
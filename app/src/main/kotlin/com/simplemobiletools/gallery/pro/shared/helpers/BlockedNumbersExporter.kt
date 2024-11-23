package com.simplemobiletools.gallery.pro.shared.helpers

import com.simplemobiletools.gallery.pro.shared.data.domain.BlockedNumber
import java.io.OutputStream

object BlockedNumbersExporter {

    fun exportBlockedNumbers(
        blockedNumbers: ArrayList<BlockedNumber>,
        outputStream: OutputStream?,
        callback: (result: ExportResult) -> Unit,
    ) {
        if (outputStream == null) {
            callback.invoke(ExportResult.EXPORT_FAIL)
            return
        }

        try {
            outputStream.bufferedWriter().use { out ->
                out.write(blockedNumbers.joinToString(BLOCKED_NUMBERS_EXPORT_DELIMITER) {
                    it.number
                })
            }
            callback.invoke(ExportResult.EXPORT_OK)
        } catch (e: Exception) {
            callback.invoke(ExportResult.EXPORT_FAIL)
        }
    }
}

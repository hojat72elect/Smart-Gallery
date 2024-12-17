package ca.hojat.smart.gallery.feature_lock

interface HashListener {
    fun receivedHash(hash: String, type: Int)
}

package ca.hojat.smart.gallery.shared.data.domain

import kotlinx.serialization.Serializable

@Serializable
data class PhoneNumber(
    var value: String,
    var type: Int,
    var label: String,
    var normalizedNumber: String,
    var isPrimary: Boolean = false
)


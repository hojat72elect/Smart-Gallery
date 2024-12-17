package ca.hojat.smart.gallery.shared.ui.extensions

import androidx.compose.ui.Modifier

inline fun Modifier.ifTrue(predicate: Boolean, builder: () -> Modifier) =
    then(if (predicate) builder() else Modifier)

inline fun Modifier.ifFalse(predicate: Boolean, builder: () -> Modifier) =
    then(if (!predicate) builder() else Modifier)

inline infix fun (() -> Unit).andThen(crossinline function: () -> Unit): () -> Unit = {
    this()
    function()
}

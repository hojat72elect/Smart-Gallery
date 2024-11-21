package com.simplemobiletools.gallery.pro.compose.lists

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.compose.extensions.rememberMutableInteractionSource
import com.simplemobiletools.gallery.pro.compose.theme.SimpleTheme

@Composable
fun SimpleScaffoldTopBar(
    modifier: Modifier = Modifier,
    title: String,
    scrolledColor: Color,
    navigationIconInteractionSource: MutableInteractionSource = rememberMutableInteractionSource(),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState()
    ),
    statusBarColor: Int,
    colorTransitionFraction: Float,
    contrastColor: Color,
    goBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier
                    .padding(start = SimpleTheme.dimens.padding.medium)
                    .fillMaxWidth(),
                color = scrolledColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            SimpleNavigationIcon(
                goBack = goBack,
                navigationIconInteractionSource = navigationIconInteractionSource,
                iconColor = scrolledColor
            )
        },
        scrollBehavior = scrollBehavior,
        colors = simpleTopAppBarColors(statusBarColor, colorTransitionFraction, contrastColor),
        modifier = modifier.topAppBarPaddings(),
        windowInsets = topAppBarInsets()
    )
}

@Composable
fun SimpleScaffoldTopBar(
    modifier: Modifier = Modifier,
    title: @Composable (scrolledColor: Color) -> Unit,
    scrolledColor: Color,
    navigationIconInteractionSource: MutableInteractionSource = rememberMutableInteractionSource(),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        rememberTopAppBarState()
    ),
    statusBarColor: Int,
    colorTransitionFraction: Float,
    contrastColor: Color,
    goBack: () -> Unit,
) {
    TopAppBar(
        title = {
            title(scrolledColor)
        },
        navigationIcon = {
            SimpleNavigationIcon(
                goBack = goBack,
                navigationIconInteractionSource = navigationIconInteractionSource,
                iconColor = scrolledColor
            )
        },
        scrollBehavior = scrollBehavior,
        colors = simpleTopAppBarColors(statusBarColor, colorTransitionFraction, contrastColor),
        modifier = modifier.topAppBarPaddings(),
        windowInsets = topAppBarInsets()
    )
}

@Composable
fun simpleTopAppBarColors(
    statusBarColor: Int,
    colorTransitionFraction: Float,
    contrastColor: Color
) = TopAppBarDefaults.topAppBarColors(
    scrolledContainerColor = Color(statusBarColor),
    containerColor = if (colorTransitionFraction == 1f) contrastColor else SimpleTheme.colorScheme.surface,
    navigationIconContentColor = if (colorTransitionFraction == 1f) contrastColor else SimpleTheme.colorScheme.surface
)

@Composable
fun topAppBarInsets() = TopAppBarDefaults.windowInsets.exclude(WindowInsets.navigationBars)

@SuppressLint("ModifierFactoryUnreferencedReceiver")
@Composable
fun Modifier.topAppBarPaddings(
    paddingValues: PaddingValues = WindowInsets.navigationBars.asPaddingValues()
): Modifier {
    val layoutDirection = LocalLayoutDirection.current
    return padding(
        top = paddingValues.calculateTopPadding(),
        start = paddingValues.calculateStartPadding(layoutDirection),
        end = paddingValues.calculateEndPadding(layoutDirection)
    )
}

@Composable
fun SimpleNavigationIcon(
    modifier: Modifier = Modifier,
    navigationIconInteractionSource: MutableInteractionSource = rememberMutableInteractionSource(),
    goBack: () -> Unit,
    iconColor: Color? = null
) {
    Box(
        modifier
            .padding(start = SimpleTheme.dimens.padding.medium)
            .clip(RoundedCornerShape(50))
            .clickable(
                navigationIconInteractionSource, rememberRipple(
                    color = SimpleTheme.colorScheme.onSurface,
                    bounded = true
                )
            ) { goBack() }
    ) {
        SimpleBackIcon(iconColor)
    }
}

@Composable
fun SimpleBackIcon(iconColor: Color?) {
    if (iconColor == null) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.back),
            modifier = Modifier.padding(SimpleTheme.dimens.padding.small)
        )
    } else {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.back),
            tint = iconColor,
            modifier = Modifier.padding(SimpleTheme.dimens.padding.small)
        )
    }
}

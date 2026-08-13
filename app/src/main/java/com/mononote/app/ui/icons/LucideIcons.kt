/*
 * Lucide "archive" icon — ISC License.
 * Copyright (c) 2026 Lucide Icons and Contributors
 * https://lucide.dev/icons/archive
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 */
package com.mononote.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Lucide "archive": a box with a lid and a tab, stroke-drawn at 2dp weight. */
val LucideArchiveIcon: ImageVector
    get() {
        if (archiveIcon != null) return archiveIcon!!

        archiveIcon =
            ImageVector
                .Builder(
                    name = "archive",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).apply {
                    path(
                        fill = SolidColor(Color.Transparent),
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = 2f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                    ) {
                        moveTo(3f, 3f)
                        horizontalLineTo(21f)
                        arcTo(1f, 1f, 0f, false, true, 22f, 4f)
                        verticalLineTo(7f)
                        arcTo(1f, 1f, 0f, false, true, 21f, 8f)
                        horizontalLineTo(3f)
                        arcTo(1f, 1f, 0f, false, true, 2f, 7f)
                        verticalLineTo(4f)
                        arcTo(1f, 1f, 0f, false, true, 3f, 3f)
                        close()
                    }
                    path(
                        fill = SolidColor(Color.Transparent),
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = 2f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                    ) {
                        moveTo(4f, 8f)
                        verticalLineToRelative(11f)
                        arcToRelative(2f, 2f, 0f, false, false, 2f, 2f)
                        horizontalLineToRelative(12f)
                        arcToRelative(2f, 2f, 0f, false, false, 2f, -2f)
                        verticalLineTo(8f)
                    }
                    path(
                        fill = SolidColor(Color.Transparent),
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = 2f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                    ) {
                        moveTo(10f, 12f)
                        horizontalLineToRelative(4f)
                    }
                }.build()

        return archiveIcon!!
    }

private var archiveIcon: ImageVector? = null

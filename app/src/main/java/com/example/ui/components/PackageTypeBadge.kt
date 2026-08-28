package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PackageType
import com.example.ui.theme.CartonBadgeBg
import com.example.ui.theme.CartonBadgeText
import com.example.ui.theme.LooseBadgeBg
import com.example.ui.theme.LooseBadgeText

@Composable
fun PackageTypeBadge(
    packageType: PackageType,
    modifier: Modifier = Modifier
) {
    val isCarton = packageType == PackageType.CARTON
    val bgColor = if (isCarton) CartonBadgeBg else LooseBadgeBg
    val textColor = if (isCarton) CartonBadgeText else LooseBadgeText
    val label = if (isCarton) "Carton (CS)" else "Loose Pack"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}


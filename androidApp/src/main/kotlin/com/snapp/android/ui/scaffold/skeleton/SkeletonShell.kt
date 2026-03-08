package com.snapp.android.ui.scaffold.skeleton

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

@Composable
private fun shimmerColor(): Color {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    val base = MaterialTheme.colorScheme.onSurface
    return lerp(base.copy(alpha = 0.1f), base.copy(alpha = 0.2f), alpha)
}

@Composable
private fun SkeletonBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(6.dp)) {
    Box(modifier = modifier.clip(shape).background(shimmerColor()))
}

private val FigmaHeaderBarColor = Color(0xFFE8E8E8)

@Composable
fun SkeletonShell() {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Top bar skeleton (Figma: light gray)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(FigmaHeaderBarColor)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SkeletonBox(modifier = Modifier.size(28.dp), shape = RoundedCornerShape(4.dp))
            SkeletonBox(modifier = Modifier.width(120.dp).height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBox(modifier = Modifier.size(28.dp), shape = CircleShape)
                SkeletonBox(modifier = Modifier.size(28.dp), shape = CircleShape)
            }
        }

        // Content area skeleton
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(140.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SkeletonBox(modifier = Modifier.weight(1f).height(80.dp))
                SkeletonBox(modifier = Modifier.weight(1f).height(80.dp))
                SkeletonBox(modifier = Modifier.weight(1f).height(80.dp))
            }
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(200.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp))
        }

        // Bottom nav skeleton (Figma: light gray)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(FigmaHeaderBarColor)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(5) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    SkeletonBox(modifier = Modifier.size(24.dp), shape = RoundedCornerShape(4.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    SkeletonBox(modifier = Modifier.width(40.dp).height(10.dp))
                }
            }
        }
    }
}

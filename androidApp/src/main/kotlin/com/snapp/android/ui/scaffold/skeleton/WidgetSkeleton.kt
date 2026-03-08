package com.snapp.android.ui.scaffold.skeleton

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun WidgetSkeleton() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Skeleton(
                Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.75f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Skeleton(
                Modifier
                    .height(24.dp)
                    .fillMaxWidth(0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Skeleton(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
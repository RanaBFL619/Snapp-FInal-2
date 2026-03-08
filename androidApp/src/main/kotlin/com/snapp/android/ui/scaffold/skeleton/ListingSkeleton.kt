package com.snapp.android.ui.scaffold.skeleton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun ListingSkeleton(
    rows: Int = 7,
    columns: Int = 4
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        repeat(rows) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                repeat(columns) {

                    Skeleton(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    )

                }
            }
        }
    }
}
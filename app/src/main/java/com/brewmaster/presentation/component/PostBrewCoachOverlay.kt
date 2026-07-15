package com.brewmaster.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brewmaster.domain.model.BrewSymptom
import com.brewmaster.domain.usecase.BrewCoachAdvice
import com.brewmaster.presentation.theme.DarkBackground
import com.brewmaster.presentation.theme.DarkCard
import com.brewmaster.presentation.theme.LimeGreen
import com.brewmaster.presentation.theme.TextPrimary
import com.brewmaster.presentation.theme.TextSecondary

@Composable
fun PostBrewCoachOverlay(
    elapsedSec: Int,
    targetSec: Int,
    adviceFor: (BrewSymptom?) -> BrewCoachAdvice,
    onDone: () -> Unit
) {
    var selected by remember { mutableStateOf<BrewSymptom?>(null) }
    val advice = remember(selected, elapsedSec, targetSec) { adviceFor(selected) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How was the cup?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Brew time ${formatTime(elapsedSec)} (target ${formatTime(targetSec)})",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            BrewSymptom.entries.chunked(2).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    rowItems.forEach { symptom ->
                        Surface(
                            onClick = { selected = symptom },
                            shape = RoundedCornerShape(12.dp),
                            color = DarkCard,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${symptom.emoji} ${symptom.label}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected == symptom) LimeGreen else TextPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    advice.timeNote?.let {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = LimeGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = advice.diagnosis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Next cup (change ONE thing):",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = advice.primaryFix,
                        style = MaterialTheme.typography.titleSmall,
                        color = LimeGreen,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    advice.secondaryFixes.forEach { fix ->
                        Text(
                            text = "· $fix",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeGreen,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp)
            ) {
                Text("DONE", fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Skip taste → still get time-based tip above",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable(onClick = onDone)
            )
        }
    }
}

private fun formatTime(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)

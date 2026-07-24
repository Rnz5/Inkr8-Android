package com.inkr8.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Submissions
import com.inkr8.data.Users
import com.inkr8.economy.EconomyConfig
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.FormatUtils

@Composable
fun SubmissionsScreen(
    user: Users,
    submissions: List<Submissions>,
    isLoading: Boolean,
    onNavigateToProfile: () -> Unit,
    onSaveSubmission: (String) -> Unit
) {
    val archiveSubmissions = submissions.filter { !it.isSaved }
    var submissionToSave by remember { mutableStateOf<Submissions?>(null) }
    val totalSavedCount = submissions.count { it.isSaved }

    if (submissionToSave != null) {
        val saveCost = EconomyConfig.getSaveSubmissionCost(totalSavedCount)
        AlertDialog(
            onDismissRequest = { submissionToSave = null },
            title = { Text("Protect Writing") },
            text = { Text("Moving this entry to the Eternal Repository will cost $saveCost Merit. It will be removed from the Archive and permanently preserved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        submissionToSave?.let { onSaveSubmission(it.id) }
                        submissionToSave = null
                    }
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { submissionToSave = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Archive",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Writing History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            IconButton(
                onClick = onNavigateToProfile,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            ) {
                Text("✕", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (archiveSubmissions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("The archive is empty. Begin your climb.", color = Color.Gray)
            }
        } else {
            Text(
                text = "Archive Status: ${archiveSubmissions.size}/10 standard entries. $totalSavedCount protected.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(archiveSubmissions) { submission ->
                    SubmissionItem(
                        submission = submission, 
                        onSaveClick = { submissionToSave = submission },
                        currentSavedCount = totalSavedCount
                    )
                }
            }
        }
    }
}

@Composable
fun SubmissionItem(
    submission: Submissions, 
    onSaveClick: () -> Unit,
    currentSavedCount: Int
) {
    val saveCost = EconomyConfig.getSaveSubmissionCost(currentSavedCount)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = submission.gamemode,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = FormatUtils.formatDate(submission.timestamp) + " " + FormatUtils.formatTime(submission.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                
                val score = submission.evaluation?.finalScore ?: 0.0
                Text(
                    text = FormatUtils.formatPercentage(score),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = when {
                        score >= 80 -> Color(0xFF4CAF50)
                        score >= 60 -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = submission.content.let { if (it.length > 150) it.take(150) + "..." else it },
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoTag("${submission.wordCount} words")
                }

                TextButton(
                    onClick = onSaveClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Save - ${FormatUtils.formatMerit(saveCost.toLong())} Merit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InfoTag(text: String) {
    Box(
        modifier = Modifier.background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}

@Preview(showBackground = true)
@Composable
fun SubmissionsPreview() {
    Inkr8Theme {
        SubmissionsScreen(user = Users(), submissions = emptyList(), isLoading = false, onNavigateToProfile = {}, onSaveSubmission = {})
    }
}

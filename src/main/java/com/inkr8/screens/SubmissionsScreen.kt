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
    if (isLoading) {
        InitialLoadingScreen()
        return
    }

    val archiveSubmissions = submissions.filter { !it.isSaved }
    var submissionToSave by remember { mutableStateOf<Submissions?>(null) }
    val totalSavedCount = submissions.count { it.isSaved }

    if (submissionToSave != null) {
        val saveCost = EconomyConfig.getSaveSubmissionCost(totalSavedCount)
        AlertDialog(
            onDismissRequest = { submissionToSave = null },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Protect Writing", color = Color.White, fontWeight = FontWeight.Black) },
            text = { Text("Moving this entry to the Eternal Repository will cost ${FormatUtils.formatMerit(saveCost.toLong())} Merit. It will be removed from the Archive and permanently preserved.", color = Color.Gray) },
            confirmButton = {
                Button(
                    onClick = {
                        submissionToSave?.let { onSaveSubmission(it.id) }
                        submissionToSave = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { submissionToSave = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Archive",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFD700),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Writing History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            IconButton(
                onClick = onNavigateToProfile,
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                Text("✕", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (archiveSubmissions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("The archive is empty. Begin your climb.", color = Color.DarkGray, fontWeight = FontWeight.Bold)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = submission.gamemode.uppercase(),
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = FormatUtils.formatDate(submission.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                
                val score = submission.evaluation?.finalScore ?: 0.0
                Text(
                    text = FormatUtils.formatPercentage(score),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = submission.content.let { if (it.length > 150) it.take(150) + "..." else it },
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoTag("${submission.wordCount} words")

                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("SAVE • ${FormatUtils.formatMerit(saveCost.toLong())}", fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun InfoTag(text: String) {
    Box(
        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubmissionsPreview() {
    Inkr8Theme {
        SubmissionsScreen(user = Users(), submissions = emptyList(), isLoading = false, onNavigateToProfile = {}, onSaveSubmission = {})
    }
}

package com.inkr8.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.inkr8.data.Evaluation
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Submissions
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.FormatUtils

private val previewSubmission = Submissions(
    id = "preview-submission",
    authorId = "user123",
    content = "This is a preview paragraph written to test the visual structure of the new results screen. R8 is watching every keystroke, judging the structural integrity of your linguistic output.",
    timestamp = System.currentTimeMillis(),
    wordCount = 38,
    characterCount = 214,
    wordsUsed = emptyList(),
    gamemode = "STANDARD",
    evaluation = Evaluation(
        submissionId = "preview-submission",
        finalScore = 82.47,
        feedback = "Structural analysis complete. Your use of syntax is adequate but predictable. To reach elite status, you must abandon safe phrasing. The metrics indicate a high coherence score, yet your creativity index remains within common parameters. Refine your lexicon or remain forgotten in the archives.",
        resultStatus = SubmissionStatus.EVALUATED,
        meritEarned = 57,
        ratingChange = 5
    ),
    status = SubmissionStatus.EVALUATED,
    playmode = "RANKED"
)

@Composable
fun Results(
    submission: Submissions,
    isPlaced: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: () -> Unit,
) {
    val evaluation = submission.evaluation
        ?: return Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)), contentAlignment = Alignment.Center) {
            Text("R8 Judging...", color = Color.Gray, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        }

    val context = LocalContext.current
    val analyticsContext = LocalContext.current
    val firebaseAnalytics = remember { FirebaseAnalytics.getInstance(analyticsContext) }

    LaunchedEffect(submission.id) {
        val scoreBucket = when {
            evaluation.finalScore >= 80 -> "high"
            evaluation.finalScore >= 60 -> "mid"
            else -> "low"
        }
        firebaseAnalytics.logEvent("results_viewed") {
            param("playmode", submission.playmode)
            param("score_bucket", scoreBucket)
        }

        if (evaluation.isMock) {
            Toast.makeText(
                context,
                "Mock mode: the connection with GPT-4o mini isn't working, try again later",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val feedbackToShow = evaluation.feedback
    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundDark).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Verdict Delivered",
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Black
            )
            
            val isMock = evaluation.isMock
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background((if (isMock) Color.Gray else Color(0xFF4CAF50)).copy(alpha = 0.1f))
                    .border(1.dp, (if (isMock) Color.Gray else Color(0xFF4CAF50)).copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isMock) "Offline Mode" else "Synced",
                    color = if (isMock) Color.Gray else Color(0xFF4CAF50),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = FormatUtils.formatPercentage(evaluation.finalScore),
                fontSize = 76.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = (-3).sp
            )
            
            val appraisal = when {
                evaluation.finalScore >= 95 -> "GOD TIER. RARE PRECISION."
                evaluation.finalScore >= 90 -> "ELITE. SYSTEM ACKNOWLEDGED."
                evaluation.finalScore >= 80 -> "STRONG. ALMOST REFINED."
                evaluation.finalScore >= 70 -> "COMPETENT. STILL SAFE."
                evaluation.finalScore >= 60 -> "FINE. COMMON OUTPUT."
                else -> "WEAK. REWORK EVERYTHING."
            }
            
            Text(
                text = appraisal,
                color = primaryGold,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceDark),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (evaluation.isMock) "Mock Breakdown" else "R8 Breakdown",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = feedbackToShow,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 26.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Transmission Log",
                color = Color.DarkGray,
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = submission.content,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (submission.playmode == "RANKED" && submission.matchResult != null) {
                    val matchResult = submission.matchResult
                    val outcomeColor = when (matchResult.outcome) {
                        "WIN" -> Color(0xFF4CAF50)
                        "LOSS" -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                    val outcomeLabel = when (matchResult.outcome) {
                        "WIN" -> "Won"
                        "LOSS" -> "Lost"
                        else -> "Draw"
                    }
                    val opponentLabel = if (matchResult.opponentId == "GHOST") {
                        "Unmatched — compared against rolling average."
                    } else {
                        "You scored ${FormatUtils.formatScore(submission.evaluation?.finalScore ?: 0.0)}. " +
                        "${matchResult.opponentName} scored ${FormatUtils.formatScore(matchResult.opponentScore)}. " +
                        "You $outcomeLabel."
                    }

                    Text(
                        text = opponentLabel,
                        color = outcomeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Merit Gain", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "+${submission.evaluation?.meritEarned ?: 0}",
                            color = primaryGold,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.05f)))

                    if (submission.playmode == "RANKED") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Rating", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            if (isPlaced) {
                                val ratingChange = submission.matchResult?.ratingChange
                                    ?: submission.evaluation?.ratingChange ?: 0L
                                if (submission.matchStatus == "PENDING") {
                                    Text(
                                        text = "Pending",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                } else {
                                    val sign = if (ratingChange >= 0) "+" else ""
                                    Text(
                                        text = "$sign$ratingChange",
                                        color = if (ratingChange >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            } else {
                                Text(
                                    text = "Calibrating",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.05f)))
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Lexicon", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${submission.wordCount} Words",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Back", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNavigateToPractice,
                modifier = Modifier.weight(1.4f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text(
                    text = if (submission.playmode == "RANKED") "Enter Ranked Again" else "Practice Again",
                    fontWeight = FontWeight.Black
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResultsPreview() {
    Inkr8Theme {
        Results(
            submission = previewSubmission,
            isPlaced = false,
            onNavigateBack = {},
            onNavigateToPractice = {}
        )
    }
}

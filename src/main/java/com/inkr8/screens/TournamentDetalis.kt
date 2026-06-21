package com.inkr8.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.inkr8.R
import com.inkr8.data.Evaluation
import com.inkr8.data.Submissions
import com.inkr8.data.Tournament
import com.inkr8.data.TournamentLeaderboardEntry
import com.inkr8.data.TournamentStatus
import com.inkr8.data.Users
import com.inkr8.economy.TournamentRewardCalculator
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.FormatUtils
import com.inkr8.utils.TimeUtils
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TournamentDetails(
    tournament: Tournament,
    onNavigateBack: () -> Unit,
    onEnroll: () -> Unit = {},
    onSubmitToTournament: () -> Unit = {},
    onViewResults: () -> Unit = {},
    onHostClick: () -> Unit = {},
    onOpenSubmission: (Submissions) -> Unit = {},
    isEnrolled: Boolean = false,
    isSubmitted: Boolean = false,
    isEnrolling: Boolean = false,
    completedLeaderboard: List<TournamentLeaderboardEntry> = emptyList()
){
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    val formattedPrizePool = formatter.format(tournament.prizePool)
    val formattedEntryFee = formatter.format(tournament.entranceFee)

    val analyticsContext = LocalContext.current
    val firebaseAnalytics = remember { FirebaseAnalytics.getInstance(analyticsContext) }

    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

    val rewardPercentages =
        TournamentRewardCalculator.calculateRewardPercentages(tournament.maxPlayers.toInt())

    val timeText = when (tournament.status) {
        TournamentStatus.ENROLLING ->
            "ENROLLMENT ENDS IN ${TimeUtils.formatRemainingTime(tournament.enrollmentDeadline)}"
        TournamentStatus.ACTIVE ->
            "SUBMISSION ENDS IN ${TimeUtils.formatRemainingTime(tournament.submissionDeadline)}"
        TournamentStatus.EVALUATING ->
            "R8 IS EVALUATING THE SUBMISSIONS..."
        TournamentStatus.COMPLETED ->
            "TOURNAMENT COMPLETED"
        TournamentStatus.CANCELLED ->
            "TOURNAMENT CANCELLED"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("Back", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Arena Specifications",
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = tournament.title.ifBlank { "Untitled Arena" }.uppercase(),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceDark),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "REWARD DISTRIBUTION",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                RewardDistributionHeader(
                    showScoreInsteadOfPercent = tournament.status == TournamentStatus.COMPLETED && completedLeaderboard.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
                ) {
                    if (tournament.status == TournamentStatus.COMPLETED && completedLeaderboard.isNotEmpty()) {
                        completedLeaderboard.take(10).forEachIndexed { index, entry ->
                            val merit = entry.submission.evaluation?.meritEarned ?: 0L
                            val score = entry.submission.evaluation?.finalScore ?: 0.0
                            RewardDistributionRow(
                                place = FormatUtils.formatPlace(index + 1),
                                merit = formatter.format(merit),
                                percent = String.format(Locale.US, "%.2f", score),
                                participant = entry.user?.name?.ifBlank { null } ?: entry.submission.authorId,
                                onClick = { onOpenSubmission(entry.submission) },
                                primaryGold = primaryGold
                            )
                        }
                    } else {
                        rewardPercentages.take(10).forEachIndexed { index, percent ->
                            val merit = (tournament.prizePool * percent).toLong()
                            RewardDistributionRow(
                                place = FormatUtils.formatPlace(index + 1),
                                merit = formatter.format(merit),
                                percent = "${String.format(Locale.US, "%.2f", percent * 100)}%",
                                participant = "TBD",
                                onClick = {},
                                primaryGold = primaryGold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TournamentOverviewSection(
            tournament = tournament,
            formattedPrizePool = formattedPrizePool,
            formattedEntryFee = formattedEntryFee,
            timeText = timeText,
            onHostClick = onHostClick,
            primaryGold = primaryGold,
            surfaceDark = surfaceDark
        )

        Spacer(modifier = Modifier.height(24.dp))

        TournamentRequirementsSection(tournament = tournament, surfaceDark = surfaceDark)

        Spacer(modifier = Modifier.height(48.dp))

        val actionText = when (tournament.status) {
            TournamentStatus.ENROLLING -> when {
                isEnrolled -> "ALREADY ENROLLED"
                isEnrolling -> "ENROLLING..."
                else -> "ENROLL • $formattedEntryFee MERIT"
            }
            TournamentStatus.ACTIVE -> when {
                !isEnrolled -> "ENROLLMENT CLOSED"
                isSubmitted -> "SUBMISSION SENT"
                else -> "SUBMIT ENTRY"
            }
            TournamentStatus.EVALUATING -> "R8 IS JUDGING"
            TournamentStatus.COMPLETED -> "VIEW FINAL RESULTS"
            TournamentStatus.CANCELLED -> "ARENA DEACTIVATED"
        }

        val actionEnabled = when (tournament.status) {
            TournamentStatus.ENROLLING -> !isEnrolled && !isEnrolling
            TournamentStatus.ACTIVE -> isEnrolled && !isSubmitted
            TournamentStatus.EVALUATING -> false
            TournamentStatus.COMPLETED -> true
            TournamentStatus.CANCELLED -> false
        }

        Button(
            onClick = {
                when (tournament.status) {
                    TournamentStatus.ENROLLING -> {
                        firebaseAnalytics.logEvent("tournament_enrolled") {
                            param("tournament_id", tournament.id)
                        }
                        onEnroll()
                    }
                    TournamentStatus.ACTIVE -> onSubmitToTournament()
                    TournamentStatus.COMPLETED -> onViewResults()
                    else -> {}
                }
            },
            enabled = actionEnabled,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                disabledContentColor = Color.Gray
            )
        ) {
            Text(actionText, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun RewardDistributionHeader(
    showScoreInsteadOfPercent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "RANK",
            modifier = Modifier.width(45.dp),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Text(
            text = "MERIT",
            modifier = Modifier.width(75.dp),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Text(
            text = if (showScoreInsteadOfPercent) "SCORE" else "SHARE",
            modifier = Modifier.width(65.dp),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Text(
            text = "COMPETITOR",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
}

@Composable
private fun RewardDistributionRow(
    place: String,
    merit: String,
    percent: String,
    participant: String,
    onClick: () -> Unit,
    primaryGold: Color
) {
    val placeColor = when (place) {
        "1st" -> primaryGold
        "2nd" -> Color.LightGray
        "3rd" -> Color(0xFFCD7F32)
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = place,
            modifier = Modifier.width(45.dp),
            color = placeColor,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )

        Text(
            text = merit,
            modifier = Modifier.width(75.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Text(
            text = percent,
            modifier = Modifier.width(65.dp),
            color = Color.Gray,
            fontSize = 13.sp
        )

        Text(
            text = participant,
            modifier = Modifier.weight(1f),
            color = if (participant == "TBD") Color.DarkGray else Color.White,
            fontSize = 13.sp,
            fontWeight = if (participant == "TBD") FontWeight.Normal else FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun TournamentOverviewSection(
    tournament: Tournament,
    formattedPrizePool: String,
    formattedEntryFee: String,
    timeText: String,
    onHostClick: () -> Unit,
    primaryGold: Color,
    surfaceDark: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceDark),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onHostClick)
            ) {
                AsyncImage(
                    model = tournament.creatorImageURL.ifEmpty { R.drawable.pfpexample },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.pfpexample),
                    placeholder = painterResource(id = R.drawable.pfpexample)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tournament.creatorName.ifBlank { "Unknown Host" },
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Source Authority",
                        color = Color.DarkGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).border(1.dp, primaryGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tournament.gamemode.replace("_", " "),
                        color = primaryGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewStatBlock(label = "PRIZE POOL", value = formattedPrizePool, color = primaryGold)
                OverviewStatBlock(label = "ENTRY FEE", value = "$formattedEntryFee Merit", color = Color.White)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverviewStatBlock(label = "CAPACITY", value = "${tournament.playersCount}/${tournament.maxPlayers}", color = Color.White)
                OverviewStatBlock(label = "MINIMUM", value = "${tournament.minPlayers}", color = Color.White)
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.3f)).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeText,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun OverviewStatBlock(
    label: String,
    value: String,
    color: Color
) {
    Column(modifier = Modifier.width(140.dp)) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = color
        )
    }
}

@Composable
private fun TournamentRequirementsSection(
    tournament: Tournament,
    surfaceDark: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceDark),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ACCESS REQUIREMENTS",
                color = Color.Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RequirementItem("STATUS", tournament.status.name)
                
                tournament.requirements.minRating?.let { RequirementItem("MIN RATING", it.toString()) }
                tournament.requirements.maxRating?.let { RequirementItem("MAX RATING", it.toString()) }
                tournament.requirements.minReputation?.let { RequirementItem("MIN REPUTATION", it.toString()) }
                tournament.requirements.minMerit?.let { RequirementItem("MIN MERIT", it.toString()) }

                if (tournament.requirements.minRating == null &&
                    tournament.requirements.maxRating == null &&
                    tournament.requirements.minReputation == null &&
                    tournament.requirements.minMerit == null) {
                    Text(
                        text = "NO SPECIAL RESTRICTIONS ENFORCED",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun RequirementItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun previewTournament(): Tournament {
    return Tournament(
        id = "t1",
        title = "Precision Writing Arena",
        creatorId = "R8",
        creatorName = "R8",
        prizePool = 10000,
        entranceFee = 500,
        playersCount = 12,
        maxPlayers = 20,
        minPlayers = 5,
        gamemode = "ON_TOPIC",
        status = TournamentStatus.ENROLLING,
        enrollmentDeadline = System.currentTimeMillis() + 1000000,
        submissionDeadline = System.currentTimeMillis() + 2000000,
        requirements = com.inkr8.data.TournamentRequirements()
    )
}

@Composable
private fun previewLeaderboard(): List<TournamentLeaderboardEntry> {
    return listOf(
        TournamentLeaderboardEntry(
            submission = Submissions(
                id = "s1",
                authorId = "user1",
                content = "Sample",
                evaluation = Evaluation(
                    finalScore = 91.23,
                    meritEarned = 5000
                )
            ),
            user = Users(
                id = "USR_8492QW",
                name = "MintCake",
                email = "email example",
                merit = 1275,
                rating = 146,
                reputation = 42,
                bestScore = 91.4,
                submissionsCount = 38,
                profileImageURL = "",
                bannerImageURL = "",
                achievements = listOf(),
                joinedDate = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 120,
                rankedWinStreak = 2,
                rankedLossStreak = 0
            )
        ),
        TournamentLeaderboardEntry(
            submission = Submissions(
                id = "s2",
                authorId = "user2",
                content = "Sample",
                evaluation = Evaluation(
                    finalScore = 87.12,
                    meritEarned = 3000
                )
            ),
            user = Users(
                id = "USR_8492QW",
                name = "Shrimpy",
                email = "email example",
                merit = 1275,
                rating = 146,
                reputation = 42,
                bestScore = 91.4,
                submissionsCount = 38,
                profileImageURL = "",
                bannerImageURL = "",
                achievements = listOf(),
                joinedDate = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 120,
                rankedWinStreak = 2,
                rankedLossStreak = 0
            )
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TournamentDetailsPreview() {
    Inkr8Theme {
        TournamentDetails(
            tournament = previewTournament(),
            onNavigateBack = {},
            onEnroll = {},
            onSubmitToTournament = {},
            onViewResults = {},
            onHostClick = {},
            onOpenSubmission = {},
            isEnrolled = false,
            isSubmitted = false,
            isEnrolling = false,
            completedLeaderboard = previewLeaderboard()
        )
    }
}

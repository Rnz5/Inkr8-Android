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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.inkr8.R
import com.inkr8.data.Evaluation
import com.inkr8.data.SubmissionStatus
import com.inkr8.data.Submissions
import com.inkr8.data.Tournament
import com.inkr8.data.TournamentLeaderboardEntry
import com.inkr8.data.TournamentRequirements
import com.inkr8.data.TournamentStatus
import com.inkr8.data.Users
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.FormatUtils

@Composable
fun TournamentResultsScreen(
    tournament: Tournament,
    leaderboard: List<TournamentLeaderboardEntry>,
    isLoading: Boolean,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    onTipUser: (recipientId: String, amount: Long) -> Unit,
    onOpenUserProfile: (String) -> Unit
){
    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
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
            text = "Final Authority Report",
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = tournament.title.ifBlank { "Arena Archive" }.uppercase(),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxSize().padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceDark),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryGold)
                }
            } else if (leaderboard.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NO DATA LOGGED FOR THIS SESSION", color = Color.DarkGray, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp)
                ) {
                    ResultsHeader()

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        itemsIndexed(
                            items = leaderboard,
                            key = { _, item -> item.submission.id }
                        ) { index, entry ->
                            TournamentResultRow(
                                tournament = tournament,
                                placement = index + 1,
                                entry = entry,
                                currentUserId = currentUserId,
                                onTipUser = onTipUser,
                                onOpenUserProfile = onOpenUserProfile,
                                primaryGold = primaryGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader() {
    Column {
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
                text = "COMPETITOR",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Text(
                text = "SCORE",
                modifier = Modifier.width(60.dp),
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Text(
                text = "MERIT",
                modifier = Modifier.width(55.dp),
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentResultRow(
    tournament: Tournament,
    placement: Int,
    entry: TournamentLeaderboardEntry,
    currentUserId: String,
    onTipUser: (recipientId: String, amount: Long) -> Unit,
    onOpenUserProfile: (String) -> Unit,
    primaryGold: Color
) {
    val finalScore = entry.submission.evaluation?.finalScore ?: 0.0
    val meritEarned = entry.submission.evaluation?.meritEarned ?: 0L
    val displayName = entry.user?.name?.ifBlank { null } ?: entry.submission.authorId
    val isSelf = entry.submission.authorId == currentUserId

    var showSubmissionSheet by remember { mutableStateOf(false) }
    var showTipDialog by remember { mutableStateOf(false) }

    val placeColor = when (placement) {
        1 -> primaryGold
        2 -> Color.LightGray
        3 -> Color(0xFFCD7F32)
        else -> Color.Gray
    }

    if (showSubmissionSheet) {
        TournamentSubmissionBottomSheet(
            tournament = tournament,
            entry = entry,
            isSelf = isSelf,
            onDismiss = { showSubmissionSheet = false },
            onOpenProfile = {
                showSubmissionSheet = false
                onOpenUserProfile(entry.submission.authorId)
            },
            onTip = { amount -> onTipUser(entry.submission.authorId, amount) },
            primaryGold = primaryGold
        )
    }

    if (showTipDialog) {
        TipAmountDialog(
            recipientName = displayName,
            onDismiss = { showTipDialog = false },
            onSelectAmount = { amount ->
                showTipDialog = false
                onTipUser(entry.submission.authorId, amount)
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelf) Color.White.copy(alpha = 0.05f) else Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = FormatUtils.formatPlace(placement),
            modifier = Modifier.width(45.dp),
            fontWeight = FontWeight.Black,
            color = placeColor,
            fontSize = 13.sp
        )

        Row(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { showSubmissionSheet = true }.padding(horizontal = 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = entry.user?.profileImageURL?.ifEmpty { R.drawable.pfpexample } ?: R.drawable.pfpexample,
                contentDescription = null,
                modifier = Modifier.size(28.dp).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.pfpexample),
                placeholder = painterResource(id = R.drawable.pfpexample)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = FormatUtils.formatScore(finalScore),
            modifier = Modifier.width(60.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Text(
            text = FormatUtils.formatMerit(meritEarned),
            modifier = Modifier.width(55.dp),
            color = primaryGold,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )

        if (!isSelf) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .clickable { showTipDialog = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "TIP",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TournamentSubmissionBottomSheet(
    tournament: Tournament,
    entry: TournamentLeaderboardEntry,
    isSelf: Boolean,
    onDismiss: () -> Unit,
    onOpenProfile: () -> Unit,
    onTip: (Long) -> Unit,
    primaryGold: Color
) {
    val submission = entry.submission
    val evaluation = submission.evaluation
    val displayName = entry.user?.name?.ifBlank { null } ?: submission.authorId

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        scrimColor = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp).navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onOpenProfile() }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = entry.user?.profileImageURL?.ifEmpty { R.drawable.pfpexample } ?: R.drawable.pfpexample,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.pfpexample),
                    placeholder = painterResource(id = R.drawable.pfpexample)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "View Competitor Dossier",
                        color = primaryGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TRANSMITTED CONTENT",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = submission.content,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoMiniBlock(
                    label = "ACCURACY",
                    value = FormatUtils.formatPercentage(evaluation?.finalScore ?: 0.0),
                    modifier = Modifier.weight(1f),
                    primaryGold = primaryGold
                )
                InfoMiniBlock(
                    label = "MERIT EARNED",
                    value = FormatUtils.formatMerit(evaluation?.meritEarned ?: 0L),
                    modifier = Modifier.weight(1f),
                    primaryGold = primaryGold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isSelf) {
                Text(
                    text = "REWARD COMPETITOR",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(100L, 250L, 500L).forEach { amount ->
                        OutlinedButton(
                            onClick = { onTip(amount) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(FormatUtils.formatMerit(amount), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoMiniBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    primaryGold: Color
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = if (label.contains("MERIT")) primaryGold else Color.White
        )
    }
}

@Composable
private fun TipAmountDialog(
    recipientName: String,
    onDismiss: () -> Unit,
    onSelectAmount: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = { Text("Reward $recipientName", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select an amount of Merit to transfer from your balance.", color = Color.Gray, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100L, 250L, 500L).forEach { amount ->
                        Button(
                            onClick = { onSelectAmount(amount) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) {
                            Text(FormatUtils.formatMerit(amount), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    )
}

private val previewSubmission1 = Submissions(
    id = "sub1",
    authorId = "user1",
    content = "The structural integrity of the output remains within parameters.",
    evaluation = Evaluation(
        finalScore = 94.25,
        feedback = "Great work",
        meritEarned = 4500,
        rankLeaderboard = 1
    ),
    status = SubmissionStatus.EVALUATED
)

private val previewSubmission2 = Submissions(
    id = "sub2",
    authorId = "user2",
    content = "Syntactic variety is the core of linguistic evolution.",
    evaluation = Evaluation(
        finalScore = 89.75,
        feedback = "Strong writing",
        meritEarned = 2700,
        rankLeaderboard = 2
    ),
    status = SubmissionStatus.EVALUATED
)

private val previewTournament = Tournament(
    id = "t1",
    title = "Standard Writing Tournament - Edition #12",
    creatorId = "host1",
    creatorName = "R8",
    prizePool = 10000,
    maxPlayers = 20,
    minPlayers = 5,
    playersCount = 20,
    status = TournamentStatus.COMPLETED,
    requirements = TournamentRequirements()
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TournamentResultsScreenPreview() {
    Inkr8Theme {
        TournamentResultsScreen(
            tournament = previewTournament,
            leaderboard = listOf(
                TournamentLeaderboardEntry(
                    submission = previewSubmission1,
                    user = Users(id = "user1", name = "MintCake")
                ),
                TournamentLeaderboardEntry(
                    submission = previewSubmission2,
                    user = Users(id = "user2", name = "VelvetAsh")
                )
            ),
            isLoading = false,
            currentUserId = "user1",
            onNavigateBack = {},
            onTipUser = { _, _ -> },
            onOpenUserProfile = {}
        )
    }
}

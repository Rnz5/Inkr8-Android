package com.inkr8.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.inkr8.R
import com.inkr8.data.Users
import com.inkr8.economy.EconomyConfig
import com.inkr8.rating.League
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.FormatUtils

@Composable
fun Profile(
    user: Users,
    isOwner: Boolean,
    pantheonPosition: Int?,
    lastTippedTimestamp: Long? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSubmissions: () -> Unit,
    onNavigateToSavedSubmissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onPurchaseReputation: (onSuccess: () -> Unit) -> Unit,
    onTip: (Long) -> Unit = {}
) {
    val league = League.fromRating(user.rating)
    val scrollState = rememberScrollState()
    var showTipDialog by remember { mutableStateOf(false) }
    var isReputationRevealed by remember { mutableStateOf(false) }

    val cooldownMs = 24 * 60 * 60 * 1000L
    val now = System.currentTimeMillis()
    val isCooldownActive = lastTippedTimestamp != null && (now - lastTippedTimestamp < cooldownMs)
    
    val remainingCooldownText = if (isCooldownActive && lastTippedTimestamp != null) {
        val remaining = cooldownMs - (now - lastTippedTimestamp)
        val hours = remaining / (1000 * 60 * 60)
        "COOLDOWN ${hours}H"
    } else null

    if (showTipDialog) {
        TipAmountDialog(
            recipientName = user.name,
            onDismiss = { showTipDialog = false },
            onSelectAmount = { amount ->
                showTipDialog = false
                onTip(amount)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Text("←", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (isOwner) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Text("⚙", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.offset(y = (-50).dp)) {
                AsyncImage(
                    model = user.profileImageURL.ifEmpty { R.drawable.defaultpng },
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(
                            3.dp,
                            if (user.isPhilosopher) MaterialTheme.colorScheme.primary else Color(0xFFC0C0C0),
                            CircleShape
                        )
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.defaultpng),
                    placeholder = painterResource(id = R.drawable.defaultpng)
                )
            }

            Column(
                modifier = Modifier.offset(y = (-40).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                
                if (user.isPhilosopher) {
                    Text(
                        text = "Philosopher",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                }
                
                Text(
                    text = "Member since ${FormatUtils.formatDate(user.joinedDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray,
                    letterSpacing = 1.sp
                )
            }
        }

        if (isOwner) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).offset(y = (-20).dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Liquid Merit", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            val isDebt = user.merit < 0
                            val meritText = if (isDebt) {
                                "-${FormatUtils.formatMerit(java.lang.Math.abs(user.merit))}"
                            } else {
                                FormatUtils.formatMerit(user.merit)
                            }
                            Text(
                                text = meritText,
                                color = if (isDebt) MaterialTheme.colorScheme.error else Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    val progress = (user.merit.coerceAtLeast(0).toFloat() / user.meritCap.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                    Column {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Capacity: ${FormatUtils.formatMerit(user.meritCap)}", color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${(progress * 100).toInt()}%", color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val weeklyTax = (user.meritCap * 0.01).toLong()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("System Tax (Weekly)", color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${FormatUtils.formatMerit(weeklyTax)} Merit",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (user.meritHold > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.03f)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SRR (HOLD)", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text(
                                    text = FormatUtils.formatMerit(user.meritHold),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Slow Release Active",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = if (isOwner) 8.dp else 0.dp).offset(y = if (isOwner) 0.dp else (-20).dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (isOwner) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem("Rating", user.rating.toString(), if(pantheonPosition != null) "PANTHEON #$pantheonPosition" else league.displayName.uppercase())
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.05f)))
                    StatItem("Reputation", if(isReputationRevealed) user.reputation.toString() else "LOCKED", "BEHAVIORAL")
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StatItem("Rating", user.rating.toString(), if(pantheonPosition != null) "PANTHEON #$pantheonPosition" else league.displayName.uppercase())
                }
            }
        }

        SectionTitle("Battle History")
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BattleStatSmall(Modifier.weight(1f), "Submissions", user.submissionsCount.toString())
            BattleStatSmall(Modifier.weight(1f), "Tournaments", user.tournamentsPlayed.toString())
            BattleStatSmall(Modifier.weight(1f), "Victories", user.tournamentsWon.toString())
            BattleStatSmall(Modifier.weight(1f), "Best Score", FormatUtils.formatPercentage(user.bestScore))
        }
        
        Text(
            text = "Only counts Ranked and Tournaments scores",
            color = Color.DarkGray,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            SectionTitle("Competitive Curve")
            Text(
                text = "* PRACTICE SCORES ARE NOT TRACKED",
                color = Color.DarkGray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(160.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            if (user.recentScores.size < 2) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Awaiting competitive data...", color = Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                PerformanceChart(scores = user.recentScores, lineIndicatorColor = MaterialTheme.colorScheme.primary)
            }
        }

        if (isOwner) {
            SectionTitle("System Archive")
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileActionButton(
                    title = "Archive Entries",
                    subtitle = "Review and refine your history",
                    onClick = onNavigateToSubmissions,
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
                
                ProfileActionButton(
                    title = "Eternal Repository",
                    subtitle = "Locked and protected entries",
                    onClick = onNavigateToSavedSubmissions,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    showBorder = true
                )
            }

            SectionTitle("Behavioral Protocols")
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (isReputationRevealed) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "Reputation is fully integrated with system standing. Low standing increases entry fees and limits access to tournaments.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Button(
                        onClick = { onPurchaseReputation { isReputationRevealed = true } },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text("Reveal Reputation • ${FormatUtils.formatMerit(EconomyConfig.PURCHASE_REPUTATION_VIEW.toLong())} Merit", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        } else {
            if (pantheonPosition != null) {
                SectionTitle("System Interaction")
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { if (!isCooldownActive) showTipDialog = true },
                        enabled = !isCooldownActive,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCooldownActive) Color.White.copy(alpha = 0.05f) else Color.White,
                            contentColor = if (isCooldownActive) Color.Gray else Color.Black,
                            disabledContainerColor = Color.White.copy(alpha = 0.05f),
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        Text(remainingCooldownText ?: "Tip ${user.name}", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun PerformanceChart(scores: List<Double>, lineIndicatorColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp)) {
        val width = size.width
        val height = size.height
        val maxScore = 100f
        val minScore = 0f
        
        val spaceBetweenPoints = if (scores.size > 1) width / (scores.size - 1) else 0f
        
        val points = scores.mapIndexed { index, score ->
            val x = index * spaceBetweenPoints
            val y = height - ((score.toFloat() - minScore) / (maxScore - minScore)) * height
            Offset(x, y)
        }

        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * i / gridLines
            drawLine(
                color = Color.White.copy(alpha = 0.03f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (points.isNotEmpty()) {
            val path = Path()
            points.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(point.x, point.y)
                } else {
                    val prevPoint = points[index - 1]
                    path.cubicTo(
                        prevPoint.x + spaceBetweenPoints / 2, prevPoint.y,
                        point.x - spaceBetweenPoints / 2, point.y,
                        point.x, point.y
                    )
                }
            }

            drawPath(
                path = path,
                color = lineIndicatorColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            points.forEach { point ->
                drawCircle(
                    color = lineIndicatorColor,
                    radius = 3.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color(0xFF1A1A1A),
                    radius = 1.5.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Composable
fun ProfileActionButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    showBorder: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        border = if (showBorder) androidx.compose.foundation.BorderStroke(1.dp, contentColor.copy(alpha = 0.2f)) else null,
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                Text(text = subtitle.uppercase(), fontSize = 9.sp, color = contentColor.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
            }
            Text("→", fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StatItem(label: String, value: String, subValue: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(text = subValue, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
fun BattleStatSmall(modifier: Modifier, label: String, value: String) {
    Card(
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = label, fontSize = 8.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 12.dp),
        color = Color.DarkGray
    )
}

@Composable
private fun TipAmountDialog(
    recipientName: String,
    onDismiss: () -> Unit,
    onSelectAmount: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Tip $recipientName", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                TipOptionButton(100L, onSelectAmount)
                TipOptionButton(150L, onSelectAmount)
                TipOptionButton(200L, onSelectAmount)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun TipOptionButton(amount: Long, onClick: (Long) -> Unit) {
    Button(
        onClick = { onClick(amount) },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(80.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(FormatUtils.formatMerit(amount), fontWeight = FontWeight.Black)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfilePreview() {
    Inkr8Theme {
        Profile(
            user = Users(name = "MintCake", merit = 45000, meritCap = 50000, meritHold = 1250, isPhilosopher = true, reputation = 450),
            pantheonPosition = 4,
            isOwner = false,
            onNavigateBack = {},
            onNavigateToSubmissions = {},
            onNavigateToSavedSubmissions = {},
            onNavigateToSettings = {},
            onPurchaseReputation = {}
        )
    }
}

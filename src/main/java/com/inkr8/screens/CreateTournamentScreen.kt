package com.inkr8.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Users
import com.inkr8.economy.EconomyConfig.insufficientMerit
import com.inkr8.economy.TournamentEconomyCalculator
import com.inkr8.ui.theme.Inkr8Theme
import com.inkr8.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentScreen(
    user: Users,
    onCreate: (String, String, Long, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var gamemode by remember { mutableStateOf("STANDARD") }
    var prizePoolInput by remember { mutableStateOf("") }
    var maxPlayersInput by remember { mutableStateOf("20") }

    val prizePool = prizePoolInput.toLongOrNull() ?: 0L
    val maxPlayers = maxPlayersInput.toIntOrNull() ?: 0

    val projection = remember(prizePool, maxPlayers) {
        if (maxPlayers > 1) {
            TournamentEconomyCalculator.calculateProjection(
                prizePool = prizePool.coerceAtLeast(1L),
                maxPlayers = maxPlayers
            )
        } else {
            null
        }
    }

    val displayedPrizePool = if (prizePool > 0) prizePool else 0L
    val displayedEntryFee = projection?.entranceFee ?: 0L
    val displayedSystemFee = if (prizePool > 0) projection?.systemFee ?: 0L else 0L
    val totalCost = displayedPrizePool + displayedSystemFee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
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
            text = "Tournament Host",
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            text = "CREATE ARENA",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )

        Text(
            text = "Set the stakes. Define the rules. Watch them compete.",
            color = Color.DarkGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column {
                    Text(
                        text = "IDENTIFIER",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("E.g. Precision Writing Open", color = Color.DarkGray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Column {
                    Text(
                        text = "CORE DIRECTIVE",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GamemodeSelectorChip(
                            text = "Standard",
                            selected = gamemode == "STANDARD",
                            onClick = { gamemode = "STANDARD" },
                            modifier = Modifier.weight(1f)
                        )
                        GamemodeSelectorChip(
                            text = "On-Topic",
                            selected = gamemode == "ON_TOPIC",
                            onClick = { gamemode = "ON_TOPIC" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PRIZE POOL",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = prizePoolInput,
                            onValueChange = { prizePoolInput = it.filter(Char::isDigit) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text(
                            text = "CAPACITY",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = maxPlayersInput,
                            onValueChange = { maxPlayersInput = it.filter(Char::isDigit) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "ECONOMY PROJECTION",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Entry Fee", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = FormatUtils.formatMerit(displayedEntryFee),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("System Fee", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = FormatUtils.formatMerit(displayedSystemFee),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    EconomyStatBlock(
                        label = "Prize Pool",
                        value = FormatUtils.formatMerit(displayedPrizePool)
                    )
                    EconomyStatBlock(
                        label = "Total Cost",
                        value = FormatUtils.formatMerit(totalCost)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                when {
                    title.isBlank() -> {
                        Toast.makeText(context, "Tournament title is required", Toast.LENGTH_SHORT).show()
                    }
                    prizePool < 5000L -> {
                        Toast.makeText(context, "The minimum prize pool is 5,000 Merit", Toast.LENGTH_SHORT).show()
                    }
                    maxPlayers < 2 -> {
                        Toast.makeText(context, "At least 2 players are required", Toast.LENGTH_SHORT).show()
                    }
                    user.merit < totalCost -> {
                        Toast.makeText(context, insufficientMerit(), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        onCreate(title, gamemode, prizePool, maxPlayers)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text(
                text = "INITIALIZE ARENA • ${FormatUtils.formatMerit(totalCost)} Merit",
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun GamemodeSelectorChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) Color.White.copy(alpha = 0.05f) else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else Color.Gray

    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(containerColor).border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ).clickable(onClick = onClick).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun EconomyStatBlock(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

private val previewCreateTournamentUser = Users(
    id = "USR_8492QW",
    name = "MintCake",
    email = "email example",
    merit = 18000,
    rating = 86,
    reputation = 42,
    bestScore = 91.4,
    submissionsCount = 38,
    profileImageURL = "",
    bannerImageURL = "",
    achievements = listOf(),
    joinedDate = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 120,
    rankedWinStreak = 3,
    rankedLossStreak = 0
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateTournamentScreenPreview() {
    Inkr8Theme {
        CreateTournamentScreen(
            user = previewCreateTournamentUser,
            onCreate = { _, _, _, _ -> },
            onBack = {}
        )
    }
}

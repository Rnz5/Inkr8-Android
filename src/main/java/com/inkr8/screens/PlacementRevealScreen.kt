package com.inkr8.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.rating.League
import kotlinx.coroutines.delay

@Composable
fun PlacementRevealScreen(
    league: League,
    onContinue: () -> Unit
) {
    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)

    var showLeague by remember { mutableStateOf(false) }
    var showVerdict by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    val leagueAlpha by animateFloatAsState(
        targetValue = if (showLeague) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "leagueAlpha"
    )

    val verdictAlpha by animateFloatAsState(
        targetValue = if (showVerdict) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "verdictAlpha"
    )

    LaunchedEffect(Unit) {
        delay(600)
        showLeague = true
        delay(1200)
        showVerdict = true
        delay(900)
        showButton = true
    }

    val verdict = when (league) {
        League.SCRIBE -> "After 6 matches, the system has reached a conclusion. You are a Scribe. The entry level. Prove it wrong."
        League.STYLIST -> "After 6 matches, the system has reached a conclusion. You are a Stylist. Competent. Not yet memorable."
        League.AUTHOR -> "After 6 matches, the system has reached a conclusion. You are an Author. For now."
        League.NOVELIST -> "After 6 matches, the system has reached a conclusion. You are a Novelist. The system is paying attention."
        League.LAUREATE -> "After 6 matches, the system has reached a conclusion. You are a Laureate. Few reach this. R8 is skeptical it will last."
        League.LUMINARY -> "After 6 matches, the system has reached a conclusion. You are a Luminary. This is rare. Do not waste it."
    }

    Box(
        modifier = Modifier.fillMaxSize().background(backgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Calibration Complete",
                color = primaryGold.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = league.displayName.uppercase(),
                color = primaryGold,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                modifier = Modifier.alpha(leagueAlpha)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = verdict,
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.alpha(verdictAlpha)
            )

            Spacer(modifier = Modifier.height(56.dp))

            if (showButton) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text(
                        "Enter the System",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

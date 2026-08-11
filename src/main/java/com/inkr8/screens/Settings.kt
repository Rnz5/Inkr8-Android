package com.inkr8.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.data.Users
import com.inkr8.economy.EconomyConfig
import com.inkr8.utils.FormatUtils

@Composable
fun Settings(
    user: Users,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onChangeUsername: () -> Unit,
    onExpandCap: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangeUsernameDialog by remember { mutableStateOf(false) }
    var showExpandCapDialog by remember { mutableStateOf(false) }

    val primaryGold = Color(0xFFFFD700)
    val backgroundDark = Color(0xFF0F0F0F)
    val surfaceDark = Color(0xFF1A1A1A)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = surfaceDark,
            title = { Text("Dissolve Identity", color = Color.White) },
            text = { Text("This will permanently delete your account and release your username. This action cannot be undone.", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDeleteAccount() }) {
                    Text("Dissolve", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = Color.White) } }
        )
    }

    if (showChangeUsernameDialog) {
        AlertDialog(
            onDismissRequest = { showChangeUsernameDialog = false },
            containerColor = surfaceDark,
            title = { Text("Rebrand Identity", color = Color.White) },
            text = { Text("Changing your username will cost ${FormatUtils.formatMerit(EconomyConfig.CHANGE_USERNAME.toLong())} Merit. Continue?", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { showChangeUsernameDialog = false; onChangeUsername() }) {
                    Text("Continue", color = primaryGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showChangeUsernameDialog = false }) { Text("Cancel", color = Color.White) } }
        )
    }

    if (showExpandCapDialog) {
        val expandCost = (user.meritCap * 0.25).toLong()
        AlertDialog(
            onDismissRequest = { showExpandCapDialog = false },
            containerColor = surfaceDark,
            title = { Text("Expand Merit Cap", color = primaryGold, fontWeight = FontWeight.Black) },
            text = { 
                Column {
                    Text("Current Cap: ${FormatUtils.formatMerit(user.meritCap)}", color = Color.White)
                    Text("Expansion Cost: ${FormatUtils.formatMerit(expandCost)} Merit", color = primaryGold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This will increase your liquid capacity by 10,000 merit. Extra earnings are currently stored in SRR (Hold).", color = Color.Gray, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExpandCapDialog = false; onExpandCap() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Expand", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = { TextButton(onClick = { showExpandCapDialog = false }) { Text("Cancel", color = Color.White) } }
        )
    }

    Scaffold(
        containerColor = backgroundDark,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Text("←", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "System Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(horizontal = 20.dp)
        ) {
            SectionTitle("Economy Management")
            SettingsItem(
                title = "Expand Merit Cap",
                subtitle = "Increase your liquid capacity",
                onClick = { showExpandCapDialog = true },
                color = Color.White
            )

            SectionTitle("Identity Management")
            SettingsItem(
                title = "Modify Identity",
                subtitle = "Change your system name",
                onClick = { showChangeUsernameDialog = true },
                color = Color.White
            )

            SectionTitle("Security")
            SettingsItem(
                title = "Logout",
                subtitle = "Terminate current session",
                onClick = onLogout,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.1f),
                    contentColor = Color.Red
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
            ) {
                Text("DISSOLVE IDENTITY", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    color: Color
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Text("→", color = Color.DarkGray, fontWeight = FontWeight.Black)
        }
    }
}

package com.inkr8.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.inkr8.R
import com.inkr8.data.Users
import com.inkr8.rating.League

@Composable
fun UserHeaderCard(
    user: Users,
    pantheonPosition: Int?,
    onClick: () -> Unit
) {
    val league = League.fromRating(user.rating)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = user.profileImageURL.ifEmpty { R.drawable.pfpexample },
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = if (user.isPhilosopher) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.pfpexample),
                        placeholder = painterResource(id = R.drawable.pfpexample)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (pantheonPosition != null) "Pantheon #$pantheonPosition" else league.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (pantheonPosition != null || user.isPhilosopher) MaterialTheme.colorScheme.primary else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        
                        if (user.isPhilosopher) {
                            Spacer(modifier = Modifier.width(6.6.dp))
                            Text(
                                text = "•",
                                color = Color.DarkGray,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(6.6.dp))
                            Text(
                                text = "Philosopher",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = FormatUtils.formatMerit(user.merit),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Liquid Merit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            val progress = (user.merit.toFloat() / user.meritCap.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
            val isNearCap = progress > 0.9f
            
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = if (isNearCap) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Capacity: ${FormatUtils.formatMerit(user.meritCap)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (user.meritHold > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SRR Active: ${FormatUtils.formatMerit(user.meritHold)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

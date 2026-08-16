package com.inkr8.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inkr8.ui.theme.Inkr8Theme

@Composable
fun LoginScreen(
    onGoogleLogin: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 60.dp)
            ) {
                Text(
                    text = "Inkr8",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 8.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Write and Learn",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.width(40.dp).clip(CircleShape),
                    thickness = 2.dp,
                    color = Color.White.copy(alpha = 0.1f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "“A good artist can paint with any color;\na great writer should write with any word.”",
                    color = Color.Gray,
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider(
                    modifier = Modifier.width(40.dp).clip(CircleShape),
                    thickness = 2.dp,
                    color = Color.White.copy(alpha = 0.1f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)
            ) {
                Text(
                    text = "Verify your Identity",
                    color = Color.DarkGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onGoogleLogin,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = "Login",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Proceed with Google Authentication",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    Inkr8Theme {
        LoginScreen(onGoogleLogin = {})
    }
}

package com.example.myapplication.ui.screens.disguise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.DarkNavy

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.platform.LocalContext

/**
 * App Camouflage Disguise Mode — Realistic Calculator Mask.
 *
 * Safe Design Rules:
 * 1. User sets a secret pattern (e.g. 7777= to unlock app, 9999= for SOS).
 * 2. Calculator accepts pattern discreetly.
 * 3. Gives a small vibration confirmation — NO obvious popup.
 * 4. Sends encrypted SOS, GPS location, timestamp, and temporary tracking link immediately to contacts.
 * 5. Shows harmless calculator screen while transmitting in background.
 * 6. Audio/video evidence recording requires prior explicit user consent.
 */
@Composable
fun CalculatorDisguiseScreen(
    secretUnlockPattern: String = "7777",
    secretSosPattern: String = "9999",
    onUnlockApp: () -> Unit,
    onTriggerDisguisedSos: () -> Unit
) {
    val context = LocalContext.current
    var displayValue by remember { mutableStateOf("0") }
    var inputBuffer by remember { mutableStateOf("") }

    fun triggerSubtleHapticVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(VibratorManager::class.java)
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (e: Exception) {
            // Ignore if vibration unavailable
        }
    }

    fun handleButtonClick(label: String) {
        when (label) {
            "AC" -> {
                displayValue = "0"
                inputBuffer = ""
            }
            "C" -> {
                if (inputBuffer.isNotEmpty()) {
                    inputBuffer = inputBuffer.dropLast(1)
                    displayValue = if (inputBuffer.isEmpty()) "0" else inputBuffer
                }
            }
            "=" -> {
                // Check secret passcodes
                if (inputBuffer == secretUnlockPattern) {
                    triggerSubtleHapticVibration()
                    displayValue = "0"
                    inputBuffer = ""
                    onUnlockApp()
                    return
                }
                if (inputBuffer == secretSosPattern) {
                    // Discreet Vibration Confirmation — NO obvious popup!
                    triggerSubtleHapticVibration()
                    
                    // Show harmless standard result on calculator while transmitting in background
                    displayValue = "0"
                    inputBuffer = ""
                    
                    // Immediately send encrypted SOS, GPS location, timestamp, and tracking link
                    onTriggerDisguisedSos()
                    return
                }

                // Standard calculation evaluation
                try {
                    val result = evaluateSimpleExpr(inputBuffer)
                    displayValue = result
                    inputBuffer = result
                } catch (e: Exception) {
                    displayValue = "Error"
                    inputBuffer = ""
                }
            }
            else -> {
                if (displayValue == "0" || displayValue == "Error") {
                    inputBuffer = label
                } else {
                    inputBuffer += label
                }
                displayValue = inputBuffer
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Disguise Title Bar (subtle)
        Text(
            "Calculator",
            color = Color.Gray.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Display Screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF121E32), RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = displayValue,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Calculator Buttons Grid (4x5)
        val buttons = listOf(
            listOf("AC", "C", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "±", "=")
        )

        for (row in buttons) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (btn in row) {
                    val isOperator = btn in listOf("÷", "×", "-", "+", "=")
                    val isFunction = btn in listOf("AC", "C", "%", "±")
                    val btnColor = when {
                        btn == "=" -> Color(0xFF00BFA5) // Teal accent for =
                        isOperator -> Color(0xFF26C6DA)
                        isFunction -> Color(0xFF1A2940)
                        else -> Color(0xFF1E2D45)
                    }

                    Button(
                        onClick = { handleButtonClick(btn) },
                        colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    ) {
                        Text(
                            text = btn,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun evaluateSimpleExpr(expr: String): String {
    if (expr.isBlank()) return "0"
    val clean = expr.replace("×", "*").replace("÷", "/")
    // Simple 2-operand calculator parser
    val opMatch = Regex("([0-9.]+)\\s*([+\\-*/])\\s*([0-9.]+)").find(clean)
    if (opMatch != null) {
        val num1 = opMatch.groupValues[1].toDoubleOrNull() ?: 0.0
        val op = opMatch.groupValues[2]
        val num2 = opMatch.groupValues[3].toDoubleOrNull() ?: 0.0
        val res = when (op) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*" -> num1 * num2
            "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
            else -> num1
        }
        return if (res.isNaN()) "Error" else if (res % 1.0 == 0.0) res.toLong().toString() else "%.2f".format(res)
    }
    return expr
}

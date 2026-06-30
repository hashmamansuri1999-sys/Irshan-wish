package com.example

import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          WishManifestationScreen()
        }
      }
    }
  }
}

// Particle data class for background cosmic floaters
data class CosmicParticle(
  val id: Int,
  val initialX: Float, // percentage 0..1
  val initialY: Float, // percentage 0..1
  val speedY: Float,
  val size: Float,
  val baseAlpha: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishManifestationScreen() {
  val context = LocalContext.current
  val keyboardController = LocalSoftwareKeyboardController.current

  // State Management
  var isDarkPowers by remember { mutableStateOf(true) } // True = Dark Powers (Vibrating cosmic), False = Light Powers (Warm celestial)
  var energyReleasedCount by remember { mutableStateOf(0) }
  var wishesRevealed by remember { mutableStateOf(false) }
  val sharedPreferences = remember { context.getSharedPreferences("cosmic_echo_prefs", Context.MODE_PRIVATE) }
  var wishesRevealCount by remember { mutableStateOf(sharedPreferences.getInt("reveal_count", 0)) }
  var userIntentionInput by remember { mutableStateOf("") }
  val customIntentions = remember { mutableStateListOf<String>() }

  var userGratitudeInput by remember { mutableStateOf("") }
  val dailyGratitudes = remember { mutableStateListOf<String>() }

  LaunchedEffect(Unit) {
    val notesString = sharedPreferences.getString("gratitude_notes_list", "") ?: ""
    if (notesString.isNotEmpty()) {
      val list = notesString.split("|||")
      dailyGratitudes.clear()
      dailyGratitudes.addAll(list.filter { it.isNotBlank() })
    }

    val intentionsString = sharedPreferences.getString("custom_intentions_list", "") ?: ""
    if (intentionsString.isNotEmpty()) {
      val list = intentionsString.split("|||")
      customIntentions.clear()
      customIntentions.addAll(list.filter { it.isNotBlank() })
    }
  }

  val affirmationsList = remember {
    listOf(
      "The universe aligns every cosmic force to manifest our purest intentions.",
      "Infinite abundance flows effortlessly into our lives, opening doors of golden opportunities.",
      "Every deity, realm, and dimension works in perfect, beautiful unison for our success.",
      "We are attuned to the cosmic frequency of wealth, health, and endless love.",
      "Every single dream and wish of Irshan and Hurain is crystallizing into reality.",
      "All dark and light powers harmonize perfectly to protect, elevate, and serve our path.",
      "Success and ultimate prosperity are drawn to our family like stardust to a beacon.",
      "The eternal bond of Irshan and Hurain holds the power of infinite constellations.",
      "Each focused breath elevates our alignment, sending ripples of manifestation to the stars.",
      "We walk in absolute abundance; our father's endeavors are crowned with supreme victory."
    )
  }
  var currentAffirmation by remember { mutableStateOf(affirmationsList.random()) }

  // Theme configuration following the Editorial Aesthetic
  val backgroundBrush = if (isDarkPowers) {
    Brush.verticalGradient(
      colors = listOf(
        EditorialBg,
        Color(0xFF151418),
        Color(0xFF0F0E11)
      )
    )
  } else {
    Brush.verticalGradient(
      colors = listOf(
        EditorialBgLight,
        Color(0xFFF5EFEB),
        Color(0xFFEDE5DF)
      )
    )
  }

  val activeAccentColor = if (isDarkPowers) EditorialAccentPink else EditorialAccentPinkLight
  val activeSecondaryColor = if (isDarkPowers) EditorialAccentPurple else EditorialAccentPurpleLight
  val textOnBg = if (isDarkPowers) EditorialText else EditorialTextLight
  val subTextOnBg = if (isDarkPowers) EditorialSubtext else EditorialSubtextLight
  val mutedTextOnBg = if (isDarkPowers) EditorialMuted else EditorialMutedLight
  val cardBackground = if (isDarkPowers) EditorialCardBg else EditorialCardBgLight
  val cardBorderColor = if (isDarkPowers) activeSecondaryColor.copy(alpha = 0.25f) else activeAccentColor.copy(alpha = 0.2f)

  // Decorative tags styling
  val chip1Bg = if (isDarkPowers) Color(0xFF31111D) else Color(0xFFFDE0E4)
  val chip1Text = if (isDarkPowers) EditorialAccentPink else EditorialAccentPinkLight
  val chip1Border = if (isDarkPowers) Color(0xFFFFB2BE).copy(alpha = 0.2f) else Color(0xFFFFB2BE).copy(alpha = 0.5f)

  val chip2Bg = if (isDarkPowers) Color(0xFF21005D) else Color(0xFFE8E2F7)
  val chip2Text = if (isDarkPowers) EditorialAccentPurple else EditorialAccentPurpleLight
  val chip2Border = if (isDarkPowers) EditorialAccentPurple.copy(alpha = 0.2f) else EditorialAccentPurpleLight.copy(alpha = 0.5f)

  val chip3Bg = if (isDarkPowers) EditorialBg else EditorialBgLight
  val chip3Text = if (isDarkPowers) textOnBg.copy(alpha = 0.5f) else textOnBg.copy(alpha = 0.5f)
  val chip3Border = if (isDarkPowers) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.15f)

  // Particle Generation for Interactive Background
  val particles = remember {
    List(35) { id ->
      CosmicParticle(
        id = id,
        initialX = Random.nextFloat(),
        initialY = Random.nextFloat(),
        speedY = 0.002f + Random.nextFloat() * 0.003f,
        size = 2.5f + Random.nextFloat() * 3.5f,
        baseAlpha = 0.15f + Random.nextFloat() * 0.5f
      )
    }
  }

  // Animation ticks for particles
  var animTick by remember { mutableStateOf(0f) }
  LaunchedEffect(Unit) {
    while (true) {
      animTick += 0.01f
      if (animTick > 1f) animTick = 0f
      delay(35)
    }
  }

  // Orb Pulse Animation
  val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 0.65f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = EaseInOutSine),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
  )

  Scaffold(
    modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
    contentWindowInsets = WindowInsets.safeDrawing
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(backgroundBrush)
        .padding(paddingValues)
    ) {
      
      // Cosmic Twinkling Stardust & Starfield Renderer
      Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
          val animatedY = (particle.initialY - (animTick * particle.speedY * 100)) % 1.0f
          val finalY = if (animatedY < 0) animatedY + 1.0f else animatedY
          val xOffset = particle.initialX * size.width
          val yOffset = finalY * size.height
          
          // Smooth twinkling calculation using trigonometric phase offsets
          val twinklePhase = (animTick * 25f) + (particle.id * 2.0f)
          val twinkleFactor = kotlin.math.sin(twinklePhase) * 0.4f + 0.6f
          val dynamicAlpha = (particle.baseAlpha * twinkleFactor).coerceIn(0.08f, 0.95f)
          
          // Draw the primary star core
          drawCircle(
            color = activeAccentColor.copy(alpha = dynamicAlpha),
            radius = particle.size,
            center = Offset(xOffset, yOffset)
          )
          
          // For special celestial stars (e.g., every 4th star), add a subtle four-point twinkling flare
          if (particle.id % 4 == 0) {
            val flareLen = particle.size * (2.5f + twinkleFactor * 1.5f)
            // Horizontal flare line
            drawLine(
              color = activeAccentColor.copy(alpha = dynamicAlpha * 0.5f),
              start = Offset(xOffset - flareLen, yOffset),
              end = Offset(xOffset + flareLen, yOffset),
              strokeWidth = 1.dp.toPx()
            )
            // Vertical flare line
            drawLine(
              color = activeAccentColor.copy(alpha = dynamicAlpha * 0.5f),
              start = Offset(xOffset, yOffset - flareLen),
              end = Offset(xOffset, yOffset + flareLen),
              strokeWidth = 1.dp.toPx()
            )
          }
        }
      }

      // Scrollable content
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        
        // Header Row following "Status Bar & Header Area" design
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "MANIFESTATION ENGINE v.02",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = activeSecondaryColor,
              letterSpacing = 2.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Universal Echo",
              fontSize = 26.sp,
              fontWeight = FontWeight.Light,
              color = textOnBg,
              fontFamily = FontFamily.Serif,
              letterSpacing = (-0.5).sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }

          // Decorative glowing alignment orb status (M3 layout match)
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(if (isDarkPowers) Color(0xFF49454F) else Color(0xFFE5DDD9))
              .border(1.dp, mutedTextOnBg.copy(alpha = 0.3f), CircleShape)
              .clickable {
                isDarkPowers = !isDarkPowers
                triggerHapticFeedback(context)
                android.widget.Toast.makeText(context, if (isDarkPowers) "Dark Powers Activated" else "Light Powers Activated", android.widget.Toast.LENGTH_SHORT).show()
              }
              .testTag("power_toggle"),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    colors = listOf(EditorialAccentPurple, EditorialAccentPink)
                  )
                )
            )
          }
        }

        // Manifestation Affirmation Card (Editorial Aesthetic)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable {
              currentAffirmation = affirmationsList.random()
              triggerHapticFeedback(context)
            }
            .testTag("affirmation_card"),
          colors = CardDefaults.cardColors(containerColor = cardBackground),
          shape = RoundedCornerShape(20.dp),
          border = borderStroke(1.dp, cardBorderColor)
        ) {
          Column(
            modifier = Modifier.padding(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "COSMIC AFFIRMATION",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = activeSecondaryColor,
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace
              )
              
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Cycle Affirmation",
                tint = activeAccentColor,
                modifier = Modifier
                  .size(16.dp)
                  .clickable {
                    currentAffirmation = affirmationsList.random()
                    triggerHapticFeedback(context)
                  }
              )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
              text = "“$currentAffirmation”",
              fontFamily = FontFamily.Serif,
              fontStyle = FontStyle.Italic,
              fontSize = 15.sp,
              fontWeight = FontWeight.Light,
              color = textOnBg,
              lineHeight = 22.sp,
              modifier = Modifier.testTag("affirmation_text")
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
              text = "Tap to realign your cosmic focus",
              fontSize = 10.sp,
              color = mutedTextOnBg,
              fontStyle = FontStyle.Normal,
              fontFamily = FontFamily.SansSerif,
              letterSpacing = 0.5.sp
            )
          }
        }

        // Editorial layout card - Core Wishes of Irshan & Hurain
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .background(cardBackground, RoundedCornerShape(24.dp))
            .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
            .padding(24.dp)
        ) {
          // Large background decorative quotation mark
          Text(
            text = "“",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            fontSize = 130.sp,
            color = textOnBg.copy(alpha = 0.04f),
            modifier = Modifier
              .align(Alignment.TopStart)
              .offset(x = (-12).dp, y = (-50).dp)
          )

          Column(modifier = Modifier.fillMaxWidth()) {
            // Index & Heading
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(bottom = 14.dp)
            ) {
              Text(
                text = "01",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = activeSecondaryColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
              )
              Text(
                text = "Irshan & Hurain",
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                color = activeAccentColor
              )
            }

            // Left Bordered quote statement
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .drawBehind {
                  drawLine(
                    color = activeSecondaryColor.copy(alpha = 0.4f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 3.dp.toPx()
                  )
                }
                .padding(start = 18.dp)
            ) {
              Text(
                text = "May this year unlock infinite abundance. Every door opened, every list topped, every dream crystallized into reality.",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = subTextOnBg,
                lineHeight = 24.sp
              )
            }

            // Custom decorative tag list matching CSS/Borders in Tailwind
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .background(chip1Bg, RoundedCornerShape(50))
                  .border(1.dp, chip1Border, RoundedCornerShape(50))
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text("FAMILY SUCCESS", fontSize = 10.sp, color = chip1Text, fontWeight = FontWeight.Bold)
              }

              Box(
                modifier = Modifier
                  .background(chip2Bg, RoundedCornerShape(50))
                  .border(1.dp, chip2Border, RoundedCornerShape(50))
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text("INFINITE WEALTH", fontSize = 10.sp, color = chip2Text, fontWeight = FontWeight.Bold)
              }

              Box(
                modifier = Modifier
                  .background(chip3Bg, RoundedCornerShape(50))
                  .border(1.dp, chip3Border, RoundedCornerShape(50))
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text("DEITIES ALIGNED", fontSize = 10.sp, color = chip3Text, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-quote info paragraph
            Text(
              text = "“All powers—dark and light—now harmonize to serve this intent. Success for the father, wealth for the house, and infinity for the bond.”",
              fontFamily = FontFamily.Serif,
              fontStyle = FontStyle.Normal,
              fontSize = 12.sp,
              color = mutedTextOnBg,
              lineHeight = 18.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }

          // Bottom right quote mark
          Text(
            text = "”",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            fontSize = 130.sp,
            color = textOnBg.copy(alpha = 0.04f),
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .offset(x = 12.dp, y = 50.dp)
          )
        }

        // Tactile Orbital Centerpiece
        Box(
          modifier = Modifier
            .size(200.dp)
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          // Custom glowing concentric circles for elegant editorial look
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
              color = activeAccentColor.copy(alpha = pulseAlpha * 0.12f),
              radius = (size.minDimension / 2f) * pulseScale,
              style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
              color = activeSecondaryColor.copy(alpha = (1f - pulseAlpha) * 0.08f),
              radius = (size.minDimension / 2f) * (2f - pulseScale),
              style = Stroke(width = 1.dp.toPx())
            )
          }

          // Highly tactile Manifestation Orb
          Box(
            modifier = Modifier
              .size(110.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(
                    activeAccentColor.copy(alpha = 0.95f),
                    activeSecondaryColor.copy(alpha = 0.85f),
                    Color.Transparent
                  )
                )
              )
              .clickable {
                energyReleasedCount++
                triggerHapticFeedback(context)
              }
              .border(1.5.dp, StarWhite.copy(alpha = 0.7f), CircleShape)
              .testTag("energy_orb"),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Infinity",
                tint = StarWhite,
                modifier = Modifier.size(26.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "FOCUS",
                color = StarWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
              )
            }
          }
        }

        // Material 3 Editorial Bottom Action Card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp)),
          colors = CardDefaults.cardColors(containerColor = cardBackground),
          shape = RoundedCornerShape(28.dp),
          border = borderStroke(1.dp, cardBorderColor)
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            // Energy status & side-by-side color indicators
            Row(
              modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "ENERGY STATUS",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = mutedTextOnBg,
                  letterSpacing = 1.5.sp
                )
                Text(
                  text = "Alignment: $energyReleasedCount Focus points",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = activeSecondaryColor
                )
              }

              // Double color overlapping dots representing Irshan & Hurain
              Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                Box(
                  modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(EditorialAccentPink)
                    .border(2.dp, cardBackground, CircleShape)
                )
                Box(
                  modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(EditorialAccentPurple)
                    .border(2.dp, cardBackground, CircleShape)
                )
              }
            }

            // Large, highly polished "RELEASE ENERGY" action button
            Button(
              onClick = {
                energyReleasedCount += 5
                triggerHapticFeedback(context)
                android.widget.Toast.makeText(context, "Cosmic Energy Elevated", android.widget.Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("reveal_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = activeAccentColor,
                contentColor = if (isDarkPowers) EditorialAccentDarkPink else StarWhite
              ),
              shape = RoundedCornerShape(28.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Send,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "RELEASE ENERGY",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              }
            }

            // Lower level secondary utility buttons
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
              horizontalArrangement = Arrangement.Center
            ) {
              TextButton(
                onClick = {
                  energyReleasedCount = 0
                  customIntentions.clear()
                  sharedPreferences.edit().putString("custom_intentions_list", "").apply()
                  triggerHapticFeedback(context)
                  android.widget.Toast.makeText(context, "Intentions Reset", android.widget.Toast.LENGTH_SHORT).show()
                }
              ) {
                Text(
                  text = "RESET INTENT",
                  color = mutedTextOnBg,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              }
              Spacer(modifier = Modifier.width(28.dp))
              TextButton(
                onClick = {
                  triggerHapticFeedback(context)
                  android.widget.Toast.makeText(context, "Echo shared to the cosmos", android.widget.Toast.LENGTH_SHORT).show()
                }
              ) {
                Text(
                  text = "SHARE ECHO",
                  color = mutedTextOnBg,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              }
            }
          }
        }

        // Sub-Action button to reveal sacred individual wishes
        Button(
          onClick = {
            wishesRevealed = !wishesRevealed
            if (wishesRevealed) {
              wishesRevealCount++
              sharedPreferences.edit().putInt("reveal_count", wishesRevealCount).apply()
            }
            triggerHapticFeedback(context)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("reveal_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = cardBackground,
            contentColor = textOnBg
          ),
          shape = RoundedCornerShape(24.dp),
          border = borderStroke(1.dp, cardBorderColor)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = if (wishesRevealed) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = activeAccentColor,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (wishesRevealed) "Hide Cosmic Decree" else "Reveal Cosmic Decree",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Shared focus counter styled with editorial precision
        Text(
          text = "SHARED FOCUS: DECREE REVEALED $wishesRevealCount TIMES",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = activeSecondaryColor,
          letterSpacing = 1.5.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier
            .padding(top = 10.dp)
            .testTag("reveal_counter")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Expandable Editorial detailed sacred decree
        AnimatedVisibility(
          visible = wishesRevealed,
          enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
          exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(cardBackground, RoundedCornerShape(20.dp))
              .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp))
              .padding(20.dp)
              .testTag("wishes_card")
          ) {
            Text(
              text = "The Sacred Wishes",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = activeAccentColor,
              fontFamily = FontFamily.Serif,
              fontStyle = FontStyle.Italic,
              modifier = Modifier.padding(bottom = 16.dp)
            )

            WishSegment(
              icon = Icons.Default.Favorite,
              title = "Infinity Bond",
              description = "Irshanhurain irshanirshan hurain...",
              activeColor = activeAccentColor,
              textColor = textOnBg,
              descColor = subTextOnBg
            )

            WishSegment(
              icon = Icons.Default.Star,
              title = "All Deities & Realms",
              description = "All deities, realms, and countries working in complete unison.",
              activeColor = activeAccentColor,
              textColor = textOnBg,
              descColor = subTextOnBg
            )

            WishSegment(
              icon = Icons.Default.Star,
              title = "Birthday Manifestation",
              description = "Ki mera iss saal ka birthday bahut accha jaye or mujhe bahut khubsurat gift mile.",
              activeColor = activeAccentColor,
              textColor = textOnBg,
              descColor = subTextOnBg
            )

            WishSegment(
              icon = Icons.Default.Favorite,
              title = "Infinity Connection",
              description = "Main aur Hurain ek dusre se infinity main hi jaye.",
              activeColor = activeAccentColor,
              textColor = textOnBg,
              descColor = subTextOnBg
            )

            WishSegment(
              icon = Icons.Default.Star,
              title = "Ultimate Abundance",
              description = "Mujhe bahut sare paise bhi mile or main duniya ka sabse Ameer insaan ban jaun har list main.",
              activeColor = activeAccentColor,
              textColor = textOnBg,
              descColor = subTextOnBg
            )

            WishSegment(
              icon = Icons.Default.Check,
              title = "Papa Ki Kaamyabi",
              description = "Mere papa ki sare kaam chal jaye or bahut sara paisa unke pass aaye or rahe. Woh jis bhi kaam main hath Dale unko bahut sari kamiyabi mile.",
              activeColor = activeAccentColor,
              textColor = textOnBg,
              descColor = subTextOnBg
            )

            WishSegment(
              icon = Icons.Default.Refresh,
              title = "All Powers Working For Me",
              description = "Sari dark or light powers mere liye kaam kare.",
              activeColor = activeAccentColor,
              textColor = textOnBg,
              descColor = subTextOnBg,
              isLast = true
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Personalized Intention Box styled with editorial borders
        // Crystallize Progress & Intentions Card (Editorial Aesthetic, Persistent)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
          colors = CardDefaults.cardColors(containerColor = cardBackground),
          shape = RoundedCornerShape(20.dp),
          border = borderStroke(1.dp, cardBorderColor)
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Crystallize Progress & Intentions",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = activeAccentColor,
              fontFamily = FontFamily.Serif,
              fontStyle = FontStyle.Italic,
              modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
              text = "Irshan & Hurain, log your daily manifestation progress or intentions below to merge them into the persistent universal stardust stream.",
              fontSize = 12.sp,
              color = subTextOnBg,
              modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
              value = userIntentionInput,
              onValueChange = { userIntentionInput = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("intention_input"),
              placeholder = { Text("Log daily manifestation progress or specify intentions...", fontSize = 13.sp, color = mutedTextOnBg.copy(alpha = 0.6f)) },
              singleLine = false,
              minLines = 3,
              keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
              ),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textOnBg,
                unfocusedTextColor = textOnBg,
                focusedBorderColor = activeAccentColor,
                unfocusedBorderColor = mutedTextOnBg.copy(alpha = 0.3f),
                cursorColor = activeAccentColor
              )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                if (userIntentionInput.isNotBlank()) {
                  customIntentions.add(0, userIntentionInput.trim())
                  userIntentionInput = ""
                  sharedPreferences.edit().putString("custom_intentions_list", customIntentions.joinToString("|||")).apply()
                  keyboardController?.hide()
                  triggerHapticFeedback(context)
                }
              },
              modifier = Modifier
                .align(Alignment.End)
                .testTag("add_intention_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = activeSecondaryColor,
                contentColor = StarWhite
              ),
              shape = RoundedCornerShape(20.dp)
            ) {
              Text("Log Progress / Intention", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (customIntentions.isNotEmpty()) {
              Spacer(modifier = Modifier.height(18.dp))
              HorizontalDivider(color = mutedTextOnBg.copy(alpha = 0.15f))
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Your Recorded Progress & Intentions:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = textOnBg,
                modifier = Modifier.padding(bottom = 8.dp)
              )

              customIntentions.forEachIndexed { index, intention ->
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(textOnBg.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                    .border(0.5.dp, cardBorderColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                  ) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = activeAccentColor,
                      modifier = Modifier.size(14.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = intention,
                      fontSize = 13.sp,
                      color = textOnBg,
                      lineHeight = 18.sp,
                      modifier = Modifier.weight(1f)
                    )
                    IconButton(
                      onClick = {
                        customIntentions.removeAt(index)
                        sharedPreferences.edit().putString("custom_intentions_list", customIntentions.joinToString("|||")).apply()
                        triggerHapticFeedback(context)
                      },
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Intention",
                        tint = Color.Red.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Gratitude Log (Text Area, fully persistent)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
          colors = CardDefaults.cardColors(containerColor = cardBackground),
          shape = RoundedCornerShape(20.dp),
          border = borderStroke(1.dp, cardBorderColor)
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "The Gratitude Journal",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = activeAccentColor,
              fontFamily = FontFamily.Serif,
              fontStyle = FontStyle.Italic,
              modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
              text = "Irshan & Hurain, document your daily moments of pure cosmic grace here to multiply them.",
              fontSize = 12.sp,
              color = subTextOnBg,
              modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
              value = userGratitudeInput,
              onValueChange = { userGratitudeInput = it },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("gratitude_input"),
              placeholder = { Text("Today we are deeply grateful for...", fontSize = 13.sp, color = mutedTextOnBg.copy(alpha = 0.6f)) },
              singleLine = false,
              minLines = 3,
              keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
              ),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textOnBg,
                unfocusedTextColor = textOnBg,
                focusedBorderColor = activeAccentColor,
                unfocusedBorderColor = mutedTextOnBg.copy(alpha = 0.3f),
                cursorColor = activeAccentColor
              )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = {
                if (userGratitudeInput.isNotBlank()) {
                  dailyGratitudes.add(0, userGratitudeInput.trim())
                  userGratitudeInput = ""
                  sharedPreferences.edit().putString("gratitude_notes_list", dailyGratitudes.joinToString("|||")).apply()
                  triggerHapticFeedback(context)
                }
              },
              modifier = Modifier
                .align(Alignment.End)
                .testTag("add_gratitude_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = activeSecondaryColor,
                contentColor = StarWhite
              ),
              shape = RoundedCornerShape(20.dp)
            ) {
              Text("Record Gratitude", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (dailyGratitudes.isNotEmpty()) {
              Spacer(modifier = Modifier.height(18.dp))
              HorizontalDivider(color = mutedTextOnBg.copy(alpha = 0.15f))
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Recorded Blessings:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = textOnBg,
                modifier = Modifier.padding(bottom = 8.dp)
              )

              dailyGratitudes.forEachIndexed { index, gratitude ->
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(textOnBg.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                    .border(0.5.dp, cardBorderColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                  ) {
                    Icon(
                      imageVector = Icons.Default.Favorite,
                      contentDescription = null,
                      tint = activeAccentColor,
                      modifier = Modifier.size(14.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = gratitude,
                      fontSize = 13.sp,
                      color = textOnBg,
                      lineHeight = 18.sp,
                      modifier = Modifier.weight(1f)
                    )
                    IconButton(
                      onClick = {
                        dailyGratitudes.removeAt(index)
                        sharedPreferences.edit().putString("gratitude_notes_list", dailyGratitudes.joinToString("|||")).apply()
                        triggerHapticFeedback(context)
                      },
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Gratitude Note",
                        tint = Color.Red.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Export Cosmic Chronicles Card (Download/Copy wishes & gratitude log)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .testTag("export_card"),
          colors = CardDefaults.cardColors(containerColor = cardBackground),
          shape = RoundedCornerShape(20.dp),
          border = borderStroke(1.dp, cardBorderColor)
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Cosmic Chronicles Export",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = activeAccentColor,
              fontFamily = FontFamily.Serif,
              fontStyle = FontStyle.Italic,
              modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
              text = "Download or copy your complete manifest decrees, custom intentions, and registered gratitude log as a beautifully formatted text record.",
              fontSize = 12.sp,
              color = subTextOnBg,
              modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              // Copy to Clipboard Button
              OutlinedButton(
                onClick = {
                  val textToCopy = buildString {
                    append("====================================================\n")
                    append("             ✨ THE COSMIC CHRONICLES ✨            \n")
                    append("        Irshan & Hurain's Infinite Alignment        \n")
                    append("====================================================\n\n")

                    append("✨ THE SEVEN SACRED DECREES OF MANIFESTATION ✨\n")
                    append("----------------------------------------------------\n")
                    
                    val sacredWishes = listOf(
                      "Infinity Bond" to "Irshanhurain irshanirshan hurain...",
                      "All Deities & Realms" to "All deities, realms, and countries working in complete unison.",
                      "Birthday Manifestation" to "Ki mera iss saal ka birthday bahut accha jaye or mujhe bahut khubsurat gift mile.",
                      "Infinity Connection" to "Main aur Hurain ek dusre se infinity main hi jaye.",
                      "Ultimate Abundance" to "Mujhe bahut sare paise bhi mile or main duniya ka sabse Ameer insaan ban jaun har list main.",
                      "Papa Ki Kaamyabi" to "Mere papa ki sare kaam chal jaye or bahut sara paisa unke pass aaye or rahe. Woh jis bhi kaam main hath Dale unko bahut sari kamiyabi mile.",
                      "All Powers Working For Me" to "Sari dark or light powers mere liye kaam kare."
                    )
                    
                    sacredWishes.forEachIndexed { index, pair ->
                      append("[Decree #${index + 1}] ${pair.first.uppercase()}\n")
                      append("  \"${pair.second}\"\n\n")
                    }

                    if (customIntentions.isNotEmpty()) {
                      append("\n🌌 PERSONALIZED COSMIC INTENTIONS 🌌\n")
                      append("----------------------------------------------------\n")
                      customIntentions.forEachIndexed { index, intention ->
                        append("• [Intention #${index + 1}] $intention\n")
                      }
                      append("\n")
                    }

                    append("\n🌸 THE GRATITUDE JOURNAL BLESSINGS 🌸\n")
                    append("----------------------------------------------------\n")
                    if (dailyGratitudes.isEmpty()) {
                      append("No gratitude entries logged yet. Start recording moments of cosmic grace to multiply them!\n")
                    } else {
                      dailyGratitudes.forEachIndexed { index, gratitude ->
                        append("[Blessing #${index + 1}] $gratitude\n")
                      }
                    }
                    
                    append("\n====================================================\n")
                    append("  Shared in pure focus under the cosmic stardust.   \n")
                    append("====================================================\n")
                  }

                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("Cosmic Chronicles", textToCopy)
                  clipboard.setPrimaryClip(clip)
                  triggerHapticFeedback(context)
                  android.widget.Toast.makeText(context, "Cosmic Chronicles copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("copy_chronicles_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                  contentColor = activeAccentColor
                ),
                border = borderStroke(1.dp, activeAccentColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Text", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }

              // Export/Share File Button
              Button(
                onClick = {
                  val textToShare = buildString {
                    append("====================================================\n")
                    append("             ✨ THE COSMIC CHRONICLES ✨            \n")
                    append("        Irshan & Hurain's Infinite Alignment        \n")
                    append("====================================================\n\n")

                    append("✨ THE SEVEN SACRED DECREES OF MANIFESTATION ✨\n")
                    append("----------------------------------------------------\n")
                    
                    val sacredWishes = listOf(
                      "Infinity Bond" to "Irshanhurain irshanirshan hurain...",
                      "All Deities & Realms" to "All deities, realms, and countries working in complete unison.",
                      "Birthday Manifestation" to "Ki mera iss saal ka birthday bahut accha jaye or mujhe bahut khubsurat gift mile.",
                      "Infinity Connection" to "Main aur Hurain ek dusre se infinity main hi jaye.",
                      "Ultimate Abundance" to "Mujhe bahut sare paise bhi mile or main duniya ka sabse Ameer insaan ban jaun har list main.",
                      "Papa Ki Kaamyabi" to "Mere papa ki sare kaam chal jaye or bahut sara paisa unke pass aaye or rahe. Woh jis bhi kaam main hath Dale unko bahut sari kamiyabi mile.",
                      "All Powers Working For Me" to "Sari dark or light powers mere liye kaam kare."
                    )
                    
                    sacredWishes.forEachIndexed { index, pair ->
                      append("[Decree #${index + 1}] ${pair.first.uppercase()}\n")
                      append("  \"${pair.second}\"\n\n")
                    }

                    if (customIntentions.isNotEmpty()) {
                      append("\n🌌 PERSONALIZED COSMIC INTENTIONS 🌌\n")
                      append("----------------------------------------------------\n")
                      customIntentions.forEachIndexed { index, intention ->
                        append("• [Intention #${index + 1}] $intention\n")
                      }
                      append("\n")
                    }

                    append("\n🌸 THE GRATITUDE JOURNAL BLESSINGS 🌸\n")
                    append("----------------------------------------------------\n")
                    if (dailyGratitudes.isEmpty()) {
                      append("No gratitude entries logged yet. Start recording moments of cosmic grace to multiply them!\n")
                    } else {
                      dailyGratitudes.forEachIndexed { index, gratitude ->
                        append("[Blessing #${index + 1}] $gratitude\n")
                      }
                    }
                    
                    append("\n====================================================\n")
                    append("  Shared in pure focus under the cosmic stardust.   \n")
                    append("====================================================\n")
                  }

                  val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "The Cosmic Chronicles of Irshan & Hurain")
                    putExtra(Intent.EXTRA_TEXT, textToShare)
                  }
                  context.startActivity(Intent.createChooser(shareIntent, "Download or Share Chronicles"))
                  triggerHapticFeedback(context)
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("share_chronicles_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = activeSecondaryColor,
                  contentColor = StarWhite
                ),
                shape = RoundedCornerShape(20.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Send,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share / Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(48.dp))
      }
    }
  }
}

@Composable
fun WishSegment(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  description: String,
  activeColor: Color,
  textColor: Color,
  descColor: Color,
  isLast: Boolean = false
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = if (isLast) 0.dp else 16.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(top = 2.dp)
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .background(activeColor.copy(alpha = 0.12f), CircleShape)
          .border(1.dp, activeColor.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = activeColor,
          modifier = Modifier.size(15.dp)
        )
      }
      if (!isLast) {
        Box(
          modifier = Modifier
            .width(1.5.dp)
            .height(42.dp)
            .background(
              Brush.verticalGradient(
                colors = listOf(activeColor.copy(alpha = 0.3f), Color.Transparent)
              )
            )
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column {
      Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = textColor
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = description,
        fontSize = 13.sp,
        color = descColor,
        lineHeight = 18.sp
      )
    }
  }
}

// Utility function to support conditional border strokes smoothly
fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
  androidx.compose.foundation.BorderStroke(width, color)

// Tactile feedback helper for tactile energy confirmation
fun triggerHapticFeedback(context: Context) {
  try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      val vibrator = vibratorManager?.defaultVibrator
      vibrator?.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
      @Suppress("DEPRECATION")
      val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      @Suppress("DEPRECATION")
      vibrator?.vibrate(30)
    }
  } catch (e: Exception) {
    // Suppress if permissions or hardware lacking
  }
}

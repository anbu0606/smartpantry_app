package com.lab03g10.smartpantry.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lab03g10.smartpantry.ui.theme.SmartPantryTheme
import kotlinx.coroutines.launch

// Private data model

private data class OnboardingPage(
    val emoji:    String,
    val bgColor:  Color,
    val title:    String,
    val subtitle: String,
)

private val onboardingPages = listOf(
    OnboardingPage(
        emoji    = "🌡️",
        bgColor  = Color(0xFFE8F5E9),
        title    = "Monitor Storage Environments",
        subtitle = "Real-time temperature and humidity tracking for every storage location — fridge, pantry, cupboard, and more.",
    ),
    OnboardingPage(
        emoji    = "🥗",
        bgColor  = Color(0xFFFFFDE7),
        title    = "Track Food Inventory",
        subtitle = "Know exactly what you have and when it expires. Get alerts before food goes bad.",
    ),
    OnboardingPage(
        emoji    = "🛒",
        bgColor  = Color(0xFFE3F2FD),
        title    = "Smart Shopping Lists",
        subtitle = "Automatically generate shopping lists from expired and low-stock items. Never forget what you need.",
    ),
    OnboardingPage(
        emoji    = "📊",
        bgColor  = Color(0xFFF3E5F5),
        title    = "Reduce Food Waste",
        subtitle = "Track consumption patterns and see your waste reduction over time. Make a positive impact.",
    ),
)

// Public composable

/**
 * Onboarding screen for Smart Pantry app
 *
 * Shows 4 pages that user can swipe
 * Each page has an emoji, title, and short description
 *
 * Bottom section includes:
 * - Page indicator (dots)
 * - Next or Get Started button
 *
 * Skip button is shown at the top (not on last page)
 *
 * @param onFinish -> moves user to main app when done or skipped
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {

    val pagerState     = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage     = pagerState.currentPage == onboardingPages.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {

        // Layer 1 — Swipeable pages

        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->

            val page = onboardingPages[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 140.dp),
                verticalArrangement   = Arrangement.Center,
                horizontalAlignment   = Alignment.CenterHorizontally,
            ) {

                // Illustration box
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(page.bgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text     = page.emoji,
                        fontSize = 72.sp,
                    )
                }

                Spacer(Modifier.height(40.dp))

                Text(
                    text      = page.title,
                    style     = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color     = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text       = page.subtitle,
                    style      = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 26.sp,
                    ),
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign  = TextAlign.Center,
                )
            }
        }

        // Layer 2 — Skip button (top-end, only when not on last page)

        if (!isLastPage) {
            TextButton(
                onClick  = onFinish,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp),
            ) {
                Text(
                    text  = "Skip",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Layer 3 — Bottom bar (page dots + CTA button)

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {

            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                            ),
                    )
                }
            }

            // CTA button
            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape          = RoundedCornerShape(50),
                colors         = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text(
                    text  = if (isLastPage) "Get Started" else "Next",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

// Previews

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF9F9F9)
@Composable
private fun PreviewOnboardingLight() {
    SmartPantryTheme {
        OnboardingScreen(onFinish = {})
    }
}

@Preview(
    name            = "Dark",
    showBackground  = true,
    backgroundColor = 0xFF191C18,
    uiMode          = UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewOnboardingDark() {
    SmartPantryTheme(darkTheme = true) {
        OnboardingScreen(onFinish = {})
    }
}

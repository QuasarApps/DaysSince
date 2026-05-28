package com.quasarapps.dayssince.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quasarapps.dayssince.data.Milestone
import com.quasarapps.dayssince.ui.components.rememberElapsedDhm
import com.quasarapps.dayssince.ui.theme.LegibilityScrim
import com.quasarapps.dayssince.ui.theme.accentBrush
import com.quasarapps.dayssince.util.EnglishDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    milestones: List<Milestone>,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Pad away from status bar (top), nav bar (bottom), and landscape side nav bar.
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            // Small top bar keeps the title close to the top — much better in landscape than
            // the large variant, which leaves a big empty band above the cards.
            TopAppBar(
                title = { Text("Days Since", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            if (milestones.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAdd,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("New") },
                    // In landscape, the 3-button nav bar sits on the right edge; shift the
                    // FAB inside it so it doesn't slide under the system buttons.
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.End + WindowInsetsSides.Bottom,
                        ),
                    ),
                )
            }
        },
    ) { padding ->
        if (milestones.isEmpty()) {
            EmptyState(
                onAdd = onAdd,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalItemSpacing = 14.dp,
            ) {
                items(milestones, key = { it.id }) { milestone ->
                    MilestoneCard(milestone = milestone, onClick = { onOpen(milestone.id) })
                }
            }
        }
    }
}

@Composable
private fun MilestoneCard(milestone: Milestone, onClick: () -> Unit) {
    val dhm = rememberElapsedDhm(milestone.date, milestone.time)
    val brush = accentBrush(milestone.accent)
    val timeText = remember(milestone.time) {
        milestone.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    }

    // No fixed height: each card grows to fit its title + wrapped date so columns balance
    // naturally based on actual content (no more left/right size mismatch).
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(brush)
            .clickable(onClick = onClick),
    ) {
        Box(Modifier.matchParentSize().background(LegibilityScrim))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = dhm.days.toString(),
                style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
            )
            Text(
                text = "DAYS",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            // Allow the date to wrap onto multiple lines instead of ellipsizing.
            Text(
                text = "since ${EnglishDateFormat.formatOrdinalDate(milestone.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
            )
            Text(
                text = "at $timeText",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun EmptyState(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "No milestones yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Track the days since something that matters to you.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        Button(onClick = onAdd) {
            Text("Add your first milestone")
        }
    }
}

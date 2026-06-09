package com.quasarapps.pulsar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quasarapps.pulsar.ElapsedTime
import com.quasarapps.pulsar.R
import com.quasarapps.pulsar.data.Milestone
import com.quasarapps.pulsar.data.SortOrder
import com.quasarapps.pulsar.ui.components.Starburst
import com.quasarapps.pulsar.ui.components.rememberElapsedDhm
import com.quasarapps.pulsar.ui.theme.NewBeginningBrush
import com.quasarapps.pulsar.ui.theme.QuasarBrush
import com.quasarapps.pulsar.ui.theme.accentBrush
import com.quasarapps.pulsar.ui.theme.accentOrDefault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    milestones: List<Milestone>,
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    onOpenSettings: () -> Unit = {},
    sortOrder: SortOrder = SortOrder.RECENTLY_ADDED,
    onSetSortOrder: (SortOrder) -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Pad away from status bar (top), nav bar (bottom), and the landscape side nav bar.
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    // The Pulsar wordmark, in the Chakra Petch display face (via headlineSmall).
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    // Sorting only matters with a list to sort, so the control hides on the empty state.
                    if (milestones.isNotEmpty()) {
                        SortAction(current = sortOrder, onSelect = onSetSortOrder)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = { MarkFab(onAdd) },
    ) { padding ->
        if (milestones.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            // Two-column grid keyed per milestone id, so a re-sort animates each card to its new
            // slot (animateItem) instead of snapping. Cards size independently (min 150dp); we trade
            // the old per-row height equalization for stable keys + animated reordering.
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(items = milestones, key = { it.id }) { milestone ->
                    MilestoneCard(
                        milestone = milestone,
                        onClick = { onOpen(milestone.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SortAction(current: SortOrder, onSelect: (SortOrder) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.home_sort_content_description),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            SortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = { Text(sortOrderLabel(order)) },
                    // Re-selecting the active order is a no-op: skip it so we don't write the same
                    // value back to DataStore.
                    onClick = { open = false; if (order != current) onSelect(order) },
                    // A trailing check marks the active order without shifting the other labels.
                    trailingIcon = {
                        if (order == current) Icon(Icons.Filled.Check, contentDescription = null)
                    },
                )
            }
        }
    }
}

@Composable
private fun sortOrderLabel(order: SortOrder): String = stringResource(
    when (order) {
        SortOrder.RECENTLY_ADDED -> R.string.home_sort_recently_added
        SortOrder.MOST_DAYS -> R.string.home_sort_most_days
        SortOrder.ALPHABETICAL -> R.string.home_sort_alphabetical
    },
)

@Composable
private fun MilestoneCard(
    milestone: Milestone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dhm = rememberElapsedDhm(milestone.date, milestone.time)
    val isNew = ElapsedTime.isNewBeginning(dhm.days, milestone.date, milestone.time)
    val accent = accentOrDefault(milestone.accent)
    val onColor = if (isNew) Color.White else accent.onAccent
    val brush = if (isNew) NewBeginningBrush else accentBrush(milestone.accent)

    // One merged TalkBack node per card — "<title>, <status>" + "Button" — instead of the decorative
    // number / kicker / title being traversed as three separate, unlabeled nodes. For a 0-day card
    // the status mirrors the visible "new beginning" kicker rather than reading "0 days", so a screen
    // reader hears the same celebratory state a sighted user sees.
    val statusFragment = if (isNew) {
        stringResource(R.string.card_new_beginning_label)
    } else {
        pluralStringResource(R.plurals.widget_a11y_days, dhm.days.toInt(), dhm.days)
    }
    val cardDescription = stringResource(R.string.card_a11y_content_description, milestone.title, statusFragment)

    Box(
        modifier = modifier
            .heightIn(min = 150.dp)
            .clip(MaterialTheme.shapes.large)
            .background(brush)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = cardDescription },
    ) {
        // The 0-day "new beginning" tile earns a faint starburst in the corner — a small reward.
        if (isNew) {
            Starburst(
                color = onColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(56.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = dhm.days.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold,
                    color = onColor,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(
                        if (isNew) R.string.card_new_beginning_label else R.string.card_days_since_label,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 2.sp,
                    color = onColor.copy(alpha = 0.88f),
                )
            }
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.titleMedium,
                color = onColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MarkFab(onAdd: () -> Unit) {
    val label = stringResource(R.string.home_fab_mark)
    val fabCd = stringResource(R.string.home_fab_content_description)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        modifier = Modifier
            // In landscape the system nav bar sits on the right edge; add the End inset so the FAB
            // clears the software buttons (Scaffold only adds the BOTTOM inset here).
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.End))
            .shadow(16.dp, RoundedCornerShape(50), spotColor = Color(0xFFD131BC))
            .clip(RoundedCornerShape(50))
            .background(QuasarBrush)
            .clickable(role = Role.Button, onClick = onAdd)
            // Merge into one node so TalkBack announces a single "Add milestone" button rather than
            // the decorative star + "Mark" text separately.
            .semantics(mergeDescendants = true) { contentDescription = fabCd }
            .padding(horizontal = 22.dp, vertical = 16.dp),
    ) {
        Starburst(color = Color.White, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Starburst(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(84.dp),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

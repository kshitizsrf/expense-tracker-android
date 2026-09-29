package com.hisabkitab.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hisabkitab.core.designsystem.R as DesignR

/** Searchable list of languages (matches native or English names). */
@Composable
fun LanguageList(
    selectedTag: String,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    includeSystem: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var query by rememberSaveable { mutableStateOf("") }
    val languages = (if (includeSystem) listOf(AppLanguages.SYSTEM) else emptyList()) + AppLanguages.all
    val filtered = languages.filter {
        query.isBlank() || it.nativeName.contains(query, ignoreCase = true) || it.englishName.contains(query, ignoreCase = true)
    }
    Column(modifier) {
        SearchField(query, { query = it }, Modifier.padding(contentPadding).padding(bottom = 8.dp))
        LazyColumn(contentPadding = contentPadding, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filtered, key = { it.tag.ifEmpty { "system" } }) { language ->
                LanguageRow(language, selected = language.tag == selectedTag, onClick = { onSelect(language) })
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(scheme.surfaceContainerHighest)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = scheme.onSurfaceVariant)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) Text(stringResource(R.string.language_search), color = scheme.onSurfaceVariant)
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
                cursorBrush = SolidColor(scheme.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LanguageRow(language: AppLanguage, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val background by animateColorAsState(if (selected) scheme.primary.copy(alpha = 0.14f) else Color.Transparent, label = "langBg")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(scheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Text(language.flag, fontSize = 24.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            val isSystem = language.tag.isEmpty()
            Text(
                if (isSystem) stringResource(R.string.language_system) else language.nativeName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                if (isSystem) stringResource(R.string.language_system_summary) else language.englishName,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        if (selected) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = scheme.primary)
    }
}

@Composable
fun LanguageDialog(selectedTag: String, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language)) },
        text = { LanguageList(selectedTag = selectedTag, onSelect = onSelect, modifier = Modifier.heightIn(max = 480.dp)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(DesignR.string.action_close)) } },
    )
}

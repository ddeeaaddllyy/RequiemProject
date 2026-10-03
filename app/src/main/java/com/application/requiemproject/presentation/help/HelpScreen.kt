package com.application.requiemproject.presentation.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.application.requiemproject.domain.model.HelpArticle
import com.application.requiemproject.presentation.components.*
import com.application.requiemproject.presentation.theme.*

@Composable
fun HelpScreen(query: String, category: String, expanded: Int?, articles: List<HelpArticle>, onQuery: (String) -> Unit, onCategory: (String) -> Unit, onToggle: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeading("02 / FIELD NOTES", "ДОСЬЕ.", "Всё, что нужно знать перед началом.") }
        item {
            OutlinedTextField(query, onQuery, Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Поиск по досье") }, leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQuery("") }) { Icon(Icons.Default.Close, "Очистить поиск") } })
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Все", "Начало", "Перевод", "Приватность").forEach {
                    FilterChip(selected = category == it, onClick = { onCategory(it) }, label = { Text(it) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Red, selectedLabelColor = Paper))
                }
            }
        }
        item { Eyebrow("НАЙДЕНО: ${articles.size.toString().padStart(2, '0')}") }
        if (articles.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    RequiemInsignia(Modifier.size(80.dp), Muted)
                    Text("Следов не найдено", Modifier.padding(top = 20.dp), style = MaterialTheme.typography.titleLarge)
                    Text("Попробуйте другой запрос или раздел.", color = Muted)
                    TextButton(onClick = { onQuery(""); onCategory("Все") }) { Text("Сбросить фильтры") }
                }
            }
        }
        items(articles, key = { it.id }) { article ->
            val open = expanded == article.id
            Column(Modifier.fillMaxWidth().background(if (open) Paper else Panel).animateContentSize()) {
                Row(Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = if (open) "Свернуть ответ" else "Открыть ответ", onClick = { onToggle(article.id) }).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(article.id.toString().padStart(2, '0'), color = Red, fontWeight = FontWeight.Black)
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                        Eyebrow(article.category.uppercase(), if (open) Ink.copy(alpha = .6f) else Muted)
                        Text(article.title, color = if (open) Ink else Paper, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                    }
                    Icon(if (open) Icons.Default.Remove else Icons.Default.Add, null, tint = Red)
                }
                AnimatedVisibility(open) {
                    Text(article.answer, Modifier.padding(start = 18.dp, end = 18.dp, bottom = 22.dp), color = Ink, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)); Eyebrow("KNOWLEDGE IS YOUR SUPERPOWER.", Gold) }
    }
}

package com.kevpierce.catholicfastingapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import com.kevpierce.catholicfasting.core.model.FastingHistoryArticle
import com.kevpierce.catholicfasting.core.rules.FastingHistoryCatalog
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues
import com.kevpierce.catholicfastingapp.R

@Composable
internal fun HistoryOfFastingSection(
    supportState: AppSupportState,
    modifier: Modifier = Modifier,
) {
    val articles = FastingHistoryCatalog.articles(supportState.contentLocale)
    var selectedArticleId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedArticle = articles.firstOrNull { it.id == selectedArticleId }

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(CatholicFastingThemeValues.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.small),
    ) {
        if (selectedArticle == null) {
            HistoryOverviewCard()
            HistoryTimelineCard(
                articles = articles,
                onArticleSelected = { selectedArticleId = it.id },
            )
        } else {
            HistoryArticleDetail(
                article = selectedArticle,
                onBack = { selectedArticleId = null },
            )
        }
    }
}

@Composable
private fun HistoryOverviewCard() {
    SectionCard(title = stringResource(R.string.history_overview_title), heroTitle = true) {
        Text(
            stringResource(R.string.history_overview_eyebrow),
            style = CatholicFastingThemeValues.typography.utility,
        )
        Text(
            stringResource(R.string.history_overview_detail),
            style = CatholicFastingThemeValues.typography.body,
        )
    }
}

@Composable
private fun HistoryTimelineCard(
    articles: List<FastingHistoryArticle>,
    onArticleSelected: (FastingHistoryArticle) -> Unit,
) {
    SectionCard(title = stringResource(R.string.history_timeline_section)) {
        articles.forEach { article ->
            androidx.compose.material3.OutlinedButton(
                onClick = { onArticleSelected(article) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CatholicFastingThemeValues.spacing.xxSmall),
                ) {
                    Text(article.dateRange, style = CatholicFastingThemeValues.typography.utility)
                    Text(article.title, style = CatholicFastingThemeValues.typography.sectionTitle)
                    Text(article.summary, style = CatholicFastingThemeValues.typography.supporting)
                }
            }
        }
        Text(
            stringResource(R.string.history_timeline_footer),
            style = CatholicFastingThemeValues.typography.utility,
        )
    }
}

@Composable
private fun HistoryArticleDetail(
    article: FastingHistoryArticle,
    onBack: () -> Unit,
) {
    OutlinedActionButton(
        label = stringResource(R.string.history_back_to_timeline),
        onClick = onBack,
    )
    SectionCard(title = article.title, heroTitle = true) {
        Text(article.dateRange, style = CatholicFastingThemeValues.typography.utility)
        Text(article.summary, style = CatholicFastingThemeValues.typography.body)
    }
    SectionCard(title = stringResource(R.string.history_article_body)) {
        article.body.split("\n\n").forEach { paragraph ->
            Text(paragraph, style = CatholicFastingThemeValues.typography.body)
        }
    }
    SectionCard(title = stringResource(R.string.history_article_sources)) {
        article.sourceNotes.forEach { sourceNote ->
            Text(sourceNote.title, style = CatholicFastingThemeValues.typography.sectionTitle)
            Text(sourceNote.detail, style = CatholicFastingThemeValues.typography.supporting)
        }
    }
}

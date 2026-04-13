@file:Suppress("MatchingDeclarationName", "TooManyFunctions")

package com.kevpierce.catholicfasting.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kevpierce.catholicfasting.core.model.CatholicFastingQuote
import com.kevpierce.catholicfasting.core.model.Observance
import com.kevpierce.catholicfasting.core.model.SacredImageryItem
import com.kevpierce.catholicfasting.core.model.SeasonalContentPack
import com.kevpierce.catholicfasting.core.rules.PremiumSnapshot

data class TodayUiState(
    val todayObservance: Observance?,
    val completionSummary: String,
    val premiumSnapshot: PremiumSnapshot,
    val seasonalContentPack: SeasonalContentPack,
    val dailyFormationLine: String,
    val dailyQuote: CatholicFastingQuote,
    val devotionalGallery: List<SacredImageryItem>,
    val setupProgressSummary: String,
    val yearPlanSummary: String,
    val weeklyRecap: String,
    val streakMessage: String,
    val noticeSummary: String,
)

@Composable
fun todayScreen(
    uiState: TodayUiState,
    modifier: Modifier = Modifier,
) {
    val todayDetail =
        uiState.todayObservance?.detail ?: stringResource(R.string.today_default_detail)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        todayContent(
            uiState = uiState,
            todayDetail = todayDetail,
        )
    }
}

@Composable
private fun ColumnScope.todayContent(
    uiState: TodayUiState,
    todayDetail: String,
) {
    Text(
        stringResource(R.string.today_title),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.semantics { heading() },
    )
    observanceSummaryCard(uiState, todayDetail)
    seasonalFormationCard(uiState)
    yearPlanCard(uiState)
    personalInsightsCard(uiState)
    seasonPlanCard(uiState)
    recoveryCoachCard(uiState)
    devotionalGalleryCard(uiState)
    noticeCard(uiState)
}

@Composable
private fun todayCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            content()
        }
    }
}

@Composable
private fun observanceSummaryCard(
    uiState: TodayUiState,
    todayDetail: String,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = uiState.todayObservance?.title ?: stringResource(R.string.today_no_observance),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(todayDetail)
            Text(uiState.completionSummary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun seasonalFormationCard(uiState: TodayUiState) {
    todayCard(title = uiState.seasonalContentPack.campaignTitle) {
        Text(
            uiState.seasonalContentPack.campaignSubtitle,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(uiState.dailyFormationLine)
        Text(
            stringResource(R.string.today_quote_value, uiState.dailyQuote.text),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            stringResource(
                R.string.today_quote_author_value,
                uiState.dailyQuote.author,
                uiState.dailyQuote.tradition,
            ),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun yearPlanCard(uiState: TodayUiState) {
    todayCard(title = stringResource(R.string.today_year_plan_title)) {
        Text(uiState.yearPlanSummary)
        Text(uiState.weeklyRecap)
        Text(uiState.setupProgressSummary)
    }
}

@Composable
private fun personalInsightsCard(uiState: TodayUiState) {
    todayCard(title = stringResource(R.string.today_personal_insights_title)) {
        Text(uiState.streakMessage)
        Text(uiState.premiumSnapshot.motivationLine)
        Text(uiState.premiumSnapshot.reminderRecommendation.summaryLine)
    }
}

@Composable
private fun seasonPlanCard(uiState: TodayUiState) {
    todayCard(title = uiState.premiumSnapshot.seasonPlan.titleLine) {
        Text(
            text =
                stringResource(
                    R.string.today_season_motivation_value,
                    uiState.premiumSnapshot.season.label,
                    uiState.premiumSnapshot.motivationLine,
                ),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(uiState.premiumSnapshot.seasonPlan.focusLine)
        uiState.premiumSnapshot.seasonPlan.practices.forEach { practice ->
            Text(stringResource(R.string.today_bullet_value, practice))
        }
    }
}

@Composable
private fun recoveryCoachCard(uiState: TodayUiState) {
    todayCard(title = uiState.premiumSnapshot.recoveryCoachPlan.title) {
        Text(uiState.premiumSnapshot.recoveryCoachPlan.summary)
        uiState.premiumSnapshot.recoveryCoachPlan.steps.take(3).forEach { step ->
            Text(stringResource(R.string.today_bullet_value, step))
        }
        Text(
            text = uiState.premiumSnapshot.reflection.title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(uiState.premiumSnapshot.reflection.body)
        Text(
            stringResource(
                R.string.today_action_value,
                uiState.premiumSnapshot.reflection.action,
            ),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun devotionalGalleryCard(uiState: TodayUiState) {
    todayCard(title = stringResource(R.string.today_devotional_gallery_title)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.devotionalGallery.take(6).forEach { item ->
                Card {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall)
                        Text(item.subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun noticeCard(uiState: TodayUiState) {
    todayCard(title = stringResource(R.string.today_important_notice_title)) {
        Text(uiState.noticeSummary)
        Text(
            stringResource(R.string.today_notice_body),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

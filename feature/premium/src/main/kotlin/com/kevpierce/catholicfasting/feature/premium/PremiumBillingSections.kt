package com.kevpierce.catholicfasting.feature.premium

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kevpierce.catholicfasting.core.billing.BillingOfferUi
import com.kevpierce.catholicfasting.core.billing.BillingState
import com.kevpierce.catholicfasting.core.ui.CatholicFastingScreenTitle
import com.kevpierce.catholicfasting.core.ui.CatholicFastingThemeValues

internal fun LazyListScope.billingHeaderItems(
    billingState: BillingState,
    onRefresh: () -> Unit,
    onManageSubscription: () -> Unit,
) {
    val subscriptionHealthMessage = billingState.subscriptionHealthMessage

    item {
        CatholicFastingScreenTitle(stringResource(R.string.premium_title))
    }
    item {
        Text(
            stringResource(R.string.premium_catalog_subtitle),
            style = CatholicFastingThemeValues.typography.supporting,
        )
    }
    item {
        Text(
            if (billingState.premiumUnlocked) {
                stringResource(R.string.premium_active)
            } else {
                stringResource(R.string.premium_inactive)
            },
            style = CatholicFastingThemeValues.typography.sectionTitle,
        )
    }
    if (subscriptionHealthMessage != null) {
        item {
            Text(
                subscriptionHealthMessage.localizedText(),
                style = CatholicFastingThemeValues.typography.supporting,
            )
        }
    }
    item {
        OutlinedButton(onClick = onRefresh) {
            Text(
                if (billingState.isLoading) {
                    stringResource(R.string.premium_refreshing)
                } else {
                    stringResource(R.string.premium_restore_refresh_purchases)
                },
            )
        }
    }
    if (billingState.canManageSubscription) {
        item {
            OutlinedButton(onClick = onManageSubscription) {
                Text(stringResource(R.string.premium_manage_subscription))
            }
        }
    }
    if (billingState.hasPendingPurchases) {
        item {
            Text(stringResource(R.string.premium_purchase_pending))
        }
    }
}

internal fun LazyListScope.premiumOfferItems(
    billingState: BillingState,
    onPurchase: (String) -> Unit,
) {
    offerItems(
        titleRes = R.string.premium_subscriptions_title,
        offers = billingState.premiumOffers,
        actionLabelRes = R.string.premium_subscribe,
        emptyStateRes = R.string.premium_no_subscription_offers,
        actionsDisabled = billingState.isLoading || billingState.isPurchasing,
        onPurchase = onPurchase,
    )
    if (billingState.tipOffers.isNotEmpty()) {
        offerItems(
            titleRes = R.string.premium_support_tips_title,
            offers = billingState.tipOffers,
            actionLabelRes = R.string.premium_support,
            emptyStateRes = R.string.premium_no_tip_offers,
            actionsDisabled = billingState.isLoading || billingState.isPurchasing,
            onPurchase = onPurchase,
        )
    }
}

internal fun LazyListScope.offerItems(
    @StringRes titleRes: Int,
    offers: List<BillingOfferUi>,
    @StringRes actionLabelRes: Int,
    @StringRes emptyStateRes: Int,
    actionsDisabled: Boolean,
    onPurchase: (String) -> Unit,
) {
    item {
        Text(stringResource(titleRes), style = CatholicFastingThemeValues.typography.sectionTitle)
    }
    if (offers.isEmpty()) {
        item {
            Text(stringResource(emptyStateRes), style = CatholicFastingThemeValues.typography.supporting)
        }
        return
    }
    items(offers, key = BillingOfferUi::productId) { offer ->
        OfferCard(
            offer = offer,
            actionLabel = stringResource(actionLabelRes),
            actionEnabled = !actionsDisabled,
            onAction = { onPurchase(offer.productId) },
        )
    }
}

@Composable
private fun OfferCard(
    offer: BillingOfferUi,
    actionLabel: String,
    actionEnabled: Boolean,
    onAction: () -> Unit,
) {
    WorkspaceCard(title = offer.displayTitle) {
        Text(offer.priceLabel, style = CatholicFastingThemeValues.typography.body)
        Text(offer.billingLabel, style = CatholicFastingThemeValues.typography.supporting)
        Button(onClick = onAction, enabled = actionEnabled) {
            Text(actionLabel)
        }
    }
}

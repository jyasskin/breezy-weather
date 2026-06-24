/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.ui.main.adapters.main.holder

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AnalogClock
import android.widget.TextClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.MutableLiveData
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.ui.theme.compose.BreezyWeatherTheme
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import java.util.Date
import java.util.TimeZone

class ClockViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    ComposeView(parent.context).apply {
        layoutParams =
            ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }) {
    private val timezone = MutableLiveData(TimeZone.getDefault())

    init {
        (itemView as ComposeView).setContent {
            BreezyWeatherTheme(dynamicColor = false) {
                ClockCard()
            }
        }
    }

    @Composable
    private fun ClockCard() {
        ElevatedCard(
            modifier = Modifier.aspectRatio(1f),
            shape = MaterialShapes.Cookie12Sided.toShape(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            onClick = { onClick() }) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(0.8f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(), factory = { context ->
                        makeTextClock(context).apply {
                            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                            format12Hour = "hh"
                            format24Hour = "HH"
                        }
                    }, update = {
                        it.timeZone = timezone.value?.id
                    })
                    AndroidView(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(), factory = { context ->
                        makeTextClock(context).apply {
                            gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                            format12Hour = "mm"
                            format24Hour = "mm"
                        }
                    }, update = {
                        it.timeZone = timezone.value?.id
                    })
                }

                AndroidView(factory = { context ->
                    AnalogClock(context).apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            dialTintList = ColorStateList.valueOf(Color.Transparent.toArgb())
                        }
                    }
                }, update = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        it.visibility = View.VISIBLE
                        it.timeZone = timezone.value?.id
                    } else if (timezone.value == TimeZone.getDefault()) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                })
            }
        }
    }

    private fun makeTextClock(context: Context) = TextClock(context).apply {
        ellipsize = TextUtils.TruncateAt.END
        maxLines = 1
        includeFontPadding = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setAutoSizeTextTypeUniformWithConfiguration(50, 130, 1, TypedValue.COMPLEX_UNIT_SP)
        }
        setTextAppearance(R.style.Weather_TextAppearance_MainBlock_Clock)
        // The appearance also sets a color, so setTextColor has to be second.
        setTextColor(ColorStateList.valueOf(context.getThemeColor(android.R.attr.colorPrimary)))
    }

    private var onClick: () -> Unit = {}

    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        timezone.value = location.timeZone

        val talkBackBuilder = StringBuilder(context.getString(R.string.clock))
        talkBackBuilder.append(context.getString(R.string.colon_separator))
        talkBackBuilder.append(Date().getFormattedTime(location, activity, context.is12Hour))
        itemView.contentDescription = talkBackBuilder.toString()

        onClick = {
            IntentHelper.startDailyWeatherActivity(
                context as BreezyActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_CONDITIONS
            )
        }
    }
}

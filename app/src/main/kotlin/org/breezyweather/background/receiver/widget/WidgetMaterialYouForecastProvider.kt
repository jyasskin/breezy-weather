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

package org.breezyweather.background.receiver.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.SizeF
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.breezyweather.remoteviews.presenters.MaterialYouForecastWidgetIMP
import javax.inject.Inject

@AndroidEntryPoint
class WidgetMaterialYouForecastProvider : AppWidgetProvider() {

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val minwidth_dp: Int? = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val maxwidth_dp: Int? = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val minheight_dp: Int? = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maxheight_dp: Int? = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        @Suppress("DEPRECATION") val sizes = newOptions?.getParcelableArrayList<SizeF>(
            AppWidgetManager.OPTION_APPWIDGET_SIZES
        )
        Log.d(WidgetMaterialYouForecastProvider::class.java.simpleName,
            "Options changed: minwidth=${minwidth_dp}dp, maxwidth=${maxwidth_dp}dp, minheight=${minheight_dp}dp, maxheight=${maxheight_dp}dp.");
        Log.d(WidgetMaterialYouForecastProvider::class.java.simpleName,
            sizes?.map { "${it.width}x${it.height}dp" }?.joinToString(", ", "Possible sizes: ")?:"null");
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (MaterialYouForecastWidgetIMP.isEnabled(context)) {
            GlobalScope.launch(Dispatchers.IO) {
                val location = locationRepository.getFirstLocation(withParameters = false)
                MaterialYouForecastWidgetIMP.updateWidgetView(
                    context,
                    location?.copy(
                        weather = weatherRepository.getWeatherByLocationId(
                            location.formattedId,
                            withDaily = true,
                            withHourly = true,
                            withMinutely = false,
                            withAlerts = false,
                            withNormals = false
                        )
                    )
                )
            }
        }
    }
}

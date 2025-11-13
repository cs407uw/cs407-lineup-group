package com.cs407.lineup.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.cs407.lineup.MainActivity
import com.cs407.lineup.data.RestaurantPrefs
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.sp
import androidx.glance.background
import com.cs407.lineup.R


/* Import Glance Composables
 In the event there is a name clash with the Compose classes of the same name,
 you may rename the imports per https://kotlinlang.org/docs/packages.html#imports
 using the `as` keyword.

*/
class LastRestaurantWidget : GlanceAppWidget() {

    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = RestaurantPrefs.loadRestaurant(context)

        provideContent {
            Column(modifier = GlanceModifier.padding(12.dp).background(ColorProvider(R.color.white))) {
                Text(
                    text = data.name ?: "No restaurant selected",
                    style = TextStyle(
                        fontSize = 20.sp,
                    )
                )
                Text(
                    text = "Wait: ${data.wait ?: 0} min",
                    style = TextStyle(
                        fontSize = 16.sp,
                    )
                )

                Button(
                    text = "Open App",
                    onClick = actionStartActivity<MainActivity>()
                )
            }
        }
    }
}
package com.cs407.lineup.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import com.cs407.lineup.MainActivity
import com.cs407.lineup.R
import com.cs407.lineup.data.RestaurantPrefs
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable


class LastRestaurantWidget : GlanceAppWidget() {

    @SuppressLint("RestrictedApi")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = RestaurantPrefs.loadRestaurant(context)

        val name = data.name ?: "No restaurant selected"
        val wait = data.wait ?: 0

        provideContent {

            Column(
                modifier = GlanceModifier.fillMaxSize().padding(12.dp).cornerRadius(16.dp)
                    .background(ColorProvider(R.color.white)),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {

                Text(
                    text = name, style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                )

                Spacer(GlanceModifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Column(
                        modifier = GlanceModifier.background(ColorProvider(R.color.white))
                            .padding(6.dp).cornerRadius(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$wait", style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ColorProvider(R.color.dark_green)
                            )
                        )
                        Text(
                            text = "MIN", style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = FontFamily(R.font.ubuntu.toString()),
                                color = ColorProvider(android.R.color.black)
                            )
                        )
                    }

                    Spacer(GlanceModifier.width(10.dp))

                    Text(
                        text = "Estimated wait time", style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(R.font.ubuntu.toString()),
                            color = ColorProvider(android.R.color.darker_gray)
                        )
                    )
                }

                Spacer(GlanceModifier.height(14.dp))

                Box(
                    modifier = GlanceModifier.fillMaxWidth().height(40.dp).cornerRadius(12.dp)
                        .background(ColorProvider(R.color.dark_green))
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Open App", style = TextStyle(
                            fontSize = 16.sp,
                            color = ColorProvider(android.R.color.white),
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

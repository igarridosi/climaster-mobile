package com.example.climaster.widget

import com.google.gson.annotations.SerializedName

data class WidgetConfig(
    @SerializedName("widgetId") val widgetId: String,
    @SerializedName("background") val background: WidgetBackground,
    @SerializedName("layout") val layout: List<WidgetElement>
)

data class WidgetBackground(
    @SerializedName("color") val color: String, // "#33FFFFFF"
    @SerializedName("cornerRadius") val cornerRadius: Int
)

data class WidgetElement(
    @SerializedName("type") val type: String, // "current_temp", "humidity"...
    @SerializedName("fontSize") val fontSize: Int = 14,
    @SerializedName("alignment") val alignment: String = "left",
    @SerializedName("days") val days: Int = 3
)
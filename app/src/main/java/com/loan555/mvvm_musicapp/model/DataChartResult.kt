package com.loan555.mvvm_musicapp.model

import com.google.gson.annotations.SerializedName

data class DataChartResult(
    val err: Long,
    val msg: String,
    val data: Chart,
    val timestamp: Long
)

data class Chart(
    val song: List<Song>,
    @SerializedName("customied")
    val customID: List<Any?>,

    @SerializedName("peak_score")
    val peakScore: Long,

    val songHis: SongHis
)

enum class RankStatus(val value: String) {
    Down("down"),
    Stand("stand"),
    Up("up");

    companion object {
        public fun fromValue(value: String): RankStatus = when (value) {
            "down" -> Down
            "stand" -> Stand
            "up" -> Up
            else -> throw IllegalArgumentException()
        }
    }
}

enum class Type(val value: String) {
    Audio("audio");

    companion object {
        public fun fromValue(value: String): Type = when (value) {
            "audio" -> Audio
            else -> throw IllegalArgumentException()
        }
    }
}

data class SongHis(
    @SerializedName("min_score")
    val minScore: Long,

    @SerializedName("max_score")
    val maxScore: Double,

    val from: Long,
    val interval: Long,
    val data: Map<String, List<Datum>>,
    val score: Map<String, Score>,

    @SerializedName("total_score")
    val totalScore: Long
)

data class Datum(
    val time: Long,
    val hour: String,
    val counter: Long
)

data class Score(
    @SerializedName("total_score")
    val totalScore: Long,

    @SerializedName("total_peak_score")
    val totalPeakScore: Long,

    @SerializedName("total_score_realtime")
    val totalScoreRealtime: Long
)
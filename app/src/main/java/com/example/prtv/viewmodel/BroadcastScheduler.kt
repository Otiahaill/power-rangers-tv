package com.example.prtv.viewmodel

import com.example.prtv.data.model.YouTubeVideo
import java.util.*

/**
 * Broadcast Scheduler - Ported from power-rangers-broadcast web app
 * 
 * Creates a universal "broadcast schedule" where all users
 * see the same video at the same time, like a real TV channel.
 * 
 * The schedule:
 * 1. Uses a fixed epoch (Jan 1, 2024 00:00:00 UTC) as the "start" of broadcasting
 * 2. Shuffles videos deterministically (same order for everyone)
 * 3. Calculates which video should be playing now based on elapsed time
 */
object BroadcastScheduler {

    private const val EPOCH_START_YEAR = 2024
    private const val EPOCH_START_MONTH = 1 // January
    private const val EPOCH_START_DAY = 1

    /**
     * LCG-based seeded shuffle (same algorithm as web app)
     * Seed = YYYYMMDD (changes daily, ensures same order for all users)
     */
    fun seededShuffle(list: List<YouTubeVideo>, seed: Int): List<YouTubeVideo> {
        val result = list.toMutableList()
        var currentIndex = result.size
        var currentSeed = seed.toLong()

        // Linear Congruential Generator
        val seededRandom: () -> Double = {
            currentSeed = (currentSeed * 1103515245 + 12345) and 0x7fffffff
            currentSeed.toDouble() / 0x7fffffff
        }

        while (currentIndex > 0) {
            val randomIndex = (seededRandom() * currentIndex).toInt()
            currentIndex--
            result[currentIndex] = result[randomIndex].also { result[randomIndex] = result[currentIndex] }
        }

        return result
    }

    /**
     * Calculate the current broadcast state for simulated live
     * 
     * Returns null if no videos or total duration is 0
     */
    fun calculateBroadcastState(videos: List<YouTubeVideo>): BroadcastState? {
        if (videos.isEmpty()) return null

        // Use daily seed (YYYYMMDD) for daily schedule changes
        val today = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val daySeed = today[Calendar.YEAR] * 10000 + (today[Calendar.MONTH] + 1) * 100 + today[Calendar.DAY_OF_MONTH]
        val shuffledVideos = seededShuffle(videos, daySeed)

        // Calculate total playlist duration
        val totalDuration = shuffledVideos.sumOf { it.durationSeconds }
        if (totalDuration == 0) return null

        // Epoch: Start of the "broadcast day" (midnight UTC of EPOCH_START)
        val epochStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(EPOCH_START_YEAR, EPOCH_START_MONTH - 1, EPOCH_START_DAY, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Time elapsed since epoch start (in seconds)
        val now = System.currentTimeMillis()
        val elapsedSeconds = (now - epochStart) / 1000L

        // Position within the looping playlist
        val positionInPlaylist = elapsedSeconds % totalDuration

        // Find which video should be playing
        var accumulatedDuration = 0L
        for ((index, video) in shuffledVideos.withIndex()) {
            if (accumulatedDuration + video.durationSeconds > positionInPlaylist) {
                val startTimeInVideo = (positionInPlaylist - accumulatedDuration).toInt()
                return BroadcastState(
                    isRealLive = false,
                    currentVideoId = video.id,
                    currentVideoTitle = video.title,
                    currentVideoIndex = index,
                    startTimeInVideo = startTimeInVideo,
                    totalVideos = shuffledVideos.size
                )
            }
            accumulatedDuration += video.durationSeconds
        }

        // Fallback to first video
        return BroadcastState(
            isRealLive = false,
            currentVideoId = shuffledVideos[0].id,
            currentVideoTitle = shuffledVideos[0].title,
            currentVideoIndex = 0,
            startTimeInVideo = 0,
            totalVideos = shuffledVideos.size
        )
    }

    /**
     * Get the next video in the broadcast queue
     */
    fun getNextVideo(videos: List<YouTubeVideo>, currentIndex: Int): YouTubeVideo? {
        if (videos.isEmpty()) return null

        val today = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val daySeed = today[Calendar.YEAR] * 10000 + (today[Calendar.MONTH] + 1) * 100 + today[Calendar.DAY_OF_MONTH]
        val shuffledVideos = seededShuffle(videos, daySeed)

        val nextIndex = (currentIndex + 1) % shuffledVideos.size
        return shuffledVideos[nextIndex]
    }

    data class BroadcastState(
        val isRealLive: Boolean,
        val currentVideoId: String,
        val currentVideoTitle: String,
        val currentVideoIndex: Int,
        val startTimeInVideo: Int, // seconds into the video
        val totalVideos: Int
    ) {
        val startTimeFormatted: String
            get() {
                val m = startTimeInVideo / 60
                val s = startTimeInVideo % 60
                return String.format("%02d:%02d", m, s)
            }
    }
}

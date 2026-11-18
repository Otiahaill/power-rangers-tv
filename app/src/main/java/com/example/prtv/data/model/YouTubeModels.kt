package com.example.prtv.data.model

import com.google.gson.annotations.SerializedName

// Playlist models
data class YouTubePlaylist(
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: PlaylistSnippet,
    @SerializedName("contentDetails") val contentDetails: ContentDetails
) {
    val title: String = snippet.title
    val description: String = snippet.description
    val thumbnailUrl: String = snippet.thumbnails?.medium?.url ?: snippet.thumbnails?.default?.url ?: ""
    val itemCount: Int = contentDetails.itemCount
}

data class PlaylistSnippet(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("thumbnails") val thumbnails: Thumbnails?,
    @SerializedName("channelTitle") val channelTitle: String
)

data class ContentDetails(
    @SerializedName("itemCount") val itemCount: Int
)

data class Thumbnails(
    @SerializedName("default") val default: Thumbnail?,
    @SerializedName("medium") val medium: Thumbnail?,
    @SerializedName("high") val high: Thumbnail?
)

data class Thumbnail(
    @SerializedName("url") val url: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int
)

// Video models
data class YouTubeVideo(
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: VideoSnippet,
    @SerializedName("contentDetails") val contentDetails: VideoContentDetails?
) {
    val title: String = snippet.title
    val description: String = snippet.description
    val thumbnailUrl: String = snippet.thumbnails?.medium?.url ?: snippet.thumbnails?.default?.url ?: ""
    val channelName: String = snippet.channelTitle
    val publishedAt: String = snippet.publishedAt
    val durationSeconds: Int = parseDuration(contentDetails?.duration ?: "PT0S")
    val durationFormatted: String
        get() {
            val total = durationSeconds
            val h = total / 3600
            val m = (total % 3600) / 60
            val s = total % 60
            return if (h > 0) "$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}" else "$m:$s"
        }
}

data class VideoSnippet(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("thumbnails") val thumbnails: Thumbnails?,
    @SerializedName("channelTitle") val channelTitle: String,
    @SerializedName("publishedAt") val publishedAt: String
)

data class VideoContentDetails(
    @SerializedName("duration") val duration: String
)

// API Response wrappers
data class PlaylistListResponse(
    @SerializedName("items") val items: List<YouTubePlaylist>,
    @SerializedName("nextPageToken") val nextPageToken: String?,
    @SerializedName("pageInfo") val pageInfo: PageInfo?
)

data class VideoListResponse(
    @SerializedName("items") val items: List<YouTubeVideo>,
    @SerializedName("nextPageToken") val nextPageToken: String?,
    @SerializedName("pageInfo") val pageInfo: PageInfo?
)

data class SearchResponse(
    @SerializedName("items") val items: List<SearchItem>,
    @SerializedName("pageInfo") val pageInfo: PageInfo?
)

data class SearchItem(
    @SerializedName("id") val id: SearchId,
    @SerializedName("snippet") val snippet: VideoSnippet
)

data class SearchId(
    @SerializedName("videoId") val videoId: String?
)

data class PageInfo(
    @SerializedName("totalResults") val totalResults: Int,
    @SerializedName("resultsPerPage") val resultsPerPage: Int
)

data class ChannelResponse(
    @SerializedName("items") val items: List<ChannelItem>
)

data class ChannelItem(
    @SerializedName("id") val id: String,
    @SerializedName("snippet") val snippet: ChannelSnippet
)

data class ChannelSnippet(
    @SerializedName("title") val title: String,
    @SerializedName("customUrl") val customUrl: String?
)

/**
 * Parses ISO 8601 duration (PT1H2M3S) to seconds
 */
fun parseDuration(isoDuration: String): Int {
    val pattern = java.util.regex.Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
    val matcher = pattern.matcher(isoDuration)
    if (!matcher.matches()) return 0

    val hours = matcher.group(1)?.toIntOrNull() ?: 0
    val minutes = matcher.group(2)?.toIntOrNull() ?: 0
    val seconds = matcher.group(3)?.toIntOrNull() ?: 0

    return hours * 3600 + minutes * 60 + seconds
}

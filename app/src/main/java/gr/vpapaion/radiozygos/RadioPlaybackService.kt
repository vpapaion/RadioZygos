package gr.vpapaion.radiozygos

import android.app.Notification
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class RadioPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()

        val exoPlayer = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )

            val metadata = MediaMetadata.Builder()
                .setTitle(STATION_NAME)
                .setArtist(STATION_LOCATION)
                .setAlbumTitle("Live Radio")
                .build()

            setMediaItem(
                MediaItem.Builder()
                    .setUri(STREAM_URL)
                    .setMediaMetadata(metadata)
                    .build()
            )
            prepare()
        }

        player = exoPlayer
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setId("radio_zygos_session")
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        player?.pause()
        stopSelf()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        player = null
        super.onDestroy()
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, startInForegroundRequired)
    }

    companion object {
        const val STREAM_URL = "https://sh.onweb.gr/8870/;"
        const val STATION_NAME = "Ράδιο Ζυγός 100"
        const val STATION_LOCATION = "Τρίκαλα FM 100"
    }
}

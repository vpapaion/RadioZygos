package gr.vpapaion.radiozygos

import android.Manifest
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : AppCompatActivity() {

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private lateinit var playPauseButton: ImageButton
    private lateinit var statusText: TextView

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playPauseButton = findViewById(R.id.playPauseButton)
        statusText = findViewById(R.id.statusText)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        playPauseButton.setOnClickListener {
            val mediaController = controller ?: return@setOnClickListener
            if (mediaController.isPlaying) {
                mediaController.pause()
            } else {
                mediaController.play()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, RadioPlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync().also { future ->
            future.addListener(
                {
                    controller = future.get().also { mediaController ->
                        mediaController.addListener(object : Player.Listener {
                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                updateUi(isPlaying)
                            }
                        })
                        updateUi(mediaController.isPlaying)
                    }
                },
                MoreExecutors.directExecutor()
            )
        }
    }

    override fun onStop() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        controller = null
        super.onStop()
    }

    private fun updateUi(isPlaying: Boolean) {
        playPauseButton.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow)
        playPauseButton.contentDescription = if (isPlaying) getString(R.string.pause) else getString(R.string.play)
        statusText.text = if (isPlaying) getString(R.string.playing_now) else getString(R.string.ready_to_play)
    }
}

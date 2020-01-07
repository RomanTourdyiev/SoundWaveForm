package tink.co.soundform.util;

import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import tink.co.soundform.Record;
import tink.co.soundform.callback.PlaybackCallback;

/**
 * Created by Tourdyiev Roman on 2019-09-15.
 */
public class RecordUtils implements MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener {

    private PlaybackCallback playbackCallback;
    private static MediaPlayer mediaPlayer;

    private int playbackPosition = 0;
    private int mediaFileLengthInMilliseconds;
    private boolean prepared = false;

    private Handler handler;

    private static RecordUtils instance = new RecordUtils();

    private RecordUtils() {

    }

    public static RecordUtils getInstance() {
        return instance;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (playbackCallback != null) {
            playbackCallback.onBufferingUpdate(percent);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
//        if (mediaPlayer!=null) {
//            if (mediaPlayer.isPlaying()) {
//                if (playbackCallback != null) {
//                    playbackCallback.onMediaPlayerCompletion();
//                }
//            }
//        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        prepared = true;
        if (playbackCallback != null) {
            playbackCallback.onMediaPlayerPrepared();
        }
    }

    public void initMediaPlayer() {
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
    }

    public void setPlaybackCallback(PlaybackCallback callback) {
        this.playbackCallback = callback;
    }

    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                handler.removeCallbacks(progressRunnable);
                mediaPlayer.stop();
//                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable progressRunnable = new Runnable() {
        public void run() {
            updateProgress();
        }
    };

    private void updateProgress() {

        if (playbackCallback != null) {
            playbackCallback.onProgressChanged(mediaPlayer.getCurrentPosition(),  mediaFileLengthInMilliseconds);

            if (mediaPlayer.isPlaying()) {
                handler.postDelayed(progressRunnable, 100);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setPlaybackSpeed(float speed) {
        if (prepared) {
            mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(speed));
        }
//        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
//            mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(speed));
//            mediaPlayer.reset();
//            mediaPlayer.start();
//        }
    }

    public void toggleAudio(final Record record) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    mediaPlayer.setDataSource(record.getPath());
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mediaFileLengthInMilliseconds = mediaPlayer.getDuration();

                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    if (playbackCallback != null) {
                        playbackCallback.onPlayPause(true);
                    }
                } else {
                    mediaPlayer.pause();
                    if (playbackCallback != null) {
                        playbackCallback.onPlayPause(false);
                    }
                }

                updateProgress();
            }
        });
    }

    public void stopAudio(final Record record) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    mediaPlayer.setDataSource(record.getPath());
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mediaFileLengthInMilliseconds = mediaPlayer.getDuration();

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    if (playbackCallback != null) {
                        playbackCallback.onPlayPause(false);
                    }
                }

                updateProgress();
            }
        });
    }

    public void seekBy(int diff) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + diff);
        }
    }

    public void seekTo(int percent) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo((mediaFileLengthInMilliseconds / 100) * percent);
        }
    }
}

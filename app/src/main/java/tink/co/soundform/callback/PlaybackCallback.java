package tink.co.soundform.callback;

/**
 * Created by Tourdyiev Roman on 2019-09-15.
 */
public interface PlaybackCallback {
    void onProgressChanged(int currentPosition, int mediaFileLengthInMilliseconds);
    void onPlayPause(boolean nowPlaying);
    void onMediaPlayerCompletion();
    void onMediaPlayerPrepared();
    void onBufferingUpdate(int percent);
}

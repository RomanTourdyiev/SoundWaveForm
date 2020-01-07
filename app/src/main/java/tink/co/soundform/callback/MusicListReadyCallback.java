package tink.co.soundform.callback;

import java.util.List;

import tink.co.soundform.Record;

/**
 * Created by Tourdyiev Roman on 2020-01-06.
 */
public interface MusicListReadyCallback {
    void OnMusicListReady(List<Record> records);
    void OnStartMusicList();
}
